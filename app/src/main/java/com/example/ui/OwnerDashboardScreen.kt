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
                        Text("Live Telemetry", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Tab(selected = ownerSubTab == 1, onClick = { ownerSubTab = 1 }) {
                        Text("Warehouse Matrix", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Tab(selected = ownerSubTab == 2, onClick = { 
                        ownerSubTab = 2 
                    }) {
                        BadgedBox(
                            badge = {
                                if (pendingApprovals.isNotEmpty()) {
                                    Badge { Text("${pendingApprovals.size}") }
                                }
                            },
                            modifier = Modifier.padding(vertical = 14.dp)
                        ) {
                            Text("Approvals", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                    Tab(selected = ownerSubTab == 3, onClick = { ownerSubTab = 3 }) {
                        Text("Campaign Manager", modifier = Modifier.padding(14.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
                0 -> LazyColumn(
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
                1 -> Column(
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
                2 -> Column(
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
                3 -> Column(
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
