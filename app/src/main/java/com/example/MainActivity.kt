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
                                        when (userRole) {
                                            "Owner" -> MaterialTheme.colorScheme.primaryContainer
                                            "Customer" -> MaterialTheme.colorScheme.secondaryContainer
                                            else -> Color(0xFF2ECC71).copy(alpha = 0.15f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (userRole) {
                                        "Owner" -> Icons.Default.AdminPanelSettings
                                        "Customer" -> Icons.Default.ShoppingCart
                                        else -> Icons.Default.Person
                                    },
                                    contentDescription = "User role icon",
                                    tint = when (userRole) {
                                        "Owner" -> MaterialTheme.colorScheme.onPrimaryContainer
                                        "Customer" -> MaterialTheme.colorScheme.onSecondaryContainer
                                        else -> Color(0xFF27AE60)
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = userDisplayName ?: "Active User",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = when (userRole) {
                                        "Owner" -> "Corporate Access (Owner)"
                                        "Customer" -> "Customer Portal"
                                        else -> "Sales Executive (Worker)"
                                    },
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
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (userRole) {
                    "Customer" -> {
                        CustomerPortalScreen(viewModel = viewModel)
                    }
                    "Owner" -> {
                        OwnerDashboardScreen(
                            viewModel = viewModel,
                            onNavigateToTab = { }
                        )
                    }
                    else -> {
                        SalesExecutivePortalScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
