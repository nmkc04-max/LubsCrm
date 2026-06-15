package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import coil.compose.AsyncImage
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LubricantProduct
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

// Local representation of a Cart Item (Immutable for pristine Jetpack Compose state changes)
data class CartItem(
    val product: LubricantProduct,
    val quantity: Int
)

// Data representation for orders
data class CRMOrder(
    val id: String,
    val date: String,
    val status: String,
    val items: String,
    val price: Double,
    val isCredit: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerPortalScreen(
    viewModel: CRMViewModel,
    modifier: Modifier = Modifier
) {
    val brandPrimary = Color(0xFF6C47FF)
    val brandSecondary = Color(0xFFF5F5F5)
    val brandBackground = Color(0xFFFFFFFF)
    val brandTextColor = Color(0xFF111827)

    val customerColorScheme = lightColorScheme(
        primary = brandPrimary,
        onPrimary = Color.White,
        primaryContainer = Color(0xFFECEBFF),
        onPrimaryContainer = brandPrimary,
        secondary = brandPrimary,
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFF5F5F5),
        onSecondaryContainer = brandTextColor,
        background = brandBackground,
        onBackground = brandTextColor,
        surface = Color.White,
        onSurface = brandTextColor,
        surfaceVariant = Color(0xFFF9F9FA),
        onSurfaceVariant = brandTextColor.copy(alpha = 0.7f),
        outline = Color(0xFFE2E8F0)
    )

    MaterialTheme(
        colorScheme = customerColorScheme
    ) {
        val context = LocalContext.current
        val products by viewModel.products.collectAsState()

        val additionalProducts = remember {
            listOf(
                LubricantProduct(id = 1001, name = "UltraFlow Premium Engine Filter", category = "Filters", packSize = "1 Unit", availableStock = 450, soldStock = 120, unitPrice = 280.0),
                LubricantProduct(id = 1002, name = "ProShield Oil Filter HF-90", category = "Filters", packSize = "1 Unit", availableStock = 320, soldStock = 90, unitPrice = 195.0),
                LubricantProduct(id = 1003, name = "MicroGlow Chrome Polish Spray", category = "Accessories", packSize = "250mL Can", availableStock = 180, soldStock = 340, unitPrice = 150.0),
                LubricantProduct(id = 1004, name = "Heavy Duty Cleaning Microfiber Grid", category = "Accessories", packSize = "3 Pack", availableStock = 600, soldStock = 1200, unitPrice = 99.0),
                LubricantProduct(id = 1005, name = "LubeStar Spark Plug SP-20", category = "Spare Parts", packSize = "4 Unit Box", availableStock = 240, soldStock = 150, unitPrice = 350.0)
            )
        }
        val allDisplayProducts = remember(products) { products + additionalProducts }
        
        // Core Customer States
        var selectedLocation by remember { mutableStateOf("Viman Nagar Market Hub, Pune") }
        var searchQuery by remember { mutableStateOf("") }
        var selectedCategory by remember { mutableStateOf("All") }
        
        // Cart List managed with copied modifications to trigger instant recompositions
        val cart = remember { mutableStateListOf<CartItem>() }
        var appliedCoupon by remember { mutableStateOf<String?>(null) }
        var redeemLoyaltyPoints by remember { mutableStateOf(false) }
        
        // Portal Sections: 0=Home, 1=Categories, 2=Orders, 3=Rewards, 4=Profile
        var currentSubTab by remember { mutableIntStateOf(0) }
        
        // Address Selection State
        var showAddressDialog by remember { mutableStateOf(false) }
        
        // Details dialog trigger
        var detailedProduct by remember { mutableStateOf<LubricantProduct?>(null) }
        
        // Profile Sub-Section Authenticity Scan States
        var scannedProductCode by remember { mutableStateOf("") }
        var scanVerifiedResult by remember { mutableStateOf<String?>(null) }
        
        // Loyalty and Tier Info (Demo Mock mutable for custom interactions)
        var currentTier by remember { mutableStateOf("Platinum Elite") }
        var loyaltyPoints by remember { mutableIntStateOf(3450) }
        var creditLimit by remember { mutableDoubleStateOf(50000.0) }
        var usedCredit by remember { mutableDoubleStateOf(14200.0) }
        val activeOrders = remember { 
            mutableStateListOf(
                CRMOrder(id = "AS-99214", date = "Today, 10:45 AM", status = "In Transit", items = "Engine Max 4T 10W-30 (25 Packs)", price = 137.50, isCredit = true),
                CRMOrder(id = "AS-98102", date = "08 Jun 2026", status = "Delivered", items = "GearForce EP-90 Premium (10 Cans)", price = 225.00, isCredit = false)
            )
        }

        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    NavigationBarItem(
                        icon = { Icon(if (currentSubTab == 0) Icons.Default.Home else Icons.Outlined.Home, contentDescription = "Home") },
                        label = { Text("Home", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        selected = currentSubTab == 0,
                        onClick = { currentSubTab = 0 }
                    )
                    NavigationBarItem(
                        icon = { Icon(if (currentSubTab == 1) Icons.Default.Category else Icons.Outlined.Category, contentDescription = "Categories") },
                        label = { Text("Categories", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        selected = currentSubTab == 1,
                        onClick = { currentSubTab = 1 }
                    )
                    NavigationBarItem(
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (cart.isNotEmpty()) {
                                        Badge(containerColor = Color(0xFF6C47FF)) { 
                                            Text("${cart.sumOf { it.quantity }}", color = Color.White) 
                                        }
                                    }
                                }
                            ) {
                                Icon(if (currentSubTab == 2) Icons.Default.ReceiptLong else Icons.Outlined.ReceiptLong, contentDescription = "Orders")
                            }
                        },
                        label = { Text("Orders", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        selected = currentSubTab == 2,
                        onClick = { currentSubTab = 2 }
                    )
                    NavigationBarItem(
                        icon = { Icon(if (currentSubTab == 3) Icons.Default.Stars else Icons.Outlined.Stars, contentDescription = "Rewards") },
                        label = { Text("Rewards", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        selected = currentSubTab == 3,
                        onClick = { currentSubTab = 3 }
                    )
                    NavigationBarItem(
                        icon = { Icon(if (currentSubTab == 4) Icons.Default.Person else Icons.Outlined.Person, contentDescription = "Profile") },
                        label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        selected = currentSubTab == 4,
                        onClick = { currentSubTab = 4 }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color.White)
            ) {
                when (currentSubTab) {
                    0 -> MarketplaceTab(
                        products = allDisplayProducts,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        selectedCategory = selectedCategory,
                        onCategoryChange = { selectedCategory = it },
                        onProductClick = { detailedProduct = it },
                        onAddProduct = { product ->
                            val existingIndex = cart.indexOfFirst { it.product.id == product.id }
                            if (existingIndex != -1) {
                                val existing = cart[existingIndex]
                                cart[existingIndex] = existing.copy(quantity = existing.quantity + 1)
                            } else {
                                cart.add(CartItem(product, 1))
                            }
                            Toast.makeText(context, "${product.name} added to cart!", Toast.LENGTH_SHORT).show()
                        },
                        selectedLocation = selectedLocation,
                        onLocationClick = { showAddressDialog = true },
                        loyaltyPoints = loyaltyPoints,
                        onScrollToRewardsRequest = { currentSubTab = 3 }
                    )
                    1 -> CategoriesTab(
                        onSelectCategory = { category ->
                            selectedCategory = category
                            currentSubTab = 0
                        }
                    )
                    2 -> CartAndOrdersTab(
                        cartItems = cart,
                        appliedCoupon = appliedCoupon,
                        onApplyCoupon = { appliedCoupon = it },
                        redeemLoyaltyPoints = redeemLoyaltyPoints,
                        onToggleLoyaltyPoints = { redeemLoyaltyPoints = it },
                        loyaltyPoints = loyaltyPoints,
                        creditLimit = creditLimit,
                        usedCredit = usedCredit,
                        activeOrders = activeOrders,
                        onQuantityChange = { item, newQty ->
                            val index = cart.indexOf(item)
                            if (index >= 0) {
                                if (newQty <= 0) {
                                    cart.removeAt(index)
                                } else {
                                    cart[index] = item.copy(quantity = newQty)
                                }
                            }
                        },
                        onCheckout = { method, orderTotal, orderDetails ->
                            if (method == "Credit Account" && (creditLimit - usedCredit) < orderTotal) {
                                Toast.makeText(context, "Error: Checkout exceeds available B2B credit limit!", Toast.LENGTH_LONG).show()
                            } else {
                                if (redeemLoyaltyPoints) {
                                    loyaltyPoints = (loyaltyPoints - 500).coerceAtLeast(0)
                                    redeemLoyaltyPoints = false
                                }
                                if (method == "Credit Account") {
                                    usedCredit += orderTotal
                                }
                                // Earned loyalty rewards
                                loyaltyPoints += (orderTotal * 1.5).toInt()
                                
                                val orderId = "AS-${10000 + Random.nextInt(90000)}"
                                activeOrders.add(0, CRMOrder(
                                    id = orderId,
                                    date = "Today, " + SimpleDateFormat("hh:mm a", Locale.US).format(Date()),
                                    status = "Order Placed",
                                    items = orderDetails,
                                    price = orderTotal,
                                    isCredit = (method == "Credit Account")
                                ))
                                
                                cart.clear()
                                appliedCoupon = null
                                Toast.makeText(context, "B2B Order successfully placed via $method!", Toast.LENGTH_LONG).show()
                            }
                        },
                        onReorder = { order ->
                            val parts = order.items.split(" (")
                            val prodName = parts.firstOrNull() ?: ""
                            val pMatch = allDisplayProducts.find { it.name.lowercase() == prodName.lowercase() }
                            if (pMatch != null) {
                                cart.add(CartItem(pMatch, 25))
                                Toast.makeText(context, "Reordered ${pMatch.name} (Qty: 25) added to cart!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Specification changed. Configure order manually.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    3 -> DealerLoyaltyTab(
                        currentTier = currentTier,
                        loyaltyPoints = loyaltyPoints,
                        creditLimit = creditLimit,
                        usedCredit = usedCredit
                    )
                    4 -> DealerProfileTab(
                        scannedProductCode = scannedProductCode,
                        onScannedCodeChange = { scannedProductCode = it },
                        scanVerifiedResult = scanVerifiedResult,
                        onVerify = { code ->
                            scanVerifiedResult = if (code.lowercase().contains("asian") || code.lowercase().contains("lube") || code.length > 5) {
                                "SECURE MATCH ✅\nGenuine Asianstar Lubricant Batch #9823-A.\nManufacturing Date: 12-May-2026\nOrigin: Hyderabad Plant Warehouse.\nGrade Performance: Synthetic API SP certified."
                            } else {
                                "UNVERIFIED KEY ⚠️\nWarning: Product registration key not discovered in distribution ledger records. Contact corporate QA help immediately."
                            }
                        },
                        onClear = {
                            scannedProductCode = ""
                            scanVerifiedResult = null
                        }
                    )
                }

                // Sticky Bottom Cart Panel (Always Overlaying for Home and Categories views when items > 0)
                if (cart.isNotEmpty() && (currentSubTab == 0 || currentSubTab == 1)) {
                    val itemCount = cart.sumOf { it.quantity }
                    val netTotal = cart.sumOf { it.product.unitPrice * it.quantity }
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF6C47FF)),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "$itemCount ${if (itemCount == 1) "Item" else "Items"}",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "₹${String.format(Locale.US, "%,.0f", netTotal)}",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { currentSubTab = 2 }
                            ) {
                                Text(
                                    text = "View Cart",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowForwardIos,
                                    contentDescription = "View",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Custom Destination Address Dialog
            if (showAddressDialog) {
                val addressOptions = listOf(
                    "Viman Nagar Market Hub, Pune",
                    "Central Depot Warehousing, Vijayawada",
                    "Industrial Area Road #12, Hyderabad",
                    "Sai Service Auto Workshop, Gachibowli"
                )
                AlertDialog(
                    onDismissRequest = { showAddressDialog = false },
                    confirmButton = {
                        TextButton(onClick = { showAddressDialog = false }) { Text("Close") }
                    },
                    title = { Text("Select Delivery Destination", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF6C47FF)) },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            addressOptions.forEach { addr ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedLocation = addr
                                            showAddressDialog = false
                                            Toast.makeText(context, "Delivery route redirected!", Toast.LENGTH_SHORT).show()
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.LocationOn, contentDescription = "Loc", tint = Color(0xFF6C47FF), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(addr, fontSize = 13.sp, color = Color(0xFF111827))
                                }
                            }
                        }
                    }
                )
            }

            // Product Details overlay dialog
            if (detailedProduct != null) {
                val prod = detailedProduct!!
                AlertDialog(
                    onDismissRequest = { detailedProduct = null },
                    confirmButton = {
                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C47FF)),
                            onClick = {
                                val existingIndex = cart.indexOfFirst { it.product.id == prod.id }
                                if (existingIndex != -1) {
                                    val existing = cart[existingIndex]
                                    cart[existingIndex] = existing.copy(quantity = existing.quantity + 1)
                                } else {
                                    cart.add(CartItem(prod, 1))
                                }
                                detailedProduct = null
                                Toast.makeText(context, "${prod.name} added to cart!", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text("Add to Cart", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { detailedProduct = null }) { 
                            Text("Close", color = Color(0xFF6C47FF)) 
                        }
                    },
                    title = { Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF6C47FF)) },
                    text = {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFECEBFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when {
                                        prod.category.contains("2-Wheeler") || prod.category.contains("Bike") -> Icons.Default.TwoWheeler
                                        prod.category.contains("Truck") -> Icons.Default.LocalShipping
                                        prod.category.contains("Hydraulic") -> Icons.Default.SettingsInputComponent
                                        prod.category.contains("Grease") -> Icons.Default.WaterDrop
                                        else -> Icons.Default.DirectionsCar
                                    },
                                    contentDescription = "Oil logo",
                                    tint = Color(0xFF6C47FF),
                                    modifier = Modifier.size(56.dp)
                                )
                            }
                            
                            Text(
                                text = "Technical Specifications",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF6C47FF)
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Category:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                                Text(prod.category, fontSize = 12.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Container Size:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                                Text(prod.packSize, fontSize = 12.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("B2B Invoice Price:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                                Text("₹${String.format(Locale.US, "%.2f", prod.unitPrice)} / pack", fontSize = 12.sp, color = Color(0xFF6C47FF), fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Recommended MRP:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                                Text("₹${String.format(Locale.US, "%.2f", prod.unitPrice * 1.35)} / pack", fontSize = 12.sp, style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Standard Margin:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                                Text("35% profit margin markup", fontSize = 12.sp, color = Color(0xFF6C47FF), fontWeight = FontWeight.Bold)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Depot Available Stock:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                                Text("${prod.availableStock} packs", fontSize = 12.sp, color = Color(0xFF22C55E), fontWeight = FontWeight.Bold)
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Application Guideline", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF6C47FF))
                            Text(
                                text = "Engineered to deliver ultimate protection against friction, thermal breakdown, and engine sludge. API SP and OEM standards certified for commercial/industrial environments.",
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = Color.Gray
                            )
                        }
                    }
                )
            }
        }
    }
}

fun matchesCategory(productCategory: String, uiCategory: String): Boolean {
    if (uiCategory == "All") return true
    return when (uiCategory) {
        "Engine Oils" -> productCategory.contains("Oil", ignoreCase = true) && !productCategory.contains("Base", ignoreCase = true) || productCategory.contains("Transmission", ignoreCase = true) || productCategory.contains("Gear", ignoreCase = true)
        "Coolants" -> productCategory.contains("Coolant", ignoreCase = true)
        "Greases" -> productCategory.contains("Grease", ignoreCase = true) || productCategory.contains("Base", ignoreCase = true)
        "Additives" -> productCategory.contains("Additive", ignoreCase = true) || productCategory.contains("Base", ignoreCase = true)
        "Batteries" -> productCategory.contains("Battery", ignoreCase = true)
        "Filters" -> productCategory.contains("Filter", ignoreCase = true)
        "Accessories" -> productCategory.contains("Accessory", ignoreCase = true)
        "Spare Parts" -> productCategory.contains("Spare", ignoreCase = true)
        else -> productCategory.equals(uiCategory, ignoreCase = true)
    }
}

fun getCategoryIcon(cat: String): ImageVector {
    return when (cat) {
        "All" -> Icons.Default.GridView
        "Engine Oils" -> Icons.Default.DirectionsCar
        "Coolants" -> Icons.Default.Thermostat
        "Greases" -> Icons.Default.WaterDrop
        "Additives" -> Icons.Default.Settings
        "Batteries" -> Icons.Default.FlashOn
        "Filters" -> Icons.Default.FilterAlt
        "Accessories" -> Icons.Default.Extension
        "Spare Parts" -> Icons.Default.Build
        else -> Icons.Default.GridView
    }
}

@Composable
fun OffersCompactBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF6C47FF), Color(0xFF8B5CF6))
                )
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "MONSOON CARGO BLAST",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700)
                )
                Text(
                    text = "Flat 5% Discount on Invoice Billing",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Code: MONSOON5",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun ProductGridCardCompact(
    product: LubricantProduct,
    onProductClick: (LubricantProduct) -> Unit,
    onAddProduct: (LubricantProduct) -> Unit
) {
    val imageUrl = getCategoryPromoImageUrl(product.category)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProductClick(product) },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFF3F4F6))
    ) {
        Column(modifier = Modifier.padding(6.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF3F4F6))
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF22C55E).copy(alpha = 0.9f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text("In Stock", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = product.name,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color(0xFF111827)
            )
            
            Text(
                text = product.packSize,
                fontSize = 9.sp,
                color = Color.Gray
            )

            Text(
                text = "${product.availableStock} remaining",
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium,
                color = if (product.availableStock < 200) Color(0xFFEF4444) else Color(0xFF22C55E)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₹${String.format(Locale.US, "%.0f", product.unitPrice)}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF6C47FF)
                )
                
                Button(
                    onClick = { onAddProduct(product) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF6C47FF)
                    ),
                    border = BorderStroke(1.5.dp, Color(0xFF6C47FF)),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.height(24.dp)
                ) {
                    Text("ADD", fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun MarketplaceTab(
    products: List<LubricantProduct>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    onProductClick: (LubricantProduct) -> Unit,
    onAddProduct: (LubricantProduct) -> Unit,
    selectedLocation: String,
    onLocationClick: () -> Unit,
    loyaltyPoints: Int,
    onScrollToRewardsRequest: () -> Unit
) {
    val context = LocalContext.current
    val categories = listOf("All", "Engine Oils", "Coolants", "Greases", "Additives", "Batteries", "Filters", "Accessories", "Spare Parts")

    val filtered = remember(products, selectedCategory, searchQuery) {
        products.filter {
            matchesCategory(it.category, selectedCategory) &&
            (searchQuery.isEmpty() || it.name.lowercase().contains(searchQuery.lowercase()))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top Section: Address, Search & Notifications
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onLocationClick() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Pin",
                        tint = Color(0xFF6C47FF),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("B2B SHIP ROUTE", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color(0xFF6C47FF))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "drop", modifier = Modifier.size(12.dp), tint = Color(0xFF6C47FF))
                        }
                        Text(
                            text = selectedLocation,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Cargo Dispatch: Regional hub is tracking 1 consignment.", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        BadgedBox(
                            badge = { Badge(containerColor = Color(0xFFE11D48)) { Text("1", color = Color.White) } }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Alerts", modifier = Modifier.size(20.dp), tint = Color(0xFF111827))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF3F4F6))
                            .clickable { onScrollToRewardsRequest() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Stars, contentDescription = "points", tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("$loyaltyPoints pts", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("q_commerce_search_field"),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("Search lubricants, oils & coolants...", fontSize = 12.sp, color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon", tint = Color(0xFF6C47FF)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6C47FF),
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedContainerColor = Color(0xFFF9FAFB),
                    unfocusedContainerColor = Color(0xFFF9FAFB),
                    focusedTextColor = Color(0xFF111827),
                    unfocusedTextColor = Color(0xFF111827),
                    focusedPlaceholderColor = Color.Gray,
                    unfocusedPlaceholderColor = Color.Gray
                ),
                singleLine = true
            )
        }

        // Middle Section: Offers Banner (Compact)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            OffersCompactBanner()
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        // Bottom Section: Vertical Category Rail (Left) + Grid (Right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Left Column Category Rail
            Column(
                modifier = Modifier
                    .width(90.dp)
                    .fillMaxHeight()
                    .background(Color(0xFFF9FAFB))
                    .verticalScroll(rememberScrollState())
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCategoryChange(cat) }
                            .background(if (isSelected) Color.White else Color.Transparent)
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFFECEBFF) else Color(0xFFF3F4F6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCategoryIcon(cat),
                                contentDescription = cat,
                                tint = if (isSelected) Color(0xFF6C47FF) else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Text(
                            text = cat,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF6C47FF) else Color(0xFF4B5563),
                            textAlign = TextAlign.Center,
                            lineHeight = 12.sp
                        )
                    }
                    Divider(color = Color(0xFFF3F4F6), thickness = 1.dp)
                }
            }
            
            // Vertical Divider Line
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(Color(0xFFF3F4F6))
            )
            
            // Right Column: Product Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .testTag("marketplace_grid"),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (filtered.isEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 60.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.SearchOff, contentDescription = "None", tint = Color.LightGray, modifier = Modifier.size(44.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No items found", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    items(filtered) { prod ->
                        ProductGridCardCompact(
                            product = prod,
                            onProductClick = onProductClick,
                            onAddProduct = onAddProduct
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoriesTab(
    onSelectCategory: (String) -> Unit
) {
    val categoryList = listOf(
        Pair("Engine Oils", Icons.Default.DirectionsCar),
        Pair("Coolants", Icons.Default.Thermostat),
        Pair("Greases", Icons.Default.WaterDrop),
        Pair("Additives", Icons.Default.Settings),
        Pair("Batteries", Icons.Default.FlashOn),
        Pair("Filters", Icons.Default.FilterAlt),
        Pair("Accessories", Icons.Default.Extension),
        Pair("Spare Parts", Icons.Default.Build)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text("Product Catalog Categories", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6C47FF))
            Text("Select a category to filter the regional warehouse lubricants range.", fontSize = 12.sp, color = Color.Gray)
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectCategory("All") },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFECEBFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.GridView, contentDescription = "All", tint = Color(0xFF6C47FF))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Complete Range Catalog", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                            Text("Show all 10,000+ lubricant assets", fontSize = 11.sp, color = Color.Gray)
                        }
                        Icon(Icons.Default.ArrowForwardIos, contentDescription = "Go", modifier = Modifier.size(14.dp), tint = Color(0xFF6C47FF))
                    }
                }
            }

            items(categoryList) { categoryPair ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectCategory(categoryPair.first) },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF3F4F6)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(categoryPair.second, contentDescription = categoryPair.first, tint = Color(0xFF6C47FF))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(categoryPair.first, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                            Text("Browse specifications & stock values", fontSize = 11.sp, color = Color.Gray)
                        }
                        Icon(Icons.Default.ArrowForwardIos, contentDescription = "Go", modifier = Modifier.size(14.dp), tint = Color(0xFF6C47FF))
                    }
                }
            }
        }
    }
}

