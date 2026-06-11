package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
    var isOwnerTab by remember { mutableStateOf(false) } // false = Worker (Sales Field Rep), true = Owner (Manager Portal)
    
    var emailInput by remember { mutableStateOf("worker@lubecrm.com") }
    var passwordInput by remember { mutableStateOf("sales123") }
    var showPassword by remember { mutableStateOf(false) }

    // Synchronize inputs when toggling roles for quick testing
    LaunchedEffect(isOwnerTab) {
        if (isOwnerTab) {
            emailInput = "owner@lubecrm.com"
            passwordInput = "admin123"
        } else {
            emailInput = "worker@lubecrm.com"
            passwordInput = "sales123"
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
                .widthIn(max = 450.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // LubeCRM Branding Logo & Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = "Lubricants CRM Logo",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(44.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "LubeCRM Portals",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Field Sales Dispatch, GPS & CRM Control",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tab Selector (Worker vs Owner)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                // Field Worker
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!isOwnerTab) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable { isOwnerTab = false }
                        .padding(vertical = 10.dp)
                        .testTag("worker_login_tab"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = "Worker icon",
                            tint = if (!isOwnerTab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Field Worker",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (!isOwnerTab) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // Business Owner
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isOwnerTab) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable { isOwnerTab = true }
                        .padding(vertical = 10.dp)
                        .testTag("owner_login_tab"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Owner icon",
                            tint = if (isOwnerTab) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Business Owner",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isOwnerTab) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Input Fields Card
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
                        text = if (isOwnerTab) "Business Manager Portal Access" else "Sales Executive Logs Access",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Email Input
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Database ID / Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("login_email"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Password Input
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Portal Password Code") },
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

                    // Automated Demo Fill Helper
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable {
                                    if (isOwnerTab) {
                                        emailInput = "owner@lubecrm.com"
                                        passwordInput = "admin123"
                                    } else {
                                        emailInput = "worker@lubecrm.com"
                                        passwordInput = "sales123"
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
                                    text = "Demo Testing Mode Active",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = if (isOwnerTab) "Autofill Owner: owner@lubecrm.com | Password: admin123"
                                           else "Autofill Vikram: worker@lubecrm.com | Password: sales123",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Sign In Button
                    Button(
                        onClick = {
                            if (emailInput.isBlank() || passwordInput.isBlank()) {
                                Toast.makeText(context, "Please write clean matching credentials.", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            // Role Routing logic
                            if (isOwnerTab) {
                                if (emailInput.trim() == "owner@lubecrm.com" && passwordInput.trim() == "admin123") {
                                    viewModel.loginUser(emailInput.trim(), "Owner", "Anil (Group Business Owner)")
                                    Toast.makeText(context, "Welcome back Anil, loading remote staff updates!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Invalid Owner credentials. Use demo helper.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                if (emailInput.trim() == "worker@lubecrm.com" && passwordInput.trim() == "sales123") {
                                    viewModel.loginUser(emailInput.trim(), "Worker", "Vikram (Sales Field Executive)")
                                    Toast.makeText(context, "Welcome shift Vikram, GPS sync standby active!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Invalid Worker credentials. Use demo helper.", Toast.LENGTH_SHORT).show()
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
                        Text("Secure Portal Sign-In", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // High Quality Architect Explain Summary Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Production Token Authentication Setup",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "This screen sets the user's local workspace state. To connect with custom login protocols in production, use standard Firebase Authentication SDK by invoking FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password) or inject JWT bearer tokens onto external API routers securely via OkHttp headers.",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 13.sp
                    )
                }
            }
        }
    }
}
