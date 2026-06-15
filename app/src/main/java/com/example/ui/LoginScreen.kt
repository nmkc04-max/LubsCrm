package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(viewModel: CRMViewModel) {
    val context = LocalContext.current
    
    // selectedPortal options: "Customer", "Worker", "Owner"
    var selectedPortal by remember { mutableStateOf("Customer") } 
    
    var emailInput by remember { mutableStateOf("dealer@asianstar.com") }
    var passwordInput by remember { mutableStateOf("dealer77") }
    var showPassword by remember { mutableStateOf(false) }

    // Synchronize inputs when toggling portals for convenient testing
    LaunchedEffect(selectedPortal) {
        when (selectedPortal) {
            "Customer" -> {
                emailInput = "dealer@asianstar.com"
                passwordInput = "dealer77"
            }
            "Worker" -> {
                emailInput = "worker@lubecrm.com"
                passwordInput = "sales123"
            }
            "Owner" -> {
                emailInput = "owner@lubecrm.com"
                passwordInput = "admin123"
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Asianstar Brand Emblem
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ElectricBolt,
                    contentDescription = "Asianstar Logo Emblem",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(52.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Asianstar Lubricants",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "B2B Procurement, Field Sales SFA & ERP System",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Three-Way Portal Selector
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Customer
                    Box(
                        modifier = Modifier
                            .weight(1.5f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedPortal == "Customer") MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { selectedPortal = "Customer" }
                            .padding(vertical = 10.dp)
                            .testTag("customer_login_tab"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Customer",
                                tint = if (selectedPortal == "Customer") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Customer Portal",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (selectedPortal == "Customer") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Worker / Executive
                    Box(
                        modifier = Modifier
                            .weight(1.5f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedPortal == "Worker") MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { selectedPortal = "Worker" }
                            .padding(vertical = 10.dp)
                            .testTag("worker_login_tab"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.DirectionsRun,
                                contentDescription = "Worker",
                                tint = if (selectedPortal == "Worker") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Field Sales",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (selectedPortal == "Worker") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Owner / Corporate
                    Box(
                        modifier = Modifier
                            .weight(1.5f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedPortal == "Owner") MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { selectedPortal = "Owner" }
                            .padding(vertical = 10.dp)
                            .testTag("owner_login_tab"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Owner",
                                tint = if (selectedPortal == "Owner") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Corporate ERP",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (selectedPortal == "Owner") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Authentication core panel Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = when (selectedPortal) {
                            "Customer" -> "Customer Portal Access"
                            "Worker" -> "SFA Field Agent Authorization"
                            else -> "Owner Corporate Dashboard Access"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Email / ID Form Input
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Database ID / Registered Email") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("login_email"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Password Form Input
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Portal Security Key Code") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Show password"
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("login_password"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Automated testing/debugging quick-fill button
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable {
                                    when (selectedPortal) {
                                        "Customer" -> {
                                            emailInput = "dealer@asianstar.com"
                                            passwordInput = "dealer77"
                                        }
                                        "Worker" -> {
                                            emailInput = "worker@lubecrm.com"
                                            passwordInput = "sales123"
                                        }
                                        "Owner" -> {
                                            emailInput = "owner@lubecrm.com"
                                            passwordInput = "admin123"
                                        }
                                    }
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = "Quick Autofill",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Automated Demo Bypass Mode",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = when (selectedPortal) {
                                        "Customer" -> "Email: customer@asianstar.com | Pass: customer77"
                                        "Worker" -> "Email: worker@lubecrm.com | Pass: sales123"
                                        else -> "Email: owner@lubecrm.com | Pass: admin123"
                                    },
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    // Secure Portal Sign-In trigger button
                    Button(
                        onClick = {
                            if (emailInput.isBlank() || passwordInput.isBlank()) {
                                Toast.makeText(context, "Please write complete matching credentials.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            when (selectedPortal) {
                                "Customer" -> {
                                    val cleanEmail = emailInput.trim().lowercase()
                                    val cleanPass = passwordInput.trim()
                                    if ((cleanEmail == "customer@asianstar.com" && cleanPass == "customer77") || 
                                        (cleanEmail == "dealer@asianstar.com" && cleanPass == "dealer77")) {
                                        viewModel.loginUser(emailInput.trim(), "Customer", "Arvinder Singh (Apex Lubricants & Spares)")
                                        Toast.makeText(context, "Welcome back Arvinder, loaded Elite benefits!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Invalid Customer credentials. Use demo helper bypass.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                "Worker" -> {
                                    if (emailInput.trim() == "worker@lubecrm.com" && passwordInput.trim() == "sales123") {
                                        viewModel.loginUser(emailInput.trim(), "Worker", "Vikram (Sales Field Executive)")
                                        Toast.makeText(context, "Welcome Vikram, live GPS transit logging standby!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Invalid field rep credentials. Use demo helper.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                "Owner" -> {
                                    if (emailInput.trim() == "owner@lubecrm.com" && passwordInput.trim() == "admin123") {
                                        viewModel.loginUser(emailInput.trim(), "Owner", "Anil (Corporate Chief Executive)")
                                        Toast.makeText(context, "Welcome Anil, loading live team telemetry!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Invalid corporate key. Use demo helper.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_login_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Connect Secure Asianstar Network", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Regulatory Compliance Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Encrypted Token Access Ledger",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Asianstar Lubricants secure session terminal. All transactions, telemetry updates, and workflow authorization requests are logged on internal ledger auditing protocols.",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 13.sp
                    )
                }
            }
        }
    }
}