@Composable
fun CartAndOrdersTab(
    cartItems: List<CartItem>,
    appliedCoupon: String?,
    onApplyCoupon: (String) -> Unit,
    redeemLoyaltyPoints: Boolean,
    onToggleLoyaltyPoints: (Boolean) -> Unit,
    loyaltyPoints: Int,
    creditLimit: Double,
    usedCredit: Double,
    activeOrders: List<CRMOrder>,
    onQuantityChange: (CartItem, Int) -> Unit,
    onCheckout: (String, Double, String) -> Unit,
    onReorder: (CRMOrder) -> Unit
) {
    val context = LocalContext.current
    var couponText by remember { mutableStateOf("") }
    var selectedPaymentMode by remember { mutableStateOf("UPI / QR") }
    
    val netTotal = cartItems.sumOf { it.product.unitPrice * it.quantity }
    val gst = netTotal * 0.18 // 18% standard GST
    val deliveryCharge = if (netTotal > 5400 || netTotal == 0.0) 0.0 else 250.0 // Free delivery above 5400
    val discount = when {
        appliedCoupon?.uppercase() == "MONSOON5" -> netTotal * 0.05
        appliedCoupon?.uppercase() == "ASIANSTAR20" -> netTotal * 0.20
        else -> 0.0
    }
    val pointsDiscount = if (redeemLoyaltyPoints && loyaltyPoints >= 500) 50.0 else 0.0
    val finalTotal = (netTotal + gst + deliveryCharge - discount - pointsDiscount).coerceAtLeast(0.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Procurement Cart", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        if (cartItems.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Empty", modifier = Modifier.size(36.dp), tint = Color.Gray)
                    Text("Your cart is empty. Add warehouse products to begin.", fontSize = 12.sp, color = Color.Gray)
                }
            }
        } else {
            // Cart Items List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                cartItems.forEach { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.product.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text("₹${String.format(Locale.US, "%.0f", item.product.unitPrice)} each x ${item.quantity} packs", fontSize = 11.sp, color = Color.Gray)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 2.dp)
                            ) {
                                IconButton(
                                    onClick = { onQuantityChange(item, item.quantity - 1) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Minus", modifier = Modifier.size(14.dp))
                                }
                                Text("${item.quantity}", modifier = Modifier.padding(horizontal = 6.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                IconButton(
                                    onClick = { onQuantityChange(item, item.quantity + 1) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Plus", modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }

            // Coupon applying codes
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = couponText,
                    onValueChange = { couponText = it },
                    placeholder = { Text("Coupon code (e.g., MONSOON5)", fontSize = 12.sp, color = Color.Gray) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (couponText.trim().isNotEmpty()) {
                            onApplyCoupon(couponText.trim())
                            Toast.makeText(context, "Promo code validated!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Apply", color = Color.White)
                }
            }

            // Loyalty Switch if eligible
            if (loyaltyPoints >= 500) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Redeem 500 Gold Points", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        Text("Saves extra ₹50.00 off B2B invoice value", fontSize = 10.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = redeemLoyaltyPoints,
                        onCheckedChange = onToggleLoyaltyPoints
                    )
                }
            }

            // Billing breakdown invoice report
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("B2B Invoice Breakdown Summary", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Cart Net Value:", fontSize = 11.sp, color = Color.Gray)
                        Text("₹${String.format(Locale.US, "%.2f", netTotal)}", fontSize = 11.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("18% GST (Govt Lubricant Tax):", fontSize = 11.sp, color = Color.Gray)
                        Text("₹${String.format(Locale.US, "%.2f", gst)}", fontSize = 11.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Regional Transport & Delivery:", fontSize = 11.sp, color = Color.Gray)
                        Text(if (deliveryCharge == 0.0) "FREE" else "₹${String.format(Locale.US, "%.2f", deliveryCharge)}", fontSize = 11.sp, color = if (deliveryCharge == 0.0) Color(0xFF22C55E) else MaterialTheme.colorScheme.onSurface)
                    }
                    if (discount > 0.0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Applied Promo Discount:", fontSize = 11.sp, color = Color(0xFF22C55E))
                            Text("- ₹${String.format(Locale.US, "%.2f", discount)}", fontSize = 11.sp, color = Color(0xFF22C55E), fontWeight = FontWeight.Bold)
                        }
                    }
                    if (pointsDiscount > 0.0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Loyalty Point Reduction:", fontSize = 11.sp, color = Color(0xFF22C55E))
                            Text("- ₹50.00", fontSize = 11.sp, color = Color(0xFF22C55E), fontWeight = FontWeight.Bold)
                        }
                    }
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Grand Total Payable:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("₹${String.format(Locale.US, "%.2f", finalTotal)}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Payment Term channel chooser
            Text("Select B2B Settlement Channel", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            listOf("UPI / QR", "Internet Banking", "Razorpay Secured Portal", "Credit Account").forEach { mode ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedPaymentMode == mode) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else Color.Transparent)
                        .clickable { selectedPaymentMode = mode }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedPaymentMode == mode,
                        onClick = { selectedPaymentMode = mode },
                        colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (mode == "Credit Account") "B2B Credit Line Ledger (Limit Avail: ₹${String.format(Locale.US, "%.0f", creditLimit - usedCredit)})" else mode,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Button(
                onClick = {
                    val detailsDesc = cartItems.joinToString(", ") { "${it.product.name} (${it.quantity} Packs)" }
                    onCheckout(selectedPaymentMode, finalTotal, detailsDesc)
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Confirm & Book B2B Load Delivery", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Divider()

        // Past purchases timeline track
        Text("Your Orders & Active Dispatch Logs", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        activeOrders.forEach { order ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("LOAD REF: ${order.id}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(order.date, fontSize = 10.sp, color = Color.Gray)
                        }
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (order.status == "Delivered") Color(0xFF22C55E).copy(alpha = 0.12f)
                                    else MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = order.status,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (order.status == "Delivered") Color(0xFF22C55E) else MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Text("Consignment: ${order.items}", fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = Color.Gray)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Amount: ₹${String.format(Locale.US, "%.2f", order.price)} ${if (order.isCredit) "(via Credit Terms)" else ""}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        TextButton(
                            onClick = { onReorder(order) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Quick Reorder", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        }
                    }

                    // Dispatch progress workflow tracker
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                            .padding(8.dp)
                    ) {
                        Text("B2B Consignment Track:", fontWeight = FontWeight.Bold, fontSize = 9.sp, color = MaterialTheme.colorScheme.primary)
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            TimelineTrackingIndicator("Placed", isCompleted = true)
                            TimelineTrackingIndicator("Gated", isCompleted = true)
                            TimelineTrackingIndicator("On-Road", isCompleted = order.status != "Order Placed")
                            TimelineTrackingIndicator("Completed", isCompleted = order.status == "Delivered")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineTrackingIndicator(label: String, isCompleted: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (isCompleted) Color(0xFF22C55E) else Color.LightGray)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 9.sp, fontWeight = if (isCompleted) FontWeight.Bold else FontWeight.Normal, color = if (isCompleted) Color(0xFF111827) else Color.Gray)
    }
}

@Composable
fun DealerLoyaltyTab(
    currentTier: String,
    loyaltyPoints: Int,
    creditLimit: Double,
    usedCredit: Double
) {
    val badges = listOf(
        Pair("Anchor Partner", Icons.Default.Handshake),
        Pair("Consistent Buyer", Icons.Default.TrendingUp),
        Pair("Premium Orderer", Icons.Default.EmojiEvents),
        Pair("Elite Partner", Icons.Default.MilitaryTech)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Loyalty & Rewards", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        // Tier Level Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(
                            colors = listOf(MaterialTheme.colorScheme.primary, Color(0xFF1E3A8A))
                        )
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("B2B PRIVILEGE TIER LEVEL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Text(currentTier, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.WorkspacePremium, contentDescription = "badge", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.2f))

                // Milestone ProgressBar
                Text("Progression: 82% towards Platinum Gold status", fontSize = 11.sp, color = Color.White.copy(alpha = 0.9f))
                Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.3f))) {
                    Box(modifier = Modifier.fillMaxHeight().fillMaxWidth(0.82f).clip(CircleShape).background(MaterialTheme.colorScheme.secondary))
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Lifetime Orders Vol: ₹1,84,500", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                    Text("Milestone: ₹2,00,000", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Benefits per tier catalog
        Text("Your Tier Perks Matrix", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                TierBenefitCardRow("Bronze Partner", "Basic 1.0x points accumulation rate.", currentTier == "Bronze")
                TierBenefitCardRow("Silver Partner", "1.2x points rate. Scheme privilege cashback.", currentTier == "Silver")
                TierBenefitCardRow("Gold Partner", "1.5x points rate. Handled with priority express delivery.", currentTier == "Gold")
                TierBenefitCardRow("Platinum Elite", "Double points multiplier. Dedicated ₹50,000 credit account terms.", currentTier == "Platinum Elite")
            }
        }

        // Earned Achievement Badges list
        Text("Your B2B Achievement Badges", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            badges.forEach { badge ->
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(badge.second, contentDescription = badge.first, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        }
                        Text(badge.first, fontSize = 8.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        // Regional distributors rankings checklist leaderboard
        Text("Leaderboard: India West-Zone Distributors", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                LeaderboardProfileRow("#1 Apex Lubricants & Spares", "Platinum Gold", "₹2,48,000 Volume", isSelf = true)
                LeaderboardProfileRow("#2 Sai Auto Accessories", "Gold Customer", "₹2,05,000 Volume")
                LeaderboardProfileRow("#3 Golden Wheels Service", "Platinum Elite", "₹1,84,500 Volume")
                LeaderboardProfileRow("#4 Metro Fluids Distributor", "Silver Customer", "₹1,49,000 Volume")
            }
        }

        // Refer-and-Earn Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Refer Dealer & Earn 500 Pts", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    Text("Share code: ASIANSTAR-REF-99 with partner workshops.", fontSize = 10.sp, color = Color.Gray)
                }
                Icon(Icons.Default.Send, contentDescription = "share", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun TierBenefitCardRow(tierName: String, desc: String, isActive: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = "checked",
            tint = if (isActive) Color(0xFF22C55E) else Color.LightGray,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(12.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(tierName, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium, fontSize = 11.sp, color = if (isActive) MaterialTheme.colorScheme.primary else Color.Gray)
            Text(desc, fontSize = 10.sp, color = if (isActive) MaterialTheme.colorScheme.onSurface else Color.Gray)
        }
    }
}

@Composable
fun LeaderboardProfileRow(tag: String, tier: String, amount: String, isSelf: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(tag, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (isSelf) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
            Text(tier, fontSize = 9.sp, color = Color.Gray)
        }
        Text(amount, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFF22C55E))
    }
}

@Composable
fun DealerProfileTab(
    scannedProductCode: String,
    onScannedCodeChange: (String) -> Unit,
    scanVerifiedResult: String?,
    onVerify: (String) -> Unit,
    onClear: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("My Business Profile", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

        // Executive B2B profile card details
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("AS", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Arvinder Singh", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text("Apex Lubricants & Spares", fontSize = 11.sp, color = Color.Gray)
                    }
                }
                
                Divider()
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("GSTIN Identification:", fontSize = 11.sp, color = Color.Gray)
                    Text("27AABCA1234F1Z5 (VERIFIED)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF22C55E))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Purchaser Register ID:", fontSize = 11.sp, color = Color.Gray)
                    Text("AS-99214-IND", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Logistics Warehouse Area:", fontSize = 11.sp, color = Color.Gray)
                    Text("Pune West (Zone B)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Dedicated Scan QR authenticity verification sub-section inside profile!
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan icon", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Stock Originality QR Checker", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
                
                Text("Scan/check QR badge serial values printed on Asianstar drums to test authenticity on central logistics security directory ledger.", fontSize = 11.sp, color = Color.Gray)
                
                // Manual inputs for simulating scanner codes
                OutlinedTextField(
                    value = scannedProductCode,
                    onValueChange = onScannedCodeChange,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
                    placeholder = { Text("Enter serial (e.g., ASIAN-MAX-10W30)", fontSize = 12.sp, color = Color.Gray) },
                    singleLine = true
                )

                // Quick Sim helper row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .clickable { onScannedCodeChange("ASIAN-MAX-98214-X") }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Simulate Original", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .clickable { onScannedCodeChange("COUNTERFEIT-LOT-992") }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Simulate Counterfeit", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = onClear, modifier = Modifier.weight(1f)) {
                        Text("Reset Scanner", color = Color.Gray, fontSize = 11.sp)
                    }
                    Button(
                        onClick = { onVerify(scannedProductCode) },
                        modifier = Modifier.weight(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Check Authenticity", color = Color.White, fontSize = 11.sp)
                    }
                }

                if (scanVerifiedResult != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (scanVerifiedResult.contains("SECURE")) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
                        ),
                        border = BorderStroke(1.dp, if (scanVerifiedResult.contains("SECURE")) Color(0xFF22C55E).copy(alpha = 0.3f) else Color(0xFFEF4444).copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = scanVerifiedResult,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(10.dp),
                            color = if (scanVerifiedResult.contains("SECURE")) Color(0xFF065F46) else Color(0xFF991B1B)
                        )
                    }
                }
            }
        }

        // Contact Support help desk
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Direct Corporate Support Desk", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                Text("Queries regarding credits line settlement, volume rewards, or shipment delayed, talk directly to regional executive help desk.", fontSize = 11.sp, color = Color.Gray)
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { Toast.makeText(context, "Directing call dialer: 1800-ASIAN-LUB", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = "Call", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Speak on Call", color = Color.White, fontSize = 11.sp)
                    }
                    Button(
                        onClick = { Toast.makeText(context, "Drafting secure email: help@asianstar.com", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = "Email", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Email Helpdesk", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// Custom category helper matching top quality royalty image urls
fun getCategoryPromoImageUrl(category: String): String {
    return when {
        category.contains("2-Wheeler") -> "https://images.unsplash.com/photo-1558981806-ec527fa84c39?w=300&auto=format&fit=crop&q=60" 
        category.contains("3-Wheeler") -> "https://images.unsplash.com/photo-1566008889998-f5c401509122?w=300&auto=format&fit=crop&q=60" 
        category.contains("4-Wheeler") -> "https://images.unsplash.com/photo-1486006920555-c77dce18193b?w=300&auto=format&fit=crop&q=60" 
        category.contains("Coolant") -> "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?w=300&auto=format&fit=crop&q=60" 
        category.contains("Battery") -> "https://images.unsplash.com/photo-1595079676339-1534801ad6cf?w=300&auto=format&fit=crop&q=60" 
        category.contains("Petroleum") || category.contains("Base") -> "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=300&auto=format&fit=crop&q=60" 
        else -> "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?w=300&auto=format&fit=crop&q=60"
    }
}
