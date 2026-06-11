package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FormSubmission
import com.example.data.LubricantProduct

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FormSharingScreen(
    viewModel: CRMViewModel,
    submissions: List<FormSubmission>,
    products: List<LubricantProduct>
) {
    var activeTab by remember { mutableStateOf("Gen links") } // "Gen links", "Public Web Simulator", "Inbox Submissions"
    val context = LocalContext.current
    val unprocessedCount = submissions.count { !it.isProcessed }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("form_sharing_screen_root")
    ) {
        // Header
        Text(
            text = "Form Gateway & Sharing",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Generate links for mechanics or customers to register and book orders directly",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Triple tab switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp)
        ) {
            listOf("Gen links", "Public Simulator", "Incoming Submissions").forEach { title ->
                val isSelected = when (title) {
                    "Gen links" -> activeTab == "Gen links"
                    "Public Simulator" -> activeTab == "Public Simulator"
                    else -> activeTab == "Inbox Submissions"
                }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .clickable {
                            activeTab = when (title) {
                                "Gen links" -> "Gen links"
                                "Public Simulator" -> "Public Simulator"
                                else -> "Inbox Submissions"
                            }
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (title == "Incoming Submissions") "Inbound" else title,
                            color = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (title == "Incoming Submissions" && unprocessedCount > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$unprocessedCount",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab views
        when (activeTab) {
            "Gen links" -> LinkGeneratorTab(viewModel = viewModel, context = context)
            "Public Simulator" -> FormSimulatorTab(viewModel = viewModel, products = products, context = context)
            "Inbox Submissions" -> SubmissionsGatewayTab(viewModel = viewModel, submissions = submissions, context = context)
        }
    }
}

// ================= Tab 1: Link Generator & Share Message Cards =================
@Composable
fun LinkGeneratorTab(viewModel: CRMViewModel, context: Context) {
    val templates = listOf(
        FormTemplate(
            title = "Mechanic Loyalty Sign-Up Form",
            description = "Let local workshops sign up to your lubricant rewards. Creates a CRM contact instantly on approval.",
            urlType = "Mechanic Loyalty Sign-Up",
            icon = Icons.Default.CardMembership
        ),
        FormTemplate(
            title = "Retailer Direct Booking Link",
            description = "Allow retailers & lube dealers to submit urgent stock requests online. Stocks automatically deduct on approval.",
            urlType = "Retailer Booking Form",
            icon = Icons.Default.ShoppingCart
        ),
        FormTemplate(
            title = "Mechanic Feedback survey",
            description = "Satisfactory checklist for mechanics testing new formulas (Lithium Grease, Heavy Active Diesel).",
            urlType = "Mechanic Feedback Survey",
            icon = Icons.Default.Star
        )
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f))
                    .border(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "💡 Rep Share Instructions",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Reps can select any CRM form template below, copy the pre-filled message, and send it directly over WhatsApp or SMS to mechanics or dealers! It eliminates paperwork.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        items(templates) { template ->
            var dealerExtra by remember { mutableStateOf("Kishan Spares & Garage") }

            Card(
                modifier = Modifier.fillMaxWidth().testTag("template_${template.urlType.replace(" ", "_").lowercase()}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = template.icon,
                                contentDescription = "icon",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = template.title,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = template.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (template.urlType == "Retailer Booking Form") {
                        OutlinedTextField(
                            value = dealerExtra,
                            onValueChange = { dealerExtra = it },
                            label = { Text("Pre-fill Dealer/Shop Parameter") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )
                    }

                    val fullMessage = viewModel.getShareableMessage(template.urlType, dealerExtra)

                    // Card of formatted share link
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = fullMessage,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("LubeCRM Link", fullMessage)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied Form Link Message!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "copy", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Message")
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Button(
                            onClick = {
                                Toast.makeText(context, "Simulating platform share sheet...", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "share", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Link")
                        }
                    }
                }
            }
        }
    }
}

// ================= Tab 2: public Web-Form Simulator =================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormSimulatorTab(
    viewModel: CRMViewModel,
    products: List<LubricantProduct>,
    context: Context
) {
    var formType by remember { mutableStateOf("Retailer Booking Form") }
    var senderName by remember { mutableStateOf("") }
    var contactInfo by remember { mutableStateOf("") }
    
    // For retailer order
    var selectedProduct by remember { mutableStateOf(products.firstOrNull()?.name ?: "Engine Max 4T 10W-30") }
    var requestedQty by remember { mutableStateOf("50") }
    var notesText by remember { mutableStateOf("Dealer requests delivery by this Saturday.") }

    val forms = listOf("Retailer Booking Form", "Mechanic Loyalty Sign-Up")

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                    .padding(14.dp)
            ) {
                Column {
                    Text(
                        text = "📲 Customer Web-Form Sandbox",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "This simulates what a mechanic or shop dealer sees on their phone browser when they open the shared CRM link. Try submitting an order/registration to see CRM database sync in real-time!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        item {
            // Simulated mobile phone container
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .testTag("form_simulator_phone"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
            ) {
                Column {
                    // Mobile Top Bar visual
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🌐 Chrome Secure Web", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text("100% Sync", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Title header in web
                        Text(
                            text = if (formType == "Retailer Booking Form") "Lubricants Orders Gateway" else "Mechanic Rewards Enrolment",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Please enter and submit the verified company form:",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )

                        // Selector Form type
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            forms.forEach { f ->
                                val isSelected = f == formType
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { formType = f }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (f.contains("Loyalty")) "Join Loyalty" else "Order WebForm",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                        OutlinedTextField(
                            value = senderName,
                            onValueChange = { senderName = it },
                            label = { Text(if (formType == "Retailer Booking Form") "Retailer Shop / Dealer Name" else "Mechanic Garage Name") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("sim_sender_input")
                        )

                        OutlinedTextField(
                            value = contactInfo,
                            onValueChange = { contactInfo = it },
                            label = { Text("Mobile Contact No.") },
                            shape = RoundedCornerShape(8.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth().testTag("sim_contact_input")
                        )

                        if (formType == "Retailer Booking Form") {
                            // Dropdown select product online
                            var expandedProd by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = selectedProduct,
                                    onValueChange = {},
                                    label = { Text("Select Lubricant Product") },
                                    readOnly = true,
                                    trailingIcon = {
                                        IconButton(onClick = { expandedProd = !expandedProd }) {
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = "products")
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                DropdownMenu(
                                    expanded = expandedProd,
                                    onDismissRequest = { expandedProd = false },
                                    modifier = Modifier.fillMaxWidth(0.8f)
                                ) {
                                    products.forEach { prod ->
                                        DropdownMenuItem(
                                            text = { Text(prod.name) },
                                            onClick = {
                                                selectedProduct = prod.name
                                                expandedProd = false
                                            }
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = requestedQty,
                                onValueChange = { requestedQty = it },
                                label = { Text("Order Quantity (Units / Litres)") },
                                shape = RoundedCornerShape(8.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth().testTag("sim_qty_input")
                            )

                            OutlinedTextField(
                                value = notesText,
                                onValueChange = { notesText = it },
                                label = { Text("Special Dispatch instructions") },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            OutlinedTextField(
                                value = notesText,
                                onValueChange = { notesText = it },
                                label = { Text("Garage Address & Lubricants used monthly") },
                                placeholder = { Text("Address details. e.g. Sector 5 Pune. Uses 50L engine oil monthly.") },
                                shape = RoundedCornerShape(8.dp),
                                maxLines = 3,
                                modifier = Modifier.fillMaxWidth().height(80.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // SUBMIT MOBILE WEB BUTTON
                        Button(
                            onClick = {
                                if (senderName.isNotEmpty() && contactInfo.isNotEmpty()) {
                                    val formattedContent = if (formType == "Retailer Booking Form") {
                                        "Requested Product: $selectedProduct | Qty: ${requestedQty.toIntOrNull() ?: 10} | Delivery Notes: $notesText"
                                    } else {
                                        "Garage details: $notesText"
                                    }

                                    viewModel.simulatePublicFormSubmission(
                                        type = formType,
                                        senderName = senderName,
                                        contactInfo = contactInfo,
                                        details = formattedContent
                                    )

                                    Toast.makeText(context, "🎉 Web Form Submitted to LubeCRM!", Toast.LENGTH_LONG).show()
                                    // Reset inputs
                                    senderName = ""
                                    contactInfo = ""
                                } else {
                                    Toast.makeText(context, "Please fill out all simulated web fields!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("sim_submit_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = "submit web", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SUBMIT TO LUBECRM GATEWAY", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// ================= Tab 3: Incoming Submissions list with rapid approvals =================
@Composable
fun SubmissionsGatewayTab(
    viewModel: CRMViewModel,
    submissions: List<FormSubmission>,
    context: Context
) {
    val unProcessed = submissions.filter { !it.isProcessed }
    val processed = submissions.filter { it.isProcessed }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1C40F))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Awaiting Representative Verification (${unProcessed.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (unProcessed.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No pending online form orders or signups.\nTry submitting one in 'Public Simulator'!",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            items(unProcessed) { sub ->
                SubmissionGatewayCardItem(sub = sub, isHistory = false, viewModel = viewModel, context = context)
            }
        }

        item {
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2ECC71))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Processed Submissions History (${processed.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (processed.isEmpty()) {
            item {
                Box(modifier = Modifier.padding(16.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "History is empty.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
            }
        } else {
            items(processed) { sub ->
                SubmissionGatewayCardItem(sub = sub, isHistory = true, viewModel = viewModel, context = context)
            }
        }
    }
}

@Composable
fun SubmissionGatewayCardItem(
    sub: FormSubmission,
    isHistory: Boolean,
    viewModel: CRMViewModel,
    context: Context
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("submission_card_${sub.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isHistory) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = sub.senderName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Tel: ${sub.contactInfo}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (sub.formType == "Mechanic Loyalty Sign-Up") Color(0xFFE67E22).copy(alpha = 0.15f)
                            else Color(0xFF3498DB).copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (sub.formType == "Mechanic Loyalty Sign-Up") "Mechanic Gym" else "Order Form",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (sub.formType == "Mechanic Loyalty Sign-Up") Color(0xFFE67E22) else Color(0xFF3498DB)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(10.dp)
            ) {
                Text(
                    text = sub.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!isHistory) {
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { viewModel.deleteSubmission(sub.id) },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "delete", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Discard")
                    }

                    Row {
                        if (sub.formType == "Mechanic Loyalty Sign-Up") {
                            Button(
                                onClick = {
                                    viewModel.processSubmission(sub, registerAsCustomer = true, approveOrder = false)
                                    Toast.makeText(context, "Added Mechanic directly to CRM partners!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2ECC71)),
                                modifier = Modifier.testTag("action_approve_mechanic_${sub.id}")
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = "approve")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Approve & Enroll", fontSize = 11.sp)
                            }
                        } else {
                            Button(
                                onClick = {
                                    viewModel.processSubmission(sub, registerAsCustomer = false, approveOrder = true)
                                    Toast.makeText(context, "Approved order, updated stock & logged visit!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.testTag("action_approve_order_${sub.id}")
                            ) {
                                Icon(Icons.Default.OfflinePin, contentDescription = "approve")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Approve order", fontSize = 11.sp)
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = "success", tint = Color(0xFF2ECC71), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Processed", color = Color(0xFF2ECC71), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

data class FormTemplate(
    val title: String,
    val description: String,
    val urlType: String,
    val icon: ImageVector
)
