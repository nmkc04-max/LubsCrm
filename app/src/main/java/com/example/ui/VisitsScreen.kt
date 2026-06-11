package com.example.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.CustomerContact
import com.example.data.LubricantProduct
import com.example.data.VisitLog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitsScreen(
    viewModel: CRMViewModel,
    customers: List<CustomerContact>,
    visits: List<VisitLog>,
    products: List<LubricantProduct>
) {
    var showsAddVisitDialog by remember { mutableStateOf(false) }
    var showsAddCustomerDialog by remember { mutableStateOf(false) }
    var activeSubTab by remember { mutableStateOf("Visits Feed") } // "Visits Feed", "Partners CRM", "GPS Tracker"
    
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("visits_screen_root"),
        floatingActionButton = {
            if (activeSubTab == "Visits Feed") {
                FloatingActionButton(
                    onClick = {
                        if (customers.isEmpty()) {
                            Toast.makeText(context, "Please register at least one Customer Partner first!", Toast.LENGTH_LONG).show()
                            showsAddCustomerDialog = true
                        } else {
                            showsAddVisitDialog = true
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.background,
                    modifier = Modifier.testTag("log_visit_fab")
                ) {
                    Icon(Icons.Default.DirectionsRun, contentDescription = "Log Visit")
                }
            } else if (activeSubTab == "Partners CRM") {
                FloatingActionButton(
                    onClick = { showsAddCustomerDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.background,
                    modifier = Modifier.testTag("add_customer_fab")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Add Customer")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "Workers CRM Hub",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Track representative site visits and manage dealer/mechanic partners",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sub tabs switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                listOf("Visits Feed", "Partners CRM", "GPS Tracker").forEach { subTab ->
                    val isSelected = subTab == activeSubTab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else Color.Transparent
                            )
                            .clickable { activeSubTab = subTab }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = subTab,
                            color = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sub Tab Selector Content
            when (activeSubTab) {
                "Visits Feed" -> {
                    if (visits.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.EventNote,
                                    contentDescription = "Empty Visits",
                                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "No visits logged yet. Tap '+' to create first log.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(visits) { visit ->
                                VisitLogFeedItem(visit = visit, onDelete = { viewModel.deleteVisit(visit.id) })
                            }
                        }
                    }
                }
                "Partners CRM" -> {
                    if (customers.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.PermContactCalendar,
                                    contentDescription = "Empty Partners",
                                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "LubeCRM Directory is empty.\nAdd your mechanics or shop connections!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(customers) { contact ->
                                CustomerContactItem(
                                    contact = contact,
                                    onDelete = { viewModel.deleteCustomer(contact.id) }
                                )
                            }
                        }
                    }
                }
                "GPS Tracker" -> {
                    Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                        GPSTrackerDashboard(viewModel = viewModel)
                    }
                }
            }
        }
    }

    // ========== Dialog: Log Visit ==========
    if (showsAddVisitDialog) {
        AddVisitLogDialog(
            customers = customers,
            products = products,
            onDismiss = { showsAddVisitDialog = false },
            onSave = { custName, rep, type, memo, prod, qty, ordered, location, status, closing ->
                viewModel.logVisit(custName, rep, type, memo, prod, qty, ordered, location, status, closing)
                showsAddVisitDialog = false
            }
        )
    }

    // ========== Dialog: Add Customer Partner ==========
    if (showsAddCustomerDialog) {
        AddCustomerPartnerDialog(
            onDismiss = { showsAddCustomerDialog = false },
            onSave = { title, person, tel, type, loc, link ->
                viewModel.addCustomer(title, person, tel, type, loc, link)
                showsAddCustomerDialog = false
            }
        )
    }
}

