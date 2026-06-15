package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesExecutivePortalScreen(
    viewModel: CRMViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isTracking by viewModel.isTrackingActive.collectAsState()
    val currentKm by viewModel.currentKm.collectAsState()
    val routePoints by viewModel.currentRoutePoints.collectAsState()
    
    val products by viewModel.products.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val visits by viewModel.visits.collectAsState()
    val dayTracks by viewModel.dayTracks.collectAsState()

    var executiveSubTab by remember { mutableIntStateOf(0) } // 0=Shift & Targets, 1=Route & CRM Engine, 2=Log Visit, 3=Expense claims

    // State parameters
    var selfieSimulatorUploaded by remember { mutableStateOf(false) }
    var mockLocationCheck by remember { mutableStateOf("Standby") }
    
    // Log Visit states
    var selectedCustomerForVisit by remember { mutableStateOf("") }
    var visitNotesText by remember { mutableStateOf("") }
    var interestedProductText by remember { mutableStateOf("") }
    var visitOrderPlacedChecked by remember { mutableStateOf(false) }
    var orderQtyInput by remember { mutableStateOf("") }
    var visitTypeSelected by remember { mutableStateOf("Routine Checkup") }
    var responseStatusSelected by remember { mutableStateOf("Ready to Buy") }
    var closingRequirementText by remember { mutableStateOf("") }

    // Expense claims states
    var expenseTypeSelected by remember { mutableStateOf("Fuel / Petrol") }
    var expenseAmountInput by remember { mutableStateOf("") }
    var expenseDetailsText by remember { mutableStateOf("") }
    val expenseList = remember { 
        mutableStateListOf(
            CRMExpense(id = "EXP-332", type = "Fuel", amount = 450.0, description = "Drive from Pune center to Viman Nagar", status = "APPROVED", date = "Yesterday"),
            CRMExpense(id = "EXP-314", type = "Toll Bill", amount = 120.0, description = "Express Highway Bypass Toll", status = "PENDING APPROVAL", date = "Today")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Horizontal Secondary Portals tab selectors
        TabRow(
            selectedTabIndex = executiveSubTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(selected = executiveSubTab == 0, onClick = { executiveSubTab = 0 }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(10.dp)) {
                    Icon(Icons.Default.DirectionsRun, contentDescription = "shift", modifier = Modifier.size(20.dp))
                    Text("Shift Logs", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Tab(selected = executiveSubTab == 1, onClick = { executiveSubTab = 1 }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(10.dp)) {
                    Icon(Icons.Default.Map, contentDescription = "route", modifier = Modifier.size(20.dp))
                    Text("Routes Engine", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Tab(selected = executiveSubTab == 2, onClick = { executiveSubTab = 2 }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(10.dp)) {
                    Icon(Icons.Default.BorderColor, contentDescription = "visit", modifier = Modifier.size(20.dp))
                    Text("Log Audit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Tab(selected = executiveSubTab == 3, onClick = { executiveSubTab = 3 }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(10.dp)) {
                    Icon(Icons.Default.Receipt, contentDescription = "expenses", modifier = Modifier.size(20.dp))
                    Text("Expenses", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when (executiveSubTab) {
                0 -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Start/End Shift Panel with animation
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isTracking) Color(0xFF22C55E).copy(alpha = 0.08f) 
                            else MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(
                            1.dp, 
                            if (isTracking) Color(0xFF22C55E).copy(alpha = 0.4f) 
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = if (isTracking) "SHIFT ACTIVE" else "SHIFT STANDBY",
                                        fontWeight = FontWeight.Black,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (isTracking) Color(0xFF22C55E) else MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Vikram (Territory Sales Representative)", 
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(if (isTracking) Color(0xFF22C55E) else Color.LightGray)
                                )
                            }

                            Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

                            // Shift Attendance Checklist
                            Text("Standard Shift Checklist Verification:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Checkbox(checked = selfieSimulatorUploaded, onCheckedChange = { selfieSimulatorUploaded = it })
                                Column {
                                    Text("Selfie Verification Check point", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(if (selfieSimulatorUploaded) "Selfie upload captured successfully ✅" else "Required camera face match simulator", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Checkbox(checked = mockLocationCheck != "Standby", onCheckedChange = { 
                                    mockLocationCheck = if (it) "VERIFIED: Latitude: 18.5204° N, Longitude: 73.8567° E" else "Standby" 
                                })
                                Column {
                                    Text("GPS Dispatch Match Point", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(mockLocationCheck, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Action button trigger
                            Button(
                                onClick = {
                                    if (isTracking) {
                                        viewModel.stopDayTracking()
                                        Toast.makeText(context, "Shift terminated successfully. Syncing statistics remote!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        if (!selfieSimulatorUploaded || mockLocationCheck == "Standby") {
                                            Toast.makeText(context, "Please authenticate selfie checkpoint & GPS match first!", Toast.LENGTH_LONG).show()
                                        } else {
                                            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
                                            viewModel.startDayTracking("Today (${sdf.format(Date())})")
                                            Toast.makeText(context, "Active dispatch tracking initiated!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isTracking) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isTracking) Icons.Default.StopCircle else Icons.Default.PlayCircle,
                                        contentDescription = "start stop shift",
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isTracking) "Terminate Today's Shift Logs" else "Initiate Field Shift Duty",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Target Tracking Gauges
                    Text(
                        text = "Your Assigned Sales KPI Milestones",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Monthly revenue target
                            Text("Goal 1: Monthly Booking Value", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Achieved: ₹98,500", fontSize = 11.sp)
                                Text("Target: ₹150,000", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(Color.LightGray.copy(alpha = 0.3f))) {
                                Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(0.65f).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                            }
                            Text("Performance bonus payout eligible at ₹120,000", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)

                            Divider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))

                            // Visits Goal
                            Text("Goal 2: Territory Customer Audits", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Visits Logged: ${visits.size} shops", fontSize = 11.sp)
                                Text("Target: 15 shops", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(Color.LightGray.copy(alpha = 0.3f))) {
                                val ratio = (visits.size.toFloat() / 15f).coerceAtMost(1.0f)
                                Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(ratio).clip(CircleShape).background(Color(0xFF22C55E)))
                            }
                        }
                    }

                    // Historical movements summary
                    Text("Previous Shift Movement Logs", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    dayTracks.forEach { track ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(track.dateString, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(track.status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (track.status == "Active") Color(0xFF22C55E) else Color.Gray)
                                }
                                Text("Distance Travelers Cover: ${String.format(Locale.US, "%.2f", track.kmTraveled)} km • Audited: ${track.visitedCount} shops", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                1 -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Active GPS tracking coordinates
                    if (isTracking) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Live Transit GPS Telemetry Feed", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Simulated Travel:", fontSize = 12.sp)
                                    Text("${String.format(Locale.US, "%.2f", currentKm)} km", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF22C55E))
                                }
                                Text("Latest coordinate: \"${routePoints.lastOrNull() ?: "Capturing telemetry..."}\" ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Nearest Customer list suggestion
                    Text("Nearest Registered B2B Customers & Route Options", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    
                    if (customers.isEmpty()) {
                        Text("No customers registered in ledger database.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    } else {
                        customers.take(3).forEach { cust ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text(cust.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer)
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("Closest Base", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }

                                    // Nearby mock details
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text("Address: ${cust.address}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text("Estimated Distance: 1.8 KM • Drive details: 4 min away", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        IconButton(onClick = {
                                            Toast.makeText(context, "Redirecting to Android maps link: ${cust.name}", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Icon(Icons.Default.Navigation, contentDescription = "navigate", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Territory list
                    Text("Your Active Territory: Pune West-Zone", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Route Optimizer Details", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Priority list sorted dynamically based on pending credit reviews & last visited indicators.", fontSize = 11.sp)
                            Text("1. Apex Lubricants (High priority - Inactive for 10 days)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("2. Golden Wheels (Medium priority - Pending invoice collection)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                2 -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Log Customer Field Audit & Visit Checks", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                    // Form Fields
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Audited Store:", fontWeight = FontWeight.Bold, modifier = Modifier.width(110.dp))
                        OutlinedTextField(
                            value = selectedCustomerForVisit,
                            onValueChange = { selectedCustomerForVisit = it },
                            placeholder = { Text("e.g. Apex Lubricants") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Visit Purpose:", fontWeight = FontWeight.Bold, modifier = Modifier.width(110.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .clickable { 
                                    // simple toggler
                                    visitTypeSelected = if (visitTypeSelected == "Routine Checkup") "Order Booking" else "Routine Checkup"
                                }
                                .padding(12.dp)
                        ) {
                            Text(visitTypeSelected, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Feedback Status:", fontWeight = FontWeight.Bold, modifier = Modifier.width(110.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .clickable { 
                                    responseStatusSelected = if (responseStatusSelected == "Ready to Buy") "Negotiating" else "Ready to Buy"
                                }
                                .padding(12.dp)
                        ) {
                            Text(responseStatusSelected, fontWeight = FontWeight.Bold)
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Audit & Negotiation Notes:", fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = visitNotesText,
                            onValueChange = { visitNotesText = it },
                            placeholder = { Text("Describe products pitched, competitive dealer rates, and stock feedback...") },
                            modifier = Modifier.fillMaxWidth().height(80.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = visitOrderPlacedChecked, onCheckedChange = { visitOrderPlacedChecked = it })
                        Text("Secure immediate B2B Booking Order during visit?", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    if (visitOrderPlacedChecked) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("Pitched Product:", fontWeight = FontWeight.Bold, modifier = Modifier.width(110.dp))
                            OutlinedTextField(
                                value = interestedProductText,
                                onValueChange = { interestedProductText = it },
                                placeholder = { Text("Engine Max 4T 10W-30") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("Quantity Packs:", fontWeight = FontWeight.Bold, modifier = Modifier.width(110.dp))
                            OutlinedTextField(
                                value = orderQtyInput,
                                onValueChange = { orderQtyInput = it },
                                placeholder = { Text("e.g. 50") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Closing & Delivery Requirements", fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = closingRequirementText,
                            onValueChange = { closingRequirementText = it },
                            placeholder = { Text("Needs promotional sign Board / customized credit approval...") },
                            modifier = Modifier.fillMaxWidth().height(60.dp)
                        )
                    }

                    Button(
                        onClick = {
                            if (selectedCustomerForVisit.trim().isEmpty()) {
                                Toast.makeText(context, "Please select/type customer shop name first!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            val qty = orderQtyInput.trim().toIntOrNull() ?: 0
                            viewModel.logVisit(
                                customerName = selectedCustomerForVisit.trim(),
                                representativeName = viewModel.userDisplayName.value ?: "Staff Representative",
                                visitType = visitTypeSelected,
                                notes = visitNotesText,
                                interestedProduct = interestedProductText,
                                orderQuantity = qty,
                                wasOrderPlaced = visitOrderPlacedChecked,
                                location = "Pune Central Zone Hub",
                                customerResponseStatus = responseStatusSelected,
                                closingRequirements = closingRequirementText
                            )

                            // Clear inputs
                            selectedCustomerForVisit = ""
                            visitNotesText = ""
                            interestedProductText = ""
                            orderQtyInput = ""
                            closingRequirementText = ""
                            visitOrderPlacedChecked = false

                            Toast.makeText(context, "Field Audit registered. Cloud syncing done! ✅", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Record Field Audit & Upload Updates", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                3 -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Sales Rep Operations: Expense Claims", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                    // Submit new claim
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Submit Transit Expense Claim", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("Billing Category:", fontWeight = FontWeight.Bold, modifier = Modifier.width(110.dp))
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                        .clickable { 
                                            expenseTypeSelected = if (expenseTypeSelected == "Fuel / Petrol") "Meals" else "Fuel / Petrol"
                                        }
                                        .padding(10.dp)
                                ) {
                                    Text(expenseTypeSelected, fontWeight = FontWeight.Bold)
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("Amount Claimed:", fontWeight = FontWeight.Bold, modifier = Modifier.width(110.dp))
                                OutlinedTextField(
                                    value = expenseAmountInput,
                                    onValueChange = { expenseAmountInput = it },
                                    placeholder = { Text("₹ Value") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }

                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("Usage Notes:", fontWeight = FontWeight.Bold, modifier = Modifier.width(110.dp))
                                OutlinedTextField(
                                    value = expenseDetailsText,
                                    onValueChange = { expenseDetailsText = it },
                                    placeholder = { Text("Toll, fuel liter metrics, or lunch with client description") },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            // Receipt scan simulator
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { Toast.makeText(context, "Scanned and auto-extracted receipt data!", Toast.LENGTH_SHORT).show() }
                                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = "camera receipt", tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Simulate Camera Receipt Scanner", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = {
                                    val amt = expenseAmountInput.trim().toDoubleOrNull() ?: 0.0
                                    if (amt <= 0.0) {
                                        Toast.makeText(context, "Enter a valid claim amount!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    
                                    val newExp = CRMExpense(
                                        id = "EXP-${100 + Random().nextInt(800)}",
                                        type = expenseTypeSelected,
                                        amount = amt,
                                        description = expenseDetailsText,
                                        status = "PENDING APPROVAL",
                                        date = "Just Now"
                                    )
                                    expenseList.add(0, newExp)
                                    
                                    // Reset fields
                                    expenseAmountInput = ""
                                    expenseDetailsText = ""
                                    Toast.makeText(context, "Claim submitted. Dispatched to owner workflow approval queue!", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Dispatch Expense Approval Request", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    // Historic Expense Claims list
                    Text("Your Claim History Ledger", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    expenseList.forEach { exp ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Claim: ${exp.type}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                if (exp.status == "APPROVED") Color(0xFF22C55E).copy(alpha = 0.12f)
                                                else Color(0xFFF59E0B).copy(alpha = 0.12f)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(exp.status, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (exp.status == "APPROVED") Color(0xFF22C55E) else Color(0xFFF59E0B))
                                    }
                                }
                                Text("Amount claim: ₹${String.format(Locale.US, "%.2f", exp.amount)} • Date: ${exp.date}", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                Text("Details: \"${exp.description}\"", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

data class CRMExpense(
    val id: String,
    val type: String,
    val amount: Double,
    val description: String,
    val status: String,
    val date: String
)
