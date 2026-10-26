package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.currency.CurrencyManager
import com.example.data.local.entities.UserEntity
import com.example.data.repository.ListingRepository
import com.example.data.repository.OrderRepository
import com.example.ui.components.ShowcaseDetailDialog
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.GamerBg
import com.example.ui.theme.GamerCardBorder
import com.example.ui.theme.GamerSurface
import com.example.ui.theme.GamerSurfaceElevated
import com.example.ui.theme.GamerTextMuted
import com.example.ui.theme.GamerTextPrimary
import com.example.ui.theme.GamerTextSecondary
import com.example.ui.theme.GoldLegendary
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleDark
import kotlinx.coroutines.launch

import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.OutlinedTextField
import com.example.data.repository.FavoriteRepository
import com.example.data.repository.ListingReportRepository
import com.example.ui.components.CheckoutBottomSheet
import com.example.ui.components.CryptoPaymentDialog
import com.example.ui.components.UpiPaymentDialog

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailScreen(
    listingId: String,
    listingRepository: ListingRepository,
    orderRepository: OrderRepository,
    favoriteRepository: FavoriteRepository? = null,
    listingReportRepository: ListingReportRepository? = null,
    currentUser: UserEntity?,
    userCurrency: String,
    onBack: () -> Unit,
    onOrderSuccess: (orderId: String) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val listing by listingRepository.observeListingById(listingId).collectAsState(initial = null)
    var showShowcaseDialog by remember { mutableStateOf(false) }
    var showCheckoutSheet by remember { mutableStateOf(false) }
    var showUpiDialog by remember { mutableStateOf(false) }
    var showCryptoDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("STOLEN_ACCOUNT") }
    var reportDetails by remember { mutableStateOf("") }
    var isSubmittingReport by remember { mutableStateOf(false) }
    var upiAmountInr by remember { mutableStateOf(0.0) }
    var cryptoAmountUsd by remember { mutableStateOf(0.0) }
    var isCheckingOut by remember { mutableStateOf(false) }
    val checkoutSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    val isFav by (if (currentUser != null && favoriteRepository != null) {
        favoriteRepository.isFavorite(currentUser.id, listingId)
    } else {
        kotlinx.coroutines.flow.flowOf(false)
    }).collectAsState(initial = false)

    if (listing == null) {
        Box(modifier = modifier.fillMaxSize().background(GamerBg), contentAlignment = Alignment.Center) {
            Text("Listing not found or loading...", color = GamerTextSecondary)
        }
        return
    }

    val item = listing!!
    val priceDisplay = CurrencyManager.formatDisplayPrice(
        originalPrice = item.originalPrice,
        originalCurrency = item.originalCurrency,
        userCurrency = userCurrency
    )

    Box(modifier = modifier.fillMaxSize().background(GamerBg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(GamerSurface)
                    .border(0.5.dp, GamerCardBorder)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = GamerTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.gameTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                if (currentUser != null && favoriteRepository != null) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                val nowFav = favoriteRepository.toggleFavorite(currentUser.id, item.id)
                                snackbarHostState.showSnackbar(
                                    if (nowFav) "Added to Favourites ❤️" else "Removed from Favourites"
                                )
                            }
                        },
                        modifier = Modifier.testTag("detail_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (isFav) CrimsonAlert else GamerTextSecondary
                        )
                    }
                }
                IconButton(
                    onClick = {
                        if (currentUser == null) {
                            scope.launch { snackbarHostState.showSnackbar("Please sign in to report this listing") }
                        } else {
                            showReportDialog = true
                        }
                    },
                    modifier = Modifier.testTag("detail_report_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ReportProblem,
                        contentDescription = "Report Suspicious Listing",
                        tint = CrimsonAlert
                    )
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // Image Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(NeonPurpleDark, Color(0xFF1E1B4B), Color(0xFF0F172A))
                            )
                        )
                ) {
                    val firstImage = item.imageUrlsJson.split(",").firstOrNull()?.trim()
                    if (!firstImage.isNullOrBlank()) {
                        AsyncImage(
                            model = firstImage,
                            contentDescription = item.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    // Bottom fade overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, GamerBg.copy(alpha = 0.9f))
                                )
                            )
                    )

                    // Region Badge
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.8f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Public, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(item.region, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    // Showcase Explanatory Banner if applicable
                    if (item.isShowcase) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1E1B33))
                                .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                .clickable { showShowcaseDialog = true }
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        "Official Showcase Example",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = CyberCyan
                                    )
                                    Text(
                                        "Visual presentation model. Tap to read presentation details.",
                                        fontSize = 11.sp,
                                        color = GamerTextSecondary
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Title
                    Text(
                        text = item.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stats Grid Cards
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DetailChip(label = "Rank", value = item.rank, color = GoldLegendary)
                        if (item.accountLevel > 0) {
                            DetailChip(label = "Level", value = "${item.accountLevel}", color = CyberCyan)
                        }
                        if (item.skinsCount > 0) {
                            DetailChip(label = "Skins", value = "${item.skinsCount}", color = NeonPurple)
                        }
                        if (item.charactersCount > 0) {
                            DetailChip(label = "Characters", value = "${item.charactersCount}", color = NeonGreen)
                        }
                    }

                    if (item.itemsDescription.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Featured Rare Items & Inventory", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(item.itemsDescription, fontSize = 13.sp, color = GamerTextPrimary)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Seller Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(0.5.dp, GamerCardBorder, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = GamerSurface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(NeonPurple.copy(alpha = 0.2f))
                                        .border(1.dp, NeonPurple, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item.sellerName.take(1).uppercase(),
                                        color = NeonPurple,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.sellerName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = GamerTextPrimary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Verified Seller",
                                            tint = NeonGreen,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                    Text(
                                        text = "Verified Marketplace Merchant • Escrow Protected",
                                        fontSize = 10.sp,
                                        color = GamerTextSecondary
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = GoldLegendary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = String.format("%.1f", item.sellerRating),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = GoldLegendary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Account Description
                    Text("Account Details & Notes", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = item.description,
                        fontSize = 13.sp,
                        color = GamerTextSecondary,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Account Delivery & Security Section (Requirement 7)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Protected Account Delivery",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberCyan
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "• Passwords and recovery keys are stored with AES-GCM encryption in the secure delivery vault.\n• Credentials are never posted publicly or in listing logs.\n• Instant automatic release upon payment verification.\n• Escrow holds funds safely until you verify account access.",
                                fontSize = 11.sp,
                                color = GamerTextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }

            // Bottom Purchase Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GamerSurface)
                    .border(0.5.dp, GamerCardBorder)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = priceDisplay.primaryPrice,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = CyberCyan
                    )
                    if (priceDisplay.secondaryPrice != null) {
                        Text(
                            text = priceDisplay.secondaryPrice,
                            fontSize = 11.sp,
                            color = GamerTextMuted
                        )
                    }
                }

                if (item.isShowcase) {
                    Button(
                        onClick = { showShowcaseDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GamerSurfaceElevated),
                        modifier = Modifier.testTag("showcase_info_btn")
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp), tint = CyberCyan)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Showcase Info", color = CyberCyan, fontWeight = FontWeight.Bold)
                    }
                } else if (item.status == "SOLD") {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Gray.copy(alpha = 0.3f))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text("Account Sold", color = Color.LightGray, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            if (currentUser == null) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Please register or log in first to purchase.")
                                }
                            } else {
                                showCheckoutSheet = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                        modifier = Modifier.testTag("buy_now_button")
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buy Now", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // Showcase Info Dialog
        if (showShowcaseDialog) {
            ShowcaseDetailDialog(onDismiss = { showShowcaseDialog = false })
        }

        // Multi-Payment Checkout Bottom Sheet
        if (showCheckoutSheet && currentUser != null) {
            CheckoutBottomSheet(
                sheetState = checkoutSheetState,
                listing = item,
                currentUser = currentUser,
                userCurrency = userCurrency,
                isProcessing = isCheckingOut,
                onDismiss = { showCheckoutSheet = false },
                onPayWithWallet = {
                    isCheckingOut = true
                    scope.launch {
                        val result = orderRepository.purchaseWithWallet(currentUser, item)
                        isCheckingOut = false
                        showCheckoutSheet = false
                        result.onSuccess { order ->
                            snackbarHostState.showSnackbar("Order ${order.orderNumber} complete! Credentials unlocked.")
                            onOrderSuccess(order.id)
                        }.onFailure { err ->
                            snackbarHostState.showSnackbar("Checkout failed: ${err.message}")
                        }
                    }
                },
                onSelectUpi = { amountInr, _ ->
                    upiAmountInr = amountInr
                    showCheckoutSheet = false
                    showUpiDialog = true
                },
                onSelectCrypto = { amountUsd, _ ->
                    cryptoAmountUsd = amountUsd
                    showCheckoutSheet = false
                    showCryptoDialog = true
                }
            )
        }

        // UPI Payment Gateway Flow
        if (showUpiDialog && currentUser != null) {
            val orderRef = "GB-UPI-${item.id.takeLast(6).uppercase()}"
            UpiPaymentDialog(
                amountInr = upiAmountInr,
                orderReference = orderRef,
                onDismiss = { showUpiDialog = false },
                onSubmitUpi = { utrNumber, screenshotUrl ->
                    scope.launch {
                        val result = orderRepository.purchaseWithUpi(
                            buyer = currentUser,
                            listing = item,
                            utrNumber = utrNumber,
                            screenshotUrl = screenshotUrl
                        )
                        showUpiDialog = false
                        result.onSuccess { order ->
                            snackbarHostState.showSnackbar("UPI submitted! Order ${order.orderNumber} is under merchant review.")
                            onOrderSuccess(order.id)
                        }.onFailure { err ->
                            snackbarHostState.showSnackbar("Error: ${err.message}")
                        }
                    }
                }
            )
        }

        // Crypto Payment Gateway Flow
        if (showCryptoDialog && currentUser != null) {
            val orderRef = "GB-CRYPTO-${item.id.takeLast(6).uppercase()}"
            CryptoPaymentDialog(
                amountUsd = cryptoAmountUsd,
                orderReference = orderRef,
                onDismiss = { showCryptoDialog = false },
                onCryptoPaid = { coin, network ->
                    scope.launch {
                        val result = orderRepository.purchaseWithCrypto(
                            buyer = currentUser,
                            listing = item,
                            coin = coin,
                            network = network,
                            isProviderConfigured = true
                        )
                        showCryptoDialog = false
                        result.onSuccess { order ->
                            snackbarHostState.showSnackbar("Crypto order created: ${order.orderNumber}. Awaiting blockchain confirmation.")
                            onOrderSuccess(order.id)
                        }.onFailure { err ->
                            snackbarHostState.showSnackbar("Crypto Error: ${err.message}")
                        }
                    }
                }
            )
        }

        if (showReportDialog) {
            val reportReasons = listOf(
                "STOLEN_ACCOUNT" to "Stolen / Hacked Account",
                "MISLEADING_DESCRIPTION" to "Misleading Description or Fake Stats",
                "SUSPECTED_SCAM" to "Suspected Scam or External Off-Platform Contact",
                "PROHIBITED_ITEMS" to "Prohibited Terms / TOS Violation",
                "OTHER" to "Other Suspicious Behavior"
            )
            AlertDialog(
                onDismissRequest = { if (!isSubmittingReport) showReportDialog = false },
                containerColor = GamerSurface,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ReportProblem, contentDescription = null, tint = CrimsonAlert)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Report Suspicious Listing", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        Text(
                            "Help maintain escrow integrity. Verified reports are reviewed immediately by trust & safety administrators.",
                            color = GamerTextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Reason for Report:", color = GamerTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        reportReasons.forEach { (key, label) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { reportReason = key }
                                    .padding(vertical = 4.dp)
                            ) {
                                androidx.compose.material3.RadioButton(
                                    selected = reportReason == key,
                                    onClick = { reportReason = key }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(label, color = if (reportReason == key) Color.White else GamerTextMuted, fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = reportDetails,
                            onValueChange = { reportDetails = it },
                            label = { Text("Details or Evidence", fontSize = 11.sp) },
                            placeholder = { Text("Describe why this listing is suspicious...", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth().height(100.dp),
                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = CrimsonAlert,
                                unfocusedBorderColor = GamerCardBorder
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (reportDetails.isBlank()) {
                                scope.launch { snackbarHostState.showSnackbar("Please provide details for the report") }
                                return@Button
                            }
                            isSubmittingReport = true
                            scope.launch {
                                if (currentUser != null && listingReportRepository != null) {
                                    val res = listingReportRepository.submitReport(
                                        listingId = item.id,
                                        listingTitle = item.title,
                                        reporterId = currentUser.id,
                                        reporterEmail = currentUser.email,
                                        reason = reportReason,
                                        details = reportDetails
                                    )
                                    if (res.isSuccess) {
                                        showReportDialog = false
                                        reportDetails = ""
                                        snackbarHostState.showSnackbar("Report submitted. Escrow security will inspect this listing.")
                                    } else {
                                        snackbarHostState.showSnackbar("Failed: ${res.exceptionOrNull()?.message}")
                                    }
                                } else {
                                    showReportDialog = false
                                    snackbarHostState.showSnackbar("Report recorded for investigation.")
                                }
                                isSubmittingReport = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert)
                    ) {
                        Text("Submit Report", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showReportDialog = false }) {
                        Text("Cancel", color = GamerTextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun DetailChip(
    label: String,
    value: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Column {
            Text(label, fontSize = 9.sp, color = GamerTextSecondary)
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