@Composable
fun VisitLogFeedItem(
    visit: VisitLog,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("visit_card_${visit.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = visit.customerName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Rep: ${visit.representativeName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Visit Note",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = visit.visitType,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }

                val statusColor = when (visit.customerResponseStatus.trim()) {
                    "Closed", "Ready to Buy", "Closed & Signed" -> Color(0xFF2ECC71)
                    "Pending" -> Color(0xFFE67E22)
                    "Follow-up Required", "Needs Follow-up" -> Color(0xFF9B59B6)
                    "Negotiating" -> Color(0xFFD4AC0D)
                    "Warm Lead" -> Color(0xFF3498DB)
                    "Rejected" -> Color(0xFFE74C3C)
                    else -> Color(0xFF95A5A6)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = visit.customerResponseStatus,
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Meeting Report:",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = visit.notes,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (visit.closingRequirements.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE67E22).copy(alpha = 0.08f))
                        .border(1.dp, Color(0xFFE67E22).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = "Target to close",
                        tint = Color(0xFFE67E22),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "WHAT THEY NEED TO CLOSE THIS PARTNER:",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE67E22),
                            fontSize = 9.sp
                        )
                        Text(
                            text = visit.closingRequirements,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Color(0xFFE74C3C),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = visit.simulatedLocation,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                if (visit.wasOrderPlaced && visit.orderQuantity > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF2ECC71).copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Order Confirmed",
                            tint = Color(0xFF2ECC71),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Order: ${visit.orderQuantity} units (${visit.interestedProduct})",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2ECC71),
                            fontSize = 11.sp
                        )
                    }
                } else {
                    Text(
                        text = "No order taken",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerContactItem(
    contact: CustomerContact,
    onDelete: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth().testTag("customer_card_${contact.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            when (contact.type) {
                                "Mechanic Garage" -> Color(0xFFE67E22)
                                "Retailer Dealer" -> Color(0xFF3498DB)
                                "Distributor Shop" -> Color(0xFF9B59B6)
                                else -> Color(0xFF2ECC71)
                            }.copy(alpha = 0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (contact.type) {
                            "Mechanic Garage" -> Icons.Default.Build
                            "Retailer Dealer" -> Icons.Default.Storefront
                            "Distributor Shop" -> Icons.Default.Business
                            else -> Icons.Default.AccountCircle
                        },
                        contentDescription = contact.type,
                        tint = when (contact.type) {
                            "Mechanic Garage" -> Color(0xFFE67E22)
                            "Retailer Dealer" -> Color(0xFF3498DB)
                            "Distributor Shop" -> Color(0xFF9B59B6)
                            else -> Color(0xFF2ECC71)
                        }
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = contact.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Contact: ${contact.contactPerson} • ${contact.type}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                IconButton(
                    onClick = {
                        Toast.makeText(context, "Initiating log call to ${contact.phone}", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Phone,
                        contentDescription = "Call Contact",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete CRM item",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Address",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = contact.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                if (contact.shopLocationLink.isNotEmpty()) {
                    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                    TextButton(
                        onClick = {
                            try {
                                uriHandler.openUri(contact.shopLocationLink)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Opening Map Link: ${contact.shopLocationLink}", Toast.LENGTH_LONG).show()
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("shop_maps_btn_${contact.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Maps logo",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Show Map", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ========== Dialog: Log Site Visit ==========
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVisitLogDialog(
    customers: List<CustomerContact>,
    products: List<LubricantProduct>,
    onDismiss: () -> Unit,
    onSave: (
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
    ) -> Unit
) {
    var representativeName by remember { mutableStateOf("Raj Khurana") }
    var selectedCustomerName by remember { mutableStateOf(customers.firstOrNull()?.name ?: "") }
    var visitType by remember { mutableStateOf("Routine Service") }
    var noteText by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf(products.firstOrNull()?.name ?: "") }
    var orderQuantityText by remember { mutableStateOf("0") }
    var wasOrderPlaced by remember { mutableStateOf(false) }
    var simulatedLocation by remember { mutableStateOf("GPS: 28.5355° N, 77.3910° E (Noida, India)") }
    
    var customerResponseStatus by remember { mutableStateOf("Pending") }
    var closingRequirements by remember { mutableStateOf("") }

    var expandedCustDropdown by remember { mutableStateOf(false) }
    var expandedProductDropdown by remember { mutableStateOf(false) }

    val visitTypesList = listOf("Routine Service", "Lead Follow-up", "Order Booking", "Mechanic Technical Session", "Payment Collection")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("add_visit_dialog"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Log Worker Visit",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Representative Name Input
                item {
                    OutlinedTextField(
                        value = representativeName,
                        onValueChange = { representativeName = it },
                        label = { Text("Worker / Representative Name") },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().testTag("visit_rep_input")
                    )
                }

                // Select Customer Partner Dropdown
                item {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = selectedCustomerName,
                            onValueChange = {},
                            label = { Text("Select Visited Partner") },
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { expandedCustDropdown = !expandedCustDropdown }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Partners")
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        DropdownMenu(
                            expanded = expandedCustDropdown,
                            onDismissRequest = { expandedCustDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            customers.forEach { cust ->
                                DropdownMenuItem(
                                    text = { Text("${cust.name} (${cust.type})") },
                                    onClick = {
                                        selectedCustomerName = cust.name
                                        expandedCustDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Visit Type Selection (with scrollable chips)
                item {
                    Text(
                        text = "Meeting Purpose:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        visitTypesList.take(3).forEach { vt ->
                            val isSelected = vt == visitType
                            FilterChip(
                                selected = isSelected,
                                onClick = { visitType = vt },
                                label = { Text(vt, fontSize = 11.sp) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        visitTypesList.drop(3).forEach { vt ->
                            val isSelected = vt == visitType
                            FilterChip(
                                selected = isSelected,
                                onClick = { visitType = vt },
                                label = { Text(vt, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Meeting details
                item {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Meeting Notes (e.g., feedback or discussion)") },
                        placeholder = { Text("Details of discussion with mechanic or distributor...") },
                        maxLines = 4,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("visit_notes_input")
                    )
                }

                // Customer Response Status Selector
                item {
                    Text(
                        text = "Customer Response Status:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    val responseStatuses = listOf("Pending", "Closed", "Follow-up Required", "Negotiating", "Rejected")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        responseStatuses.take(3).forEach { status ->
                            val isSelected = status == customerResponseStatus
                            FilterChip(
                                selected = isSelected,
                                onClick = { customerResponseStatus = status },
                                label = { Text(status, fontSize = 11.sp) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        responseStatuses.drop(3).forEach { status ->
                            val isSelected = status == customerResponseStatus
                            FilterChip(
                                selected = isSelected,
                                onClick = { customerResponseStatus = status },
                                label = { Text(status, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Closing Requirements field
                item {
                    OutlinedTextField(
                        value = closingRequirements,
                        onValueChange = { closingRequirements = it },
                        label = { Text("What they need to close the customer?") },
                        placeholder = { Text("e.g. 5% extra discount, co-branded signboard, free grease sample...") },
                        maxLines = 2,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("visit_closing_input")
                    )
                }

                // GPS Location Capture Simulation
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "GPS",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Site Stamp:",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = simulatedLocation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = {
                                val lat = String.format("%.4f", 12.9716 + (Math.random() - 0.5) * 10)
                                val lng = String.format("%.4f", 77.5946 + (Math.random() - 0.5) * 10)
                                simulatedLocation = "GPS: $lat° N, $lng° E (Locally Stamp-Tracked)"
                            }
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "re-fetch GPS", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                // Order Placement details
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = wasOrderPlaced,
                            onCheckedChange = { wasOrderPlaced = it }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Did they place a Booking Order?",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // If ordered is true
                if (wasOrderPlaced) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Order Specifications",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Select product
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = selectedProduct,
                                    onValueChange = {},
                                    label = { Text("Product Ordered") },
                                    readOnly = true,
                                    trailingIcon = {
                                        IconButton(onClick = { expandedProductDropdown = !expandedProductDropdown }) {
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Products")
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                DropdownMenu(
                                    expanded = expandedProductDropdown,
                                    onDismissRequest = { expandedProductDropdown = false },
                                    modifier = Modifier.fillMaxWidth(0.85f)
                                ) {
                                    products.forEach { prod ->
                                        DropdownMenuItem(
                                            text = { Text("${prod.name} (Avail: ${prod.availableStock})") },
                                            onClick = {
                                                selectedProduct = prod.name
                                                expandedProductDropdown = false
                                            }
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = orderQuantityText,
                                onValueChange = { orderQuantityText = it },
                                label = { Text("Ordered Quantity (Units)") },
                                shape = RoundedCornerShape(8.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().testTag("visit_order_qty_input")
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Discard")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (selectedCustomerName.isNotEmpty()) {
                                    val qty = if (wasOrderPlaced) orderQuantityText.toIntOrNull() ?: 0 else 0
                                    onSave(
                                        selectedCustomerName,
                                        representativeName,
                                        visitType,
                                        noteText,
                                        if (wasOrderPlaced) selectedProduct else "",
                                        qty,
                                        wasOrderPlaced,
                                        simulatedLocation,
                                        customerResponseStatus,
                                        closingRequirements
                                    )
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("submit_visit_button")
                        ) {
                            Text("Log Visit Now")
                        }
                    }
                }
            }
        }
    }
}

// ========== Dialog: Add Customer Partner / Mechanic CRM ==========
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomerPartnerDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, contactPerson: String, phone: String, type: String, address: String, shopLocationLink: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var contactPerson by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("Mechanic Garage") }
    var address by remember { mutableStateOf("") }
    var shopLocationLink by remember { mutableStateOf("") }

    val userTypes = listOf("Mechanic Garage", "Retailer Dealer", "Distributor Shop", "Fleet Account")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("add_customer_dialog"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Add Customer Partner",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Shop / Garage Name (e.g. Apex Garage)") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("cust_name_input")
                )

                OutlinedTextField(
                    value = contactPerson,
                    onValueChange = { contactPerson = it },
                    label = { Text("Contact Person Name") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Partner CRM Classification:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    userTypes.take(2).forEach { t ->
                        val isSelected = t == selectedType
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedType = t },
                            label = { Text(t, fontSize = 11.sp) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    userTypes.drop(2).forEach { t ->
                        val isSelected = t == selectedType
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedType = t },
                            label = { Text(t, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Physical Working Address") },
                    maxLines = 2,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = shopLocationLink,
                        onValueChange = { shopLocationLink = it },
                        label = { Text("Shop Location Maps URL") },
                        placeholder = { Text("e.g. https://maps.google.com/?q=...") },
                        maxLines = 1,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("cust_location_link_input")
                    )
                    
                    TextButton(
                        onClick = {
                            if (name.isNotEmpty()) {
                                shopLocationLink = "https://maps.google.com/?q=${name.replace(" ", "+")}"
                            } else {
                                shopLocationLink = "https://maps.google.com/?q=Custom+Shop+Location"
                            }
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Auto Link", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotEmpty() && phone.isNotEmpty()) {
                                onSave(name, contactPerson, phone, selectedType, address, shopLocationLink)
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("submit_customer_button")
                    ) {
                        Text("Add Account")
                    }
                }
            }
        }
    }
}

@Composable
fun GPSTrackerDashboard(viewModel: CRMViewModel) {
    val context = LocalContext.current
    val isTracking by viewModel.isTrackingActive.collectAsState()
    val currentKm by viewModel.currentKm.collectAsState()
    val currentRoutePoints by viewModel.currentRoutePoints.collectAsState()
    val dayTracks by viewModel.dayTracks.collectAsState()

    var hasLocationPermission by remember {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                                 permissions.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false)
        if (hasLocationPermission) {
            Toast.makeText(context, "Location permission granted! Live tracking enabled.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Location permission denied. Running in Offline Simulator mode.", Toast.LENGTH_LONG).show()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("gps_tracker_dashboard"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!hasLocationPermission) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp).testTag("gps_permission_request_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Location Access Off",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "GPS Tracking Approval Required",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "To monitor day-to-day route execution, transparently verify customer shop visits, and generate accurate mileage (KM) logs for travel allowances, please grant GPS location permissions.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("approve_gps_btn")
                        ) {
                            Text("Grant GPS Permission", color = Color.White)
                        }
                    }
                }
            }
        }

        // Live Tracker controls card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isTracking) Icons.Default.LocationOn else Icons.Default.LocationOff,
                                contentDescription = "Active Status",
                                tint = if (isTracking) Color(0xFF2ECC71) else MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTracking) "Live Tracking: ON" else "Live Tracking: STANDBY",
                                fontWeight = FontWeight.Bold,
                                color = if (isTracking) Color(0xFF2ECC71) else MaterialTheme.colorScheme.outline,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        
                        if (isTracking) {
                            // Blinking recording dot
                            var blinkVisible by remember { mutableStateOf(true) }
                            LaunchedEffect(key1 = Unit) {
                                while (true) {
                                    blinkVisible = !blinkVisible
                                    kotlinx.coroutines.delay(1000)
                                }
                            }
                            if (blinkVisible) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color.Red)
                                        .testTag("blinking_dot")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "DISTANCE",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = String.format(java.util.Locale.US, "%.1f km", currentKm),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Divider(
                            modifier = Modifier
                                .height(50.dp)
                                .width(1.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "PROVIDER",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = if (hasLocationPermission) "GPS (Core)" else "Sim (Offline)",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (hasLocationPermission) Color(0xFF3498DB) else Color(0xFFE67E22)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (!isTracking) {
                        Button(
                            onClick = {
                                val sdf = java.text.SimpleDateFormat("EEEE, dd MMM yyyy", java.util.Locale.US)
                                val dateStr = sdf.format(java.util.Date())
                                viewModel.startDayTracking(dateStr)
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("gps_start_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71))
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Work Day Tracking", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.stopDayTracking() },
                            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("gps_stop_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("End Work Day & Save Log", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Active path trace breadcrumbs list
        if (isTracking && currentRoutePoints.isNotEmpty()) {
            item {
                Text(
                    text = "Live Breadcrumb Traces Captured:",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        .padding(12.dp)
                ) {
                    currentRoutePoints.takeLast(4).reversed().forEachIndexed { index, point ->
                        val parts = point.split(",")
                        if (parts.size >= 3) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (index == 0) Icons.Default.LocationOn else Icons.Default.DirectionsCar,
                                    contentDescription = "Breadcrumb",
                                    tint = if (index == 0) Color(0xFF2ECC71) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Lat: ${parts[0]}°, Lon: ${parts[1]}°",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Captured at ${parts[2]}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Supervisor monitoring panel: Day-to-Day Log History list
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Employer Tracking History Monitor",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Badge(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text("${dayTracks.size} Logs", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                }
            }
        }

        if (dayTracks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No shifts recorded yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            items(dayTracks) { track ->
                var expandedRoute by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("day_track_card_${track.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = track.dateString,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                val durationText = if (track.endTime > 0) {
                                    val durationSecs = (track.endTime - track.startTime) / 1000
                                    val hours = durationSecs / 3600
                                    val minutes = (durationSecs % 3600) / 60
                                    "${hours}h ${minutes}m Duration"
                                } else {
                                    val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US)
                                    "Started at ${sdf.format(java.util.Date(track.startTime))} (Active)"
                                }
                                Text(
                                    text = durationText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (track.status == "Active") Color(0xFF2ECC71).copy(alpha = 0.15f)
                                            else MaterialTheme.colorScheme.secondaryContainer
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = track.status.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (track.status == "Active") Color(0xFF2ECC71) else MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { viewModel.deleteDayTrack(track.id) },
                                    modifier = Modifier.size(24.dp).testTag("delete_day_track_${track.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete tracking log",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = "Drive distance",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format(java.util.Locale.US, "%.1f km traveled", track.kmTraveled),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Contacts,
                                    contentDescription = "Client Checked",
                                    tint = Color(0xFFD4AC0D),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${track.visitedCount} partners verified",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Trace coordinate detail toggle
                        if (track.routePointsString.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { expandedRoute = !expandedRoute },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (expandedRoute) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Expand",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (expandedRoute) "Hide GPS Breadcrumb Coordinates" else "Show Audit Route Breadcrumbs",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (expandedRoute) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                        .padding(10.dp)
                                ) {
                                    val points = track.routePointsString.split(";")
                                    points.forEachIndexed { idx, p ->
                                        val components = p.split(",")
                                        if (components.size >= 3) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "Node ${idx + 1}: ${components[0]}, ${components[1]}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontSize = 10.sp
                                                )
                                                Text(
                                                    text = components[2],
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
