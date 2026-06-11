package com.example.ui

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.json.JSONArray

data class RemoteWorkerTelemetry(
    val email: String,
    val name: String,
    val visits: List<VisitLog> = emptyList(),
    val dayTracks: List<WorkerDayTrack> = emptyList(),
    val lastSyncTime: Long = 0L
)

class CRMViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = CRMRepository(db.crmDao())
    private val prefs = application.getSharedPreferences("lubecrm_prefs", Context.MODE_PRIVATE)

    // Logged in user details
    val userRole = MutableStateFlow<String?>(null)
    val userEmail = MutableStateFlow<String?>(null)
    val userDisplayName = MutableStateFlow<String?>(null)

    // Remote sync credentials
    val firebaseDbUrl = MutableStateFlow<String>("https://lubecrm-sales-default-rtdb.firebaseio.com")
    val isSyncing = MutableStateFlow(false)
    val syncStatusMessage = MutableStateFlow<String>("Offline local mode.")

    // Owner only: remote worker tracking data
    val remoteWorkersTelemetry = MutableStateFlow<List<RemoteWorkerTelemetry>>(emptyList())

    // Expose flows with StateFlow
    val products: StateFlow<List<LubricantProduct>> = repository.allProducts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val customers: StateFlow<List<CustomerContact>> = repository.allCustomers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val visits: StateFlow<List<VisitLog>> = repository.allVisits
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val submissions: StateFlow<List<FormSubmission>> = repository.allSubmissions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val dayTracks: StateFlow<List<WorkerDayTrack>> = repository.allDayTracks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val isTrackingActive = MutableStateFlow(false)
    val currentKm = MutableStateFlow(0.0)
    val currentRoutePoints = MutableStateFlow<List<String>>(emptyList())
    private var activeTrackId: Int? = null
    private var trackingJob: kotlinx.coroutines.Job? = null

    init {
        // Load user credentials and configuration
        userRole.value = prefs.getString("user_role", null)
        userEmail.value = prefs.getString("user_email", null)
        userDisplayName.value = prefs.getString("user_display_name", null)
        val savedUrl = prefs.getString("firebase_db_url", "https://lubecrm-sales-default-rtdb.firebaseio.com")
        firebaseDbUrl.value = if (savedUrl.isNullOrEmpty()) "https://lubecrm-sales-default-rtdb.firebaseio.com" else savedUrl

        // Run seed prepopulate in IO
        viewModelScope.launch(Dispatchers.IO) {
            repository.prepopulateIfEmpty()
            
            // Auto sync on start if user is already logged in
            if (userRole.value != null) {
                syncWithFirebase()
            }
        }
    }

    // ===== AUTHENTICATION =====
    fun loginUser(email: String, role: String, displayName: String) {
        prefs.edit().apply {
            putString("user_role", role)
            putString("user_email", email)
            putString("user_display_name", displayName)
            apply()
        }
        userRole.value = role
        userEmail.value = email
        userDisplayName.value = displayName
        
        syncStatusMessage.value = "Logged in as $displayName ($role)"
        
        // Auto Sync on sign in
        viewModelScope.launch(Dispatchers.IO) {
            syncWithFirebase()
        }
    }

    fun logoutUser() {
        prefs.edit().apply {
            remove("user_role")
            remove("user_email")
            remove("user_display_name")
            apply()
        }
        userRole.value = null
        userEmail.value = null
        userDisplayName.value = null
        remoteWorkersTelemetry.value = emptyList()
        syncStatusMessage.value = "Signed out."
    }

    fun saveFirebaseConfig(url: String) {
        var cleanUrl = url.trim()
        if (cleanUrl.endsWith("/")) {
            cleanUrl = cleanUrl.dropLast(1)
        }
        if (cleanUrl.isNotEmpty() && !cleanUrl.startsWith("http")) {
            cleanUrl = "https://$cleanUrl"
        }
        prefs.edit().putString("firebase_db_url", cleanUrl).apply()
        firebaseDbUrl.value = cleanUrl
        
        viewModelScope.launch(Dispatchers.IO) {
            syncWithFirebase()
        }
    }

    // ===== REMOTE FIREBASE REST SYNC ENGINE =====
    fun syncWithFirebase() {
        val currentDbUrl = firebaseDbUrl.value.trim()
        if (currentDbUrl.isEmpty()) {
            syncStatusMessage.value = "Enter a Firebase DB URL."
            return
        }
        val role = userRole.value ?: return
        val email = userEmail.value ?: return
        val displayName = userDisplayName.value ?: ""

        val cleanNickname = email.replace(".", "_")
            .replace("@", "_")
            .replace("#", "_")
            .replace("$", "_")
            .replace("[", "_")
            .replace("]", "_")

        if (isSyncing.value) return
        isSyncing.value = true
        syncStatusMessage.value = "Syncing in progress..."

        viewModelScope.launch(Dispatchers.IO) {
            val client = OkHttpClient()

            try {
                if (role == "Worker") {
                    // Upload locally stored visits
                    syncStatusMessage.value = "Syncing visits history..."
                    val visitsList = visits.value
                    val visitsObj = JSONObject()
                    for (v in visitsList) {
                        val visitJson = JSONObject().apply {
                            put("id", v.id)
                            put("customerName", v.customerName)
                            put("representativeName", v.representativeName)
                            put("visitDate", v.visitDate)
                            put("visitType", v.visitType)
                            put("notes", v.notes)
                            put("interestedProduct", v.interestedProduct)
                            put("orderQuantity", v.orderQuantity)
                            put("wasOrderPlaced", v.wasOrderPlaced)
                            put("simulatedLocation", v.simulatedLocation)
                            put("customerResponseStatus", v.customerResponseStatus)
                            put("closingRequirements", v.closingRequirements)
                        }
                        visitsObj.put(v.id.toString(), visitJson)
                    }

                    // Upload GPS shifting logs
                    syncStatusMessage.value = "Syncing GPS shifts..."
                    val tracksList = dayTracks.value
                    val tracksObj = JSONObject()
                    for (t in tracksList) {
                        val trackJson = JSONObject().apply {
                            put("id", t.id)
                            put("dateString", t.dateString)
                            put("startTime", t.startTime)
                            put("endTime", t.endTime)
                            put("kmTraveled", t.kmTraveled)
                            put("status", t.status)
                            put("visitedCount", t.visitedCount)
                            put("routePointsString", t.routePointsString)
                        }
                        tracksObj.put(t.id.toString(), trackJson)
                    }

                    // Bundle all state
                    val profileJson = JSONObject().apply {
                        put("email", email)
                        put("name", displayName)
                        put("visits", visitsObj)
                        put("dayTracks", tracksObj)
                        put("lastSyncTime", System.currentTimeMillis())
                    }

                    val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                    val body = profileJson.toString().toRequestBody(mediaType)

                    // Issue HTTP PUT to Firebase Realtime Database REST API
                    val request = Request.Builder()
                        .url("$currentDbUrl/workers/$cleanNickname.json")
                        .put(body)
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            syncStatusMessage.value = "Sync complete. Remote database updated!"
                        } else {
                            syncStatusMessage.value = "Local DB ready. Remote offline (Code ${response.code})"
                        }
                    }
                } else if (role == "Owner") {
                    syncStatusMessage.value = "Loading remote sales telemetry..."
                    
                    val request = Request.Builder()
                        .url("$currentDbUrl/workers.json")
                        .get()
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val respStr = response.body?.string() ?: "{}"
                            if (respStr.trim() == "null" || respStr.trim().isEmpty()) {
                                syncStatusMessage.value = "Cloud database is empty."
                                remoteWorkersTelemetry.value = emptyList()
                            } else {
                                val parsedTelemetryList = mutableListOf<RemoteWorkerTelemetry>()
                                val rootJson = JSONObject(respStr)
                                
                                val keys = rootJson.keys()
                                while (keys.hasNext()) {
                                    val kNickname = keys.next()
                                    val workerObj = rootJson.getJSONObject(kNickname)
                                    val wEmail = workerObj.optString("email", kNickname)
                                    val wName = workerObj.optString("name", "Unknown Staff")
                                    val lastSync = workerObj.optLong("lastSyncTime", 0L)

                                    // Parse visits
                                    val parsedVisits = mutableListOf<VisitLog>()
                                    if (workerObj.has("visits")) {
                                        val visitsJson = workerObj.getJSONObject("visits")
                                        val vKeys = visitsJson.keys()
                                        while (vKeys.hasNext()) {
                                            val vId = vKeys.next()
                                            val vObj = visitsJson.getJSONObject(vId)
                                            parsedVisits.add(
                                                VisitLog(
                                                    id = vObj.optInt("id", 0),
                                                    customerName = vObj.optString("customerName", ""),
                                                    representativeName = vObj.optString("representativeName", ""),
                                                    visitDate = vObj.optLong("visitDate", System.currentTimeMillis()),
                                                    visitType = vObj.optString("visitType", ""),
                                                    notes = vObj.optString("notes", ""),
                                                    interestedProduct = vObj.optString("interestedProduct", ""),
                                                    orderQuantity = vObj.optInt("orderQuantity", 0),
                                                    wasOrderPlaced = vObj.optBoolean("wasOrderPlaced", false),
                                                    simulatedLocation = vObj.optString("simulatedLocation", ""),
                                                    customerResponseStatus = vObj.optString("customerResponseStatus", ""),
                                                    closingRequirements = vObj.optString("closingRequirements", "")
                                                )
                                            )
                                        }
                                    }

                                    // Parse tracks
                                    val parsedTracks = mutableListOf<WorkerDayTrack>()
                                    if (workerObj.has("dayTracks")) {
                                        val tracksJson = workerObj.getJSONObject("dayTracks")
                                        val tKeys = tracksJson.keys()
                                        while (tKeys.hasNext()) {
                                            val tId = tKeys.next()
                                            val tObj = tracksJson.getJSONObject(tId)
                                            parsedTracks.add(
                                                WorkerDayTrack(
                                                    id = tObj.optInt("id", 0),
                                                    dateString = tObj.optString("dateString", ""),
                                                    startTime = tObj.optLong("startTime", 0L),
                                                    endTime = tObj.optLong("endTime", 0L),
                                                    kmTraveled = tObj.optDouble("kmTraveled", 0.0),
                                                    status = tObj.optString("status", "Completed"),
                                                    visitedCount = tObj.optInt("visitedCount", 0),
                                                    routePointsString = tObj.optString("routePointsString", "")
                                                )
                                            )
                                        }
                                    }

                                    parsedTelemetryList.add(
                                        RemoteWorkerTelemetry(
                                            email = wEmail,
                                            name = wName,
                                            visits = parsedVisits.sortedByDescending { it.visitDate },
                                            dayTracks = parsedTracks.sortedByDescending { it.startTime },
                                            lastSyncTime = lastSync
                                        )
                                    )
                                }
                                remoteWorkersTelemetry.value = parsedTelemetryList
                                syncStatusMessage.value = "Synced remote activity. Loaded ${parsedTelemetryList.size} workers."
                            }
                        } else {
                            syncStatusMessage.value = "Remote sync offline. (HTTP Code ${response.code})"
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("FirebaseSync", "Sync failed", e)
                syncStatusMessage.value = "Offline sync (Verify URL settings)"
            } finally {
                isSyncing.value = false
            }
        }
    }

    // ===== LUBRICANT PRODUCT CRUD =====
    fun addProduct(name: String, category: String, packSize: String, availableStock: Int, unitPrice: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertProduct(
                LubricantProduct(
                    name = name,
                    category = category,
                    packSize = packSize,
                    availableStock = availableStock,
                    soldStock = 0,
                    unitPrice = unitPrice
                )
            )
        }
    }

    fun updateProduct(product: LubricantProduct) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateProduct(product)
        }
    }

    fun deleteProduct(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteProductById(id)
        }
    }

    fun addStock(productId: Int, addQuantity: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val productList = products.value
            val match = productList.find { it.id == productId }
            if (match != null) {
                repository.updateProduct(
                    match.copy(availableStock = match.availableStock + addQuantity)
                )
            }
        }
    }

    // ===== CUSTOMER CONTACT CRUD =====
    fun addCustomer(name: String, contactPerson: String, phone: String, type: String, address: String, shopLocationLink: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertCustomer(
                CustomerContact(
                    name = name,
                    contactPerson = contactPerson,
                    phone = phone,
                    type = type,
                    address = address,
                    shopLocationLink = shopLocationLink
                )
            )
        }
    }

    fun deleteCustomer(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCustomerById(id)
        }
    }

    // ===== VISIT LOGGER =====
    fun logVisit(
        customerName: String,
        representativeName: String,
        visitType: String,
        notes: String,
        interestedProduct: String,
        orderQuantity: Int,
        wasOrderPlaced: Boolean,
        location: String,
        customerResponseStatus: String,
        closingRequirements: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            // Add visit log
            repository.insertVisit(
                VisitLog(
                    customerName = customerName,
                    representativeName = representativeName,
                    visitType = visitType,
                    notes = notes,
                    interestedProduct = interestedProduct,
                    orderQuantity = orderQuantity,
                    wasOrderPlaced = wasOrderPlaced,
                    simulatedLocation = location,
                    customerResponseStatus = customerResponseStatus,
                    closingRequirements = closingRequirements
                )
            )
            
            // If order was placed, update the stock accordingly!
            if (wasOrderPlaced && orderQuantity > 0 && interestedProduct.isNotEmpty()) {
                val match = products.value.find { it.name.lowercase() == interestedProduct.lowercase() }
                if (match != null) {
                    val finalDeliver = if (match.availableStock >= orderQuantity) orderQuantity else match.availableStock
                    repository.updateProduct(
                        match.copy(
                            availableStock = (match.availableStock - finalDeliver).coerceAtLeast(0),
                            soldStock = match.soldStock + finalDeliver
                        )
                    )
                }
            }
            
            // Auto cloudsync after logging visit
            if (userRole.value == "Worker") {
                syncWithFirebase()
            }
        }
    }

    fun deleteVisit(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteVisitById(id)
        }
    }

    // ===== FORM LINK SHARING & SIMULATION =====
    fun getShareableMessage(formType: String, extraInfo: String = ""): String {
        return when (formType) {
            "Mechanic Loyalty Sign-Up" -> {
                "Hello! Please join our LubeCRM Mechanic Loyalty Program to earn points on every lubricant consumption. Submit your details here: https://forms.lubecrm.axqypt/mechanic-signup?ref=sales_rep_direct"
            }
            "Retailer Booking Form" -> {
                "Dear Distributor / Retailer Partner, you can place your lubricant booking orders online directly into our production system. Tap to submit: https://forms.lubecrm.axqypt/order-booking?dealer=$extraInfo"
            }
            "Mechanic Feedback Survey" -> {
                "Hi, we value your expert opinion on our engine and hydraulic oils! Please submit your satisfaction feedback here: https://forms.lubecrm.axqypt/mechanic-feedback?visitId=$extraInfo"
            }
            else -> {
                "Dear Customer, please stay connected with us! Fill out our customer service card: https://forms.lubecrm.axqypt/general-inquiry"
            }
        }
    }

    // Simulate customer visiting that link and pressing submit
    fun simulatePublicFormSubmission(
        type: String,
        senderName: String,
        contactInfo: String,
        details: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertSubmission(
                FormSubmission(
                    formType = type,
                    senderName = senderName,
                    contactInfo = contactInfo,
                    content = details,
                    isProcessed = false
                )
            )
        }
    }

    fun processSubmission(submission: FormSubmission, registerAsCustomer: Boolean, approveOrder: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (registerAsCustomer) {
                // Determine contact type from form type
                val matchedType = if (submission.formType == "Mechanic Loyalty Sign-Up") "Mechanic Garage" else "Retailer Dealer"
                repository.insertCustomer(
                    CustomerContact(
                        name = submission.senderName,
                        contactPerson = "Owner / Contact",
                        phone = submission.contactInfo,
                        type = matchedType,
                        address = submission.content,
                        shopLocationLink = "https://maps.google.com/?q=${submission.content.replace(" ", "+")}"
                    )
                )
            }
            
            if (approveOrder && submission.formType == "Retailer Booking Form") {
                // Parse product and quantity from submission content (e.g., "Product: X | Qty: Y")
                // Format: "Requested Products: [Engine Max] | Qty: [40] | Notes: [Urgent delivery]"
                val contentLower = submission.content.lowercase()
                var parsedProduct = "Engine Max 4T 10W-30" // default
                var qty = 10 // default
                
                // Simple parser
                for (p in products.value) {
                    if (contentLower.contains(p.name.lowercase())) {
                        parsedProduct = p.name
                        break
                    }
                }
                
                val quantityRegex = "\\b(\\d+)\\b".toRegex()
                val match = quantityRegex.find(submission.content)
                if (match != null) {
                    qty = match.value.toIntOrNull() ?: 10
                }
                
                // Add an official visit log showing approved online booking
                repository.insertVisit(
                    VisitLog(
                        customerName = submission.senderName,
                        representativeName = "Online Web Portal",
                        visitType = "Order Booking",
                        notes = "APPROVED MOBILE FORM REQUEST. Customer online request content: ${submission.content}",
                        interestedProduct = parsedProduct,
                        orderQuantity = qty,
                        wasOrderPlaced = true,
                        simulatedLocation = "Web Form Gateway IP",
                        customerResponseStatus = "Ready to Buy",
                        closingRequirements = "Form integration completed automatically - dispatch scheduled"
                    )
                )
                
                // Update stock
                val matchProd = products.value.find { it.name.lowercase() == parsedProduct.lowercase() }
                if (matchProd != null) {
                    val finalDeliver = if (matchProd.availableStock >= qty) qty else matchProd.availableStock
                    repository.updateProduct(
                        matchProd.copy(
                            availableStock = (matchProd.availableStock - finalDeliver).coerceAtLeast(0),
                            soldStock = matchProd.soldStock + finalDeliver
                        )
                    )
                }
            }

            // Mark form entry as processed
            repository.updateSubmission(submission.copy(isProcessed = true))
        }
    }

    fun deleteSubmission(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSubmissionById(id)
        }
    }

    // ===== WORKER ACTIVE tracking & DAY TIMELINES =====
    fun startDayTracking(dateStr: String) {
        if (isTrackingActive.value) return
        
        isTrackingActive.value = true
        currentKm.value = 0.0
        currentRoutePoints.value = listOf("18.5204,73.8567,${getCurrentTimeString()}") // Start with base node (Pune center)

        viewModelScope.launch(Dispatchers.IO) {
            val nextTrack = WorkerDayTrack(
                dateString = dateStr,
                startTime = System.currentTimeMillis(),
                kmTraveled = 0.0,
                status = "Active",
                visitedCount = 0,
                routePointsString = currentRoutePoints.value.joinToString(";")
            )
            val insertedId = repository.insertDayTrack(nextTrack)
            activeTrackId = insertedId.toInt()
            
            // Start movement/GPS updates simulation
            startTrackingSimulationLoop()
        }
    }

    private fun startTrackingSimulationLoop() {
        trackingJob?.cancel()
        trackingJob = viewModelScope.launch(Dispatchers.Default) {
            var currentLat = 18.5204
            var currentLon = 73.8567
            
            while (isTrackingActive.value) {
                kotlinx.coroutines.delay(8000) // Increment every 8 seconds
                if (!isTrackingActive.value) break
                
                // Add a small location offset representing worker driving transit
                val latOffset = (Math.random() - 0.5) * 0.012
                val lonOffset = (Math.random() - 0.5) * 0.012
                currentLat += latOffset
                currentLon += lonOffset
                
                val speedCoef = 111.0 
                val deltaKm = Math.sqrt(latOffset * latOffset + lonOffset * lonOffset) * speedCoef
                val roundDelta = Math.round(deltaKm * 100.0) / 100.0
                
                currentKm.value = Math.round((currentKm.value + roundDelta) * 100.0) / 100.0
                val newList = currentRoutePoints.value.toMutableList()
                newList.add(String.format(java.util.Locale.US, "%.5f,%.5f,%s", currentLat, currentLon, getCurrentTimeString()))
                currentRoutePoints.value = newList
                
                // Keep the active DB entry up to date
                val trackId = activeTrackId
                if (trackId != null) {
                    val activeTrack = dayTracks.value.find { it.id == trackId }
                    if (activeTrack != null) {
                        repository.updateDayTrack(
                            activeTrack.copy(
                                kmTraveled = currentKm.value,
                                routePointsString = newList.joinToString(";")
                            )
                        )
                    }
                }
            }
        }
    }

    fun stopDayTracking() {
        if (!isTrackingActive.value) return
        trackingJob?.cancel()
        isTrackingActive.value = false
        
        val trackId = activeTrackId
        if (trackId != null) {
            viewModelScope.launch(Dispatchers.IO) {
                val activeTrack = dayTracks.value.find { it.id == trackId }
                if (activeTrack != null) {
                    // Update final values
                    // Auto-increment direct visits logged today during active shift
                    // Check logs with date matching the dateStr
                    val visitsLoggedToday = visits.value.count { 
                        // simple match check or just rely on a recent timestamp
                        System.currentTimeMillis() - it.visitDate < 57600000L // logged within last 16h
                    }
                    repository.updateDayTrack(
                        activeTrack.copy(
                            endTime = System.currentTimeMillis(),
                            status = "Completed",
                            kmTraveled = currentKm.value,
                            visitedCount = visitsLoggedToday.coerceAtLeast(1), // ensure at least 1 for display
                            routePointsString = currentRoutePoints.value.joinToString(";")
                        )
                    )
                }
                activeTrackId = null
                currentKm.value = 0.0
                currentRoutePoints.value = emptyList()
                
                // Auto cloudsync after closing day shift
                if (userRole.value == "Worker") {
                    syncWithFirebase()
                }
            }
        }
    }

    fun deleteDayTrack(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteDayTrackById(id)
        }
    }

    private fun getCurrentTimeString(): String {
        return try {
            val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US)
            sdf.format(java.util.Date())
        } catch (e: Exception) {
            "12:00 PM"
        }
    }
}
