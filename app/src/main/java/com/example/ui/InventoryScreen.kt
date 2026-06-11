package com.example.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.LubricantProduct

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: CRMViewModel,
    products: List<LubricantProduct>
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showStockAdjustDialog by remember { mutableStateOf<LubricantProduct?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    val categories = listOf("All", "2-Wheeler Motor Oil", "Heavy Duty Truck Oil", "Hydraulic Fluid", "Gear & Transmission Oil", "Industrial Grease")
    val filteredProducts = if (selectedCategoryFilter == "All") {
        products
    } else {
        products.filter { it.category == selectedCategoryFilter }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("inventory_screen_root"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.testTag("add_product_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Product")
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Lubricants Warehouse",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Manage available and dispatched products stock",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Categories Filter Bar (Scrollable chips)
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategoryFilter).coerceAtLeast(0),
                edgePadding = 0.dp,
                divider = {},
                containerColor = Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEach { category ->
                    val isSelected = category == selectedCategoryFilter
                    Tab(
                        selected = isSelected,
                        onClick = { selectedCategoryFilter = category },
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = category,
                                color = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Inventory List
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = "Empty Inventory",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No products found in this category.",
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
                    items(filteredProducts) { product ->
                        InventoryCardItem(
                            product = product,
                            onAdjustStock = { showStockAdjustDialog = product }
                        )
                    }
                }
            }
        }
    }

    // ========== Dialog: Add Product ==========
    if (showAddDialog) {
        AddProductDialog(
            categories = categories.filter { it != "All" },
            onDismiss = { showAddDialog = false },
            onAdd = { name, cat, size, stock, price ->
                viewModel.addProduct(name, cat, size, stock, price)
                showAddDialog = false
            }
        )
    }

    // ========== Dialog: Adjust Stock / Reprice ==========
    showStockAdjustDialog?.let { product ->
        AdjustStockDialog(
            product = product,
            onDismiss = { showStockAdjustDialog = null },
            onUpdate = { addedQty, newPrice ->
                // Add stock
                if (addedQty != 0) {
                    viewModel.addStock(product.id, addedQty)
                }
                // Price modification or other update
                viewModel.updateProduct(product.copy(unitPrice = newPrice))
                showStockAdjustDialog = null
            },
            onDelete = {
                viewModel.deleteProduct(product.id)
                showStockAdjustDialog = null
            }
        )
    }
}

@Composable
fun InventoryCardItem(
    product: LubricantProduct,
    onAdjustStock: () -> Unit
) {
    val isLowStock = product.availableStock < 100

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAdjustStock() }
            .testTag("product_item_${product.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isLowStock) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category specifier icon representation
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isLowStock) MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            product.category.contains("Engine") || product.category.contains("Motor") -> Icons.Default.DirectionsCar
                            product.category.contains("Hydraulic") -> Icons.Default.WaterDrop
                            product.category.contains("Grease") -> Icons.Default.Opacity
                            else -> Icons.Default.Settings
                        },
                        contentDescription = "Category Icon",
                        tint = if (isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "${product.packSize} • Unit Price: $${String.format("%.2f", product.unitPrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Info columns
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${product.availableStock}",
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Available",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))

            Spacer(modifier = Modifier.height(8.dp))

            // Sub info metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "Sales Tracker",
                        tint = Color(0xFF2ECC71),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${product.soldStock} items total dispatched",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                if (isLowStock) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Required Production",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Low Stock: Refill",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    Text(
                        text = "Stock Normal",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF2ECC71),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ========== Dialog Composition: Add Product ==========
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductDialog(
    categories: List<String>,
    onDismiss: () -> Unit,
    onAdd: (name: String, cat: String, size: String, stock: Int, price: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "") }
    var packSize by remember { mutableStateOf("1L Bottle") }
    var stock by remember { mutableStateOf("250") }
    var price by remember { mutableStateOf("6.99") }
    var expandedCatDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("add_product_dialog"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "New Lubricant Listing",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name (e.g., Engine Max 4T)") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("add_product_name_input")
                )

                // Category dropdown Selection
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        label = { Text("Lubricant Category") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expandedCatDropdown = !expandedCatDropdown }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Category")
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = expandedCatDropdown,
                        onDismissRequest = { expandedCatDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    expandedCatDropdown = false
                                }
                            )
                        }
                    }
                }

                // Pack Size Selection list (common industrial sizes)
                val packSizes = listOf("1L Bottle", "5L Can", "20L Pail", "210L Drum", "1kg Tub")
                var expandedPackDropdown by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = packSize,
                        onValueChange = {},
                        label = { Text("Standard Packaging Unit") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expandedPackDropdown = !expandedPackDropdown }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Pack size")
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    DropdownMenu(
                        expanded = expandedPackDropdown,
                        onDismissRequest = { expandedPackDropdown = false }
                    ) {
                        packSizes.forEach { ps ->
                            DropdownMenuItem(
                                text = { Text(ps) },
                                onClick = {
                                    packSize = ps
                                    expandedPackDropdown = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = stock,
                        onValueChange = { stock = it },
                        label = { Text("Initial Stock") },
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Unit Price ($)") },
                        shape = RoundedCornerShape(10.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
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
                            if (name.isNotEmpty() && selectedCategory.isNotEmpty()) {
                                onAdd(
                                    name,
                                    selectedCategory,
                                    packSize,
                                    stock.toIntOrNull() ?: 0,
                                    price.toDoubleOrNull() ?: 0.0
                                )
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("submit_product_button")
                    ) {
                        Text("Add Product")
                    }
                }
            }
        }
    }
}

// ========== Dialog Composition: Adjust Stock & Reprice ==========
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdjustStockDialog(
    product: LubricantProduct,
    onDismiss: () -> Unit,
    onUpdate: (addedStock: Int, newPrice: Double) -> Unit,
    onDelete: () -> Unit
) {
    var addedQuantity by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf(product.unitPrice.toString()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("adjust_stock_dialog"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Refill & Edit Product",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Product", tint = MaterialTheme.colorScheme.error)
                    }
                }

                if (showDeleteConfirm) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "Confirm Deletion?",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "This will erase this lubricant item from LubeCRM database entirely.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                TextButton(onClick = { showDeleteConfirm = false }) {
                                    Text("Back")
                                }
                                Button(
                                    onClick = onDelete,
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Confirm Delete", color = Color.White)
                                }
                            }
                        }
                    }
                }

                Text(
                    text = "${product.name} (${product.packSize})",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Current Warehouse Stock: ${product.availableStock} Units\nTotal Dispatched: ${product.soldStock} Units",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

                OutlinedTextField(
                    value = addedQuantity,
                    onValueChange = { addedQuantity = it },
                    label = { Text("Log New Production Batch (Qty to ADD)") },
                    placeholder = { Text("e.g. 50 or 100") },
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("add_qty_input")
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Adjust Unit Selling Price ($)") },
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth().testTag("adjust_price_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Dismiss")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val addQty = addedQuantity.toIntOrNull() ?: 0
                            val finalPrice = priceText.toDoubleOrNull() ?: product.unitPrice
                            onUpdate(addQty, finalPrice)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_adjust_button")
                    ) {
                        Text("Apply Changes")
                    }
                }
            }
        }
    }
}
