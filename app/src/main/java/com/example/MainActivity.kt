package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    
    private val viewModel: CRMViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainLayout(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainLayout(viewModel: CRMViewModel) {
    val userRole by viewModel.userRole.collectAsStateWithLifecycle()
    val userDisplayName by viewModel.userDisplayName.collectAsStateWithLifecycle()

    if (userRole == null) {
        LoginScreen(viewModel = viewModel)
    } else {
        var selectedTab by remember { mutableIntStateOf(0) }

        // Observe flows reactively from the DB layer
        val products by viewModel.products.collectAsStateWithLifecycle()
        val customers by viewModel.customers.collectAsStateWithLifecycle()
        val visits by viewModel.visits.collectAsStateWithLifecycle()
        val submissions by viewModel.submissions.collectAsStateWithLifecycle()

        val unprocessedCount = submissions.count { !it.isProcessed }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                // Polished Top Status Panel showing the Active Persona with cloud Sync & Log Out actions
                Surface(
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (userRole == "Owner") MaterialTheme.colorScheme.primaryContainer
                                        else Color(0xFF2ECC71).copy(alpha = 0.15f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (userRole == "Owner") Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                    contentDescription = "User role icon",
                                    tint = if (userRole == "Owner") MaterialTheme.colorScheme.onPrimaryContainer else Color(0xFF27AE60),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = userDisplayName ?: "Sales Rep",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (userRole == "Owner") "Corporate Access (Owner)" else "Sales Executive (Worker)",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Top bar Sync & Logout buttons
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.syncWithFirebase() },
                                modifier = Modifier.size(36.dp).testTag("action_sync_top")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = "Trigger Remote Sync",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { viewModel.logoutUser() },
                                modifier = Modifier.size(36.dp).testTag("action_logout_top")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = "Sign out",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 0) Icons.Default.Dashboard else Icons.Outlined.Dashboard,
                                contentDescription = "Dashboard"
                            )
                        },
                        label = { Text("Dashboard") },
                        modifier = Modifier.testTag("tab_dashboard")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 1) Icons.Default.Inventory2 else Icons.Outlined.Inventory2,
                                contentDescription = "Depot"
                            )
                        },
                        label = { Text("Depot") },
                        modifier = Modifier.testTag("tab_depot")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = {
                            Icon(
                                imageVector = if (selectedTab == 2) Icons.Default.PersonSearch else Icons.Outlined.PersonSearch,
                                contentDescription = "CRM Logs"
                            )
                        },
                        label = { Text("Visits & CRM") },
                        modifier = Modifier.testTag("tab_visits")
                    )

                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (unprocessedCount > 0) {
                                        Badge {
                                            Text("$unprocessedCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (selectedTab == 3) Icons.Default.Share else Icons.Outlined.Share,
                                    contentDescription = "Form share portal"
                                )
                            }
                        },
                        label = { Text("Form Portal") },
                        modifier = Modifier.testTag("tab_portal")
                    )
                }
            },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    0 -> {
                        if (userRole == "Owner") {
                            OwnerDashboardScreen(
                                viewModel = viewModel,
                                onNavigateToTab = { selectedTab = it }
                            )
                        } else {
                            DashboardScreen(
                                viewModel = viewModel,
                                products = products,
                                customers = customers,
                                visits = visits,
                                onNavigateToTab = { selectedTab = it }
                            )
                        }
                    }
                    1 -> InventoryScreen(
                        viewModel = viewModel,
                        products = products
                    )
                    2 -> VisitsScreen(
                        viewModel = viewModel,
                        customers = customers,
                        visits = visits,
                        products = products
                    )
                    3 -> FormSharingScreen(
                        viewModel = viewModel,
                        submissions = submissions,
                        products = products
                    )
                }
            }
        }
    }
}
