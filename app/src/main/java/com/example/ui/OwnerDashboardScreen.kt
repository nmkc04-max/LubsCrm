package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VisitLog
import com.example.data.WorkerDayTrack

data class CustomerTimelineItem(
    val title: String,
    val summary: String,
    val time: String,
    val color: Color
)

fun timelineItemsForCustomer(customer: String): List<CustomerTimelineItem> {
    return if (customer.contains("Apex")) {
        listOf(
            CustomerTimelineItem("B2B Consignment Dispatch", "Load #LOAD-922 shipped: 50 packs RideForce 4T, value ₹16,250", "Today, 11:30 AM", Color(0xFF6C47FF)),
            CustomerTimelineItem("Geotagged CRM Visit log check", "Completed by Vikram (Rep), outcome marked Ready-to-Buy. Follow-up: print branding boards.", "Yesterday, 3:15 PM", Color(0xFF22C55E)),
            CustomerTimelineItem("Credit Invoice Settled", "Paid ₹14,000 against June backlog. Outstanding reduced to ₹2,800 only.", "3 Days ago", Color(0xFFF59E0B)),
            CustomerTimelineItem("Support ticket #CS-991 closed", "Inquired about loyalty point redemption on Platinum. Answered & updated on portal.", "5 Days ago", Color.Gray)
        )
    } else if (customer.contains("Golden")) {
        listOf(
            CustomerTimelineItem("Visits Routine Audit Completed", "Completed routine audit checkup. Outlined trial bearing grease feedback. Negotiating lead acid battery list rates.", "Yesterday, 5:40 PM", Color(0xFF22C55E)),
            CustomerTimelineItem("Payment Handshake collection", "Collected ₹9,500 post delivery verify. Secured OTP key matched.", "2 Days ago", Color(0xFF22C55E)),
            CustomerTimelineItem("Credit Approved for Platform terms", "Owner granted Platinum grade credit terms extension (Limit: ₹1,50,000).", "4 Days ago", Color(0xFF6C47FF)),
            CustomerTimelineItem("Dispatch Gated At Depot", "Load #LOAD-882 exited Vijayawada checkout safely.", "6 Days ago", Color.Gray)
        )
    } else {
        listOf(
            CustomerTimelineItem("General Catalog Discovery query", "Searched complete industrial coolants catalog and marked 5 additive fluids favorite.", "Yesterday, 10:15 AM", Color(0xFF6C47FF)),
            CustomerTimelineItem("New Retail Account Created", "Automated system verified GSTIN credentials. Shifted target tier status to Bronze.", "10 Days ago", Color(0xFF22C55E))
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDashboardScreen(
    viewModel: CRMViewModel,
    onNavigateToTab: (Int) -> Unit
) {
    val context = LocalContext.current
    val syncStatusMessage by viewModel.syncStatusMessage.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val firebaseDbUrl by viewModel.firebaseDbUrl.collectAsState()
    val telemetryList by viewModel.remoteWorkersTelemetry.collectAsState()

    var urlInput by remember { mutableStateOf(firebaseDbUrl) }
    var showConfigDialog by remember { mutableStateOf(false) }

    // Owner Sub-portal Navigation
    var ownerSubTab by remember { mutableIntStateOf(0) } // 0=Team Telemetry, 1=Multi-Warehouse Matrix, 2=Workflows & Approvals, 3=Campaign Rule Engine
    var selected360Customer by remember { mutableStateOf("Apex Spares") }

    // Simulated Warehouse Stock Matrix
    var hydStock by remember { mutableIntStateOf(1420) }
    var vijStock by remember { mutableIntStateOf(850) }
    var vizStock by remember { mutableIntStateOf(410) }

    var transferSourceWarehouse by remember { mutableStateOf("Hyderabad Depot") }
    var transferDestWarehouse by remember { mutableStateOf("Vijayawada Depot") }
    var transferAmtInput by remember { mutableStateOf("") }

    // Active pending workflow approvals state
    val pendingApprovals = remember {
        mutableStateListOf(
            CRMApprovalRequest(id = "APR-912", dealerName = "Apex Lubricants", type = "Credit Limit Upgrade", detail = "Requesting credit upgrade to ₹80,000", value = "₹30,000 Diff"),
            CRMApprovalRequest(id = "APR-905", dealerName = "Golden Wheels Garage", type = "Procurement Discount", detail = "Extra 5% discount requested on Industrial Hydropower bulk barrels", value = "5% Margin")
        )
    }

    // Active campaigns configurations
    var bronzeLoyaltyMultiplier by remember { mutableStateOf("1.0") }
    var silverLoyaltyMultiplier by remember { mutableStateOf("1.2") }
    var goldLoyaltyMultiplier by remember { mutableStateOf("1.5") }
    var platinumLoyaltyMultiplier by remember { mutableStateOf("2.0") }

    LaunchedEffect(firebaseDbUrl) {
        urlInput = firebaseDbUrl
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // Header details
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Asianstar Lubricants HUB",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Anil (Corporate Commander)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row {
                        IconButton(
                            onClick = { showConfigDialog = true },
                            modifier = Modifier.testTag("owner_config_firebase_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Firebase DB settings",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = { viewModel.syncWithFirebase() },
                            modifier = Modifier.testTag("owner_refresh_sync_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh telemetry",
                                tint = if (isSyncing) Color(0xFFE67E22) else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Cloud Status Line Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isSyncing) Color(0xFFE67E22) else Color(0xFF2ECC71))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = syncStatusMessage,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                ScrollableTabRow(
                    selectedTabIndex = ownerSubTab,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[ownerSubTab]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(selected = ownerSubTab == 0, onClick = { ownerSubTab = 0 }) {
                        Text("Enterprise BI Hub", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Tab(selected = ownerSubTab == 1, onClick = { ownerSubTab = 1 }) {
                        Text("Field Tracking", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Tab(selected = ownerSubTab == 2, onClick = { ownerSubTab = 2 }) {
                        Text("Inventory Intel", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Tab(selected = ownerSubTab == 3, onClick = { ownerSubTab = 3 }) {
                        BadgedBox(
                            badge = {
                                if (pendingApprovals.isNotEmpty()) {
                                    Badge { Text("${pendingApprovals.size}") }
                                }
                            },
                            modifier = Modifier.padding(vertical = 14.dp)
                        ) {
                            Text("Workflows", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    Tab(selected = ownerSubTab == 4, onClick = { ownerSubTab = 4 }) {
                        Text("Campaign Rule Engine", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (ownerSubTab) {
                0 -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // --- REVENUE & PORTFOLIO BI INTELLIGENCE SEGMENT ---
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Analytics, contentDescription = "BI Icon", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Enterprise BI Executive Summary Dashboard", fontWeight = FontWeight.Black, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            
                            // 2x3 high-density grid for metrics
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text("Today's Revenue", fontSize = 8.sp, color = Color.Gray)
                                            Text("₹42,500", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF111827))
                                        }
                                    }
                                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text("Monthly Revenue", fontSize = 8.sp, color = Color.Gray)
                                            Text("₹4,85,000", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF6C47FF))
                                        }
                                    }
                                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text("Active Orders", fontSize = 8.sp, color = Color.Gray)
                                            Text("14 Booked", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF22C55E))
                                        }
                                    }
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text("Active Dealers", fontSize = 8.sp, color = Color.Gray)
                                            Text("156 Outlets", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.DarkGray)
                                        }
                                    }
                                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text("Active Field Staff", fontSize = 8.sp, color = Color.Gray)
                                            Text("4 Online", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.DarkGray)
                                        }
                                    }
                                    Card(modifier = Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text("Depot Alerts", fontSize = 8.sp, color = Color.Gray)
                                            Text("2 Warning", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFEF4444))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // --- BUSINESS INTELLIGENCE ANALYTICS CHART BLOCK ---
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Revenue Forecast & Sales Ledger Trends (Canvas)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF111827))
                            
                            // Modern Mini Graphical Canvas
                            Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                                val w = size.width
                                val h = size.height
                                
                                // Draw Gridlines
                                drawLine(color = Color(0xFFE2E8F0), start = androidx.compose.ui.geometry.Offset(0f, h*0.25f), end = androidx.compose.ui.geometry.Offset(w, h*0.25f))
                                drawLine(color = Color(0xFFE2E8F0), start = androidx.compose.ui.geometry.Offset(0f, h*0.5f), end = androidx.compose.ui.geometry.Offset(w, h*0.5f))
                                drawLine(color = Color(0xFFE2E8F0), start = androidx.compose.ui.geometry.Offset(0f, h*0.75f), end = androidx.compose.ui.geometry.Offset(w, h*0.75f))
                                
                                // Draw revenue trend points
                                val points = listOf(0.1f to "Apr", 0.4f to "May", 0.35f to "Jun", 0.70f to "Jul", 0.65f to "Aug", 0.95f to "Sep")
                                val n = points.size
                                val dx = w / (n - 1)
                                
                                val path = androidx.compose.ui.graphics.Path()
                                points.forEachIndexed { idx, (pct, _) ->
                                    val px = idx * dx
                                    val py = h - (pct * h * 0.8f + h * 0.1f)
                                    if (idx == 0) path.moveTo(px, py) else path.lineTo(px, py)
                                    
                                    // Highlight point dot
                                    drawCircle(
                                        color = Color(0xFF6C47FF),
                                        radius = 12f,
                                        center = androidx.compose.ui.geometry.Offset(px, py)
                                    )
                                }
                                drawPath(
                                    path = path,
                                    color = Color(0xFF6C47FF),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
                                )
                            }
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                listOf("Apr: 1.2L", "May: 1.8L", "Jun: 1.6L", "Jul: 3.1L", "Aug: 2.8L", "Sep: 4.8L").forEach { label ->
                                    Text(label, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                }
                            }
                            
                            Divider()
                            
                            // High density summary cards
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("TOP PRODUCTS", fontSize = 8.sp, color = Color.Gray)
                                    Text("• RideForce 4T (62%)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text("• Multi Grease (18%)", fontSize = 9.sp, color = Color.Gray)
                                }
                                Column {
                                    Text("TOP TERRITORIES", fontSize = 8.sp, color = Color.Gray)
                                    Text("• Pune West (₹2.4L)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text("• Kanpor East (₹1.8L)", fontSize = 9.sp, color = Color.Gray)
                                }
                                Column {
                                    Text("SALES TEAM RANK", fontSize = 8.sp, color = Color.Gray)
                                    Text("• Rahul S. (92% quota)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    Text("• Amit P. (84% quota)", fontSize = 9.sp, color = Color.Gray)
                                }
                            }
                        }
                    }

                    // --- 360-DEGREE DIAL-IN CUSTOMER TIMELINE SECTION ---
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.RecentActors, contentDescription = "Actor", tint = Color(0xFF6C47FF), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Interactive Customer 360° Timeline", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF111827))
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF22C55E).copy(alpha = 0.12f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Platinum Tier", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF22C55E))
                                }
                            }

                            // Selection chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Apex Spares", "Golden Wheels", "Auto Mech Emporium").forEach { customer ->
                                    val sel = selected360Customer == customer
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (sel) Color(0xFFECEBFF) else Color(0xFFF3F4F6))
                                            .clickable { selected360Customer = customer }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(customer, fontSize = 9.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, color = if (sel) Color(0xFF6C47FF) else Color.DarkGray)
                                    }
                                }
                            }

                            Divider(color = Color(0xFFF3F4F6))

                            // Customer Details Block
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Customer Profile: $selected360Customer", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF111827))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Lifetime Purchases: ₹1,48,200", fontSize = 9.sp, color = Color.Gray)
                                    Text("Credit Status: Approved Max (A+ Grade)", fontSize = 9.sp, color = Color(0xFF22C55E), fontWeight = FontWeight.Bold)
                                }
                                Text("Authorized Contacts: Sanjay Kumar • Last Interaction: 2 Hours ago via Vikram (Field Rep)", fontSize = 9.sp, color = Color.Gray)
                            }

                            Divider(color = Color(0xFFF3F4F6))

                            Text("Ledger Unified History Timeline:", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color.DarkGray)

                            // Unified vertical timeline entries
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                timelineItemsForCustomer(selected360Customer).forEach { item ->
                                    Row(verticalAlignment = Alignment.Top) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(item.color)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .width(1.5.dp)
                                                    .height(24.dp)
                                                    .background(Color(0xFFE2E8F0))
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text(item.title, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF111827))
                                                Text(item.time, fontSize = 8.sp, color = Color.Gray)
                                            }
                                            Text(item.summary, fontSize = 9.sp, color = Color.DarkGray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Global KPIs counts block
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OwnerKpiCard(
                                title = "Active Reps",
                                value = "${telemetryList.size} Active",
                                icon = Icons.Default.DirectionsRun,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.weight(1f)
                            )
                            val totalRepDistance = telemetryList.sumOf { w -> w.dayTracks.sumOf { t -> t.kmTraveled } }
                            OwnerKpiCard(
                                title = "Distance Tracked",
                                value = String.format(Locale.US, "%.1f km", totalRepDistance),
                                icon = Icons.Default.DirectionsCar,
                                color = Color(0xFF2ECC71),
                                modifier = Modifier.weight(1.5f)
                            )
                        }
                    }

                    // Worker telemetry files lists
                    item {
                        Text(
                            text = "Live Dispatch Team Telemetry Logs",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    if (telemetryList.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.CloudQueue, contentDescription = "none", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(36.dp))
                                    Text("No cloud rep details loaded yet.", fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
                                }
                            }
                        }
                    } else {
                        items(telemetryList) { worker ->
                            WorkerTelemetryRowCard(worker = worker)
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
                    Text("Physical Warehouses Stock Status Matrix", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                    // Display Matrix
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        WarehouseStockCard("Hyderabad Depot", "$hydStock Packs", Color(0xFF22C55E), modifier = Modifier.weight(1f))
                        WarehouseStockCard("Vijayawada Depot", "$vijStock Packs", Color(0xFFF59E0B), modifier = Modifier.weight(1f))
                        WarehouseStockCard("Visakhapatnam Depot", "$vizStock Packs", Color(0xFFEF4444), modifier = Modifier.weight(1f))
                    }

                    // Low stock checker alert block
                    if (vizStock < 500 || vijStock < 500) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f))
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = "alert", tint = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Depot Replenishment Triggered", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                                    Text("Vijayawada/Visakhapatnam depot inventories are low (threshold < 1,000 packs). Recommend stock transfers from central Hyderabad Depot.", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    // --- ENTERPRISE INVENTORY INTELLIGENCE PANEL ---
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Analytics, contentDescription = "Intel", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ERP Automated Inventory Intelligence Engine", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            }

                            Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))

                            // Dead Stock alert
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.RemoveCircleOutline, contentDescription = "dead stock", tint = Color(0xFFEF4444), modifier = Modifier.size(14.dp).padding(top = 1.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Dead Stock Detection (Unsold > 60 days):", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                                    Text("Coolant Concentrate Pink sitting inactive for 68 days in Vijayawada Depot. Action: Auto-triggered 15% discount bundle in Retailer app.", fontSize = 9.sp, color = Color.DarkGray)
                                }
                            }

                            // Fast vs Slow Moving categorization
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.TrendingUp, contentDescription = "fast moving", tint = Color(0xFF22C55E), modifier = Modifier.size(14.dp).padding(top = 1.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Fast-Moving Leader (Turnover: 4.8x/mo):", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF22C55E))
                                    Text("RideForce 4T Synth Sport 10W-30 is high velocity. Core recommendation: Maintain minimum safety stock of 500 packs.", fontSize = 9.sp, color = Color.DarkGray)
                                }
                            }

                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.TrendingDown, contentDescription = "slow moving", tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp).padding(top = 1.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Slow-Moving Leader (Turnover: 0.3x/mo):", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                                    Text("Multi-purpose Lithium Bearing Grease. Turnover rates are sluggish. Recommend hold on further production orders.", fontSize = 9.sp, color = Color.DarkGray)
                                }
                            }

                            // Replenishment Forecasting
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.Autorenew, contentDescription = "forecast", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp).padding(top = 1.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Machine Learning Replenishment Forecast (Next 30 Days):", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                                    Text("Predicted customer demand spikes by +24% in Visakhapatnam region due to upcoming regional automobile show. Automated system recommends dispatching extra +450 packs.", fontSize = 9.sp, color = Color.DarkGray)
                                }
                            }
                        }
                    }

                    // Inter-depot stock transit form
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                         border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Inter-Warehouse Stock Balance Transfer", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("From Depot:", fontWeight = FontWeight.Bold, modifier = Modifier.width(110.dp))
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .clickable { 
                                            transferSourceWarehouse = if (transferSourceWarehouse == "Hyderabad Depot") "Vijayawada Depot" else "Hyderabad Depot"
                                        }
                                        .padding(10.dp)
                                ) {
                                    Text(transferSourceWarehouse, fontWeight = FontWeight.Bold)
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("To Depot:", fontWeight = FontWeight.Bold, modifier = Modifier.width(110.dp))
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .clickable { 
                                            transferDestWarehouse = if (transferDestWarehouse == "Vijayawada Depot") "Visakhapatnam Depot" else "Vijayawada Depot"
                                        }
                                        .padding(10.dp)
                                ) {
                                    Text(transferDestWarehouse, fontWeight = FontWeight.Bold)
                                }
                            }

                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("Transfr Qty:", fontWeight = FontWeight.Bold, modifier = Modifier.width(110.dp))
                                OutlinedTextField(
                                    value = transferAmtInput,
                                    onValueChange = { transferAmtInput = it },
                                    placeholder = { Text("e.g. 200 Packs") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }

                            Button(
                                onClick = {
                                    val transAmt = transferAmtInput.toIntOrNull() ?: 0
                                    if (transAmt <= 0) {
                                        Toast.makeText(context, "Enter a valid positive stock value!", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }

                                    // simple simulation logic
                                    if (transferSourceWarehouse == "Hyderabad Depot") {
                                        if (hydStock < transAmt) {
                                            Toast.makeText(context, "Insufficient stock in source Hyderabad Depot!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            hydStock -= transAmt
                                            if (transferDestWarehouse == "Vijayawada Depot") vijStock += transAmt else vizStock += transAmt
                                            Toast.makeText(context, "Successfully balanced $transAmt packs inventory transfer!", Toast.LENGTH_LONG).show()
                                            transferAmtInput = ""
                                        }
                                    } else {
                                        if (vijStock < transAmt) {
                                            Toast.makeText(context, "Source Vijayawada Depot is low!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            vijStock -= transAmt
                                            if (transferDestWarehouse == "Visakhapatnam Depot") vizStock += transAmt else hydStock += transAmt
                                            Toast.makeText(context, "Successfully balanced $transAmt packs inventory transfer!", Toast.LENGTH_LONG).show()
                                            transferAmtInput = ""
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Process Logistical Stock Transfer", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
                3 -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Pending Corporate Workflow Approvals Queue", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                    if (pendingApprovals.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "done", tint = Color(0xFF2ECC71), modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Excellent! All dealer approval items cleared.", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        pendingApprovals.forEach { apr ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text(apr.dealerName, fontWeight = FontWeight.Black, fontSize = 13.sp)
                                            Text(apr.type, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        }
                                        Text(apr.value, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFE67E22))
                                    }
                                    Text(text = apr.detail, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        TextButton(onClick = { 
                                            pendingApprovals.remove(apr)
                                            Toast.makeText(context, "Workflow request declined.", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Text("Decline", color = Color(0xFFE74C3C))
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(onClick = { 
                                            pendingApprovals.remove(apr)
                                            Toast.makeText(context, "Approved! Simulating OTP dispatch track code to representative's terminal.", Toast.LENGTH_LONG).show()
                                        }) {
                                            Text("Approve & Dispatch OTP")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Direct OTP dispatcher generator visual test
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Simulated Logistics Dispatch OTP Key", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            Text("Generating secure dispatch key for active deliveries: \"AS-OTP-99214\" (Provide key to cargo representative at depot gated checkout for load authentication)", fontSize = 10.sp)
                        }
                    }
                }
                4 -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Campaign Manager: Tailor Distributor Loyalty Terms", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                         border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Set Dealer Points Multipliers per Tier", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("Bronze Multiplier:", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = bronzeLoyaltyMultiplier,
                                    onValueChange = { bronzeLoyaltyMultiplier = it },
                                    modifier = Modifier.width(80.dp),
                                    singleLine = true
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("Silver Multiplier:", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = silverLoyaltyMultiplier,
                                    onValueChange = { silverLoyaltyMultiplier = it },
                                    modifier = Modifier.width(80.dp),
                                    singleLine = true
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("Gold Multiplier:", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = goldLoyaltyMultiplier,
                                    onValueChange = { goldLoyaltyMultiplier = it },
                                    modifier = Modifier.width(80.dp),
                                    singleLine = true
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text("Platinum Multiplier:", modifier = Modifier.weight(1f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                OutlinedTextField(
                                    value = platinumLoyaltyMultiplier,
                                    onValueChange = { platinumLoyaltyMultiplier = it },
                                    modifier = Modifier.width(80.dp),
                                    singleLine = true
                                )
                            }

                            Button(
                                onClick = {
                                    Toast.makeText(context, "Campaign settings updated! Points multipliers are active and applied online.", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Update Scheme Rule Parameters")
                            }
                        }
                    }

                    // Special promo config
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Active Coupon Active Checker", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("1. [MONSOON5] - Giving 5% off on orders. Status: ACTIVE", fontSize = 11.sp)
                            Text("2. [ASIANSTAR20] - Off 20% on selected hydraulic fluid drums. Status: ACTIVE", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Settings dialog configuration details overlay
        if (showConfigDialog) {
            AlertDialog(
                onDismissRequest = { showConfigDialog = false },
                confirmButton = {
                    Button(onClick = {
                        viewModel.saveFirebaseConfig(urlInput)
                        showConfigDialog = false
                    }) { Text("Update URL") }
                },
                dismissButton = {
                    TextButton(onClick = { showConfigDialog = false }) { Text("Dismiss") }
                },
                title = { Text("Database Config Settings") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Connect Firebase Realtime Database Rest Endpoint:")
                        OutlinedTextField(
                            value = urlInput,
                            onValueChange = { urlInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            )
        }
    }
}

data class CRMApprovalRequest(
    val id: String,
    val dealerName: String,
    val type: String,
    val detail: String,
    val value: String
)

@Composable
fun OwnerKpiCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(96.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(icon, contentDescription = "icon", tint = color, modifier = Modifier.size(16.dp))
            }
            Text(value, fontWeight = FontWeight.Black, fontSize = 16.sp)
        }
    }
}

@Composable
fun WarehouseStockCard(
    depotName: String,
    packsCount: String,
    indicatorColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(110.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(10.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(indicatorColor))
                Spacer(modifier = Modifier.width(6.dp))
                Text(depotName, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(packsCount, fontWeight = FontWeight.Black, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
            Text("In Stock Status", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun WorkerTelemetryRowCard(worker: RemoteWorkerTelemetry) {
    var expanded by remember { mutableStateOf(false) }
    val hasActiveShift = worker.dayTracks.any { it.status == "Active" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (hasActiveShift) Color(0xFF2ECC71).copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(worker.name.firstOrNull()?.toString() ?: "W", fontWeight = FontWeight.Black, color = if (hasActiveShift) Color(0xFF27AE60) else MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(worker.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(if (hasActiveShift) "Live Shift Tracker: ACTIVE" else "Standby", fontSize = 10.sp, color = if (hasActiveShift) Color(0xFF27AE60) else Color.Gray)
                }
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = "exp")
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    
                    Text("GPS Route Replay History Node Points:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    if (worker.dayTracks.isEmpty()) {
                        Text("No movement breadcrumbs recorded.", fontSize = 11.sp)
                    } else {
                        worker.dayTracks.forEach { track ->
                            Text("• Shift cover: ${track.dateString} - ${String.format(Locale.US, "%.1f", track.kmTraveled)} KM traveled (${track.routePointsString.split(";").size} locations checked)", fontSize = 10.sp)
                        }
                    }

                    Text("Audited Store Audits Ledger:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    if (worker.visits.isEmpty()) {
                        Text("No customer visit logged.", fontSize = 11.sp)
                    } else {
                        worker.visits.take(3).forEach { vs ->
                            Text("✔ ${vs.customerName} - Pitch Product: ${vs.interestedProduct} (${vs.visitType})", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}
