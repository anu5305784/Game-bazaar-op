package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.OrderEntity
import com.example.data.local.entities.SupportTicketEntity
import com.example.data.local.entities.UserEntity
import com.example.data.local.entities.WalletTransactionEntity
import com.example.data.repository.DecryptedDeliveryInfo
import com.example.data.repository.DeliveryRepository
import com.example.data.repository.FavoriteRepository
import com.example.data.repository.OrderRepository
import com.example.data.repository.SupportTicketRepository
import com.example.data.repository.WalletRepository
import com.example.ui.components.ListingCard
import com.example.ui.components.UpiPaymentDialog
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val BUYER_TABS = listOf("Orders", "Wallet Ledger", "Favourites", "Support & Disputes", "Notifications")

@Composable
fun BuyerDashboardScreen(
    currentUser: UserEntity,
    orderRepository: OrderRepository,
    walletRepository: WalletRepository,
    favoriteRepository: FavoriteRepository,
    supportTicketRepository: SupportTicketRepository,
    deliveryRepository: DeliveryRepository,
    onViewListingDetail: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTabIndex by remember { mutableStateOf(0) }

    val buyerOrders by orderRepository.getOrdersForBuyer(currentUser.id).collectAsState(initial = emptyList())
    val transactions by walletRepository.observeTransactions(currentUser.id).collectAsState(initial = emptyList())
    val favoriteListings by favoriteRepository.observeFavoriteListings(currentUser.id).collectAsState(initial = emptyList())
    val tickets by supportTicketRepository.observeTicketsForUser(currentUser.id).collectAsState(initial = emptyList())

    // Dialogs state
    var unlockedVaultInfo by remember { mutableStateOf<DecryptedDeliveryInfo?>(null) }
    var orderToDispute by remember { mutableStateOf<OrderEntity?>(null) }
    var disputeReasonInput by remember { mutableStateOf("") }
    var showDepositDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var showNewTicketDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(GamerBg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Buyer Dashboard", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Text("Orders, transaction ledger, escrow vaults & disputes", fontSize = 11.sp, color = GamerTextSecondary)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E1B33))
                            .border(1.dp, GamerCardBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${currentUser.preferredCurrency} ${String.format("%.2f", currentUser.walletBalance)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen
                        )
                    }
                }
            }

            // Tabs Row
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = GamerBg,
                contentColor = NeonPurple,
                edgePadding = 16.dp,
                divider = {}
            ) {
                BUYER_TABS.forEachIndexed { index, tabName ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = tabName,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) NeonPurple else GamerTextSecondary
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Body according to tab
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> {
                        // 1. ORDERS TAB
                        if (buyerOrders.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    title = "No Orders Found",
                                    subtitle = "Purchased game accounts and payment references will appear here."
                                )
                            }
                        } else {
                            items(buyerOrders, key = { it.id }) { order ->
                                OrderCardView(
                                    order = order,
                                    onUnlockVault = {
                                        scope.launch {
                                            val result = deliveryRepository.getAuthorizedDeliveryInfo(
                                                listingId = order.listingId,
                                                requestingUserId = currentUser.id,
                                                requestingUserRole = currentUser.role
                                            )
                                            result.onSuccess { info ->
                                                unlockedVaultInfo = info
                                            }.onFailure { err ->
                                                snackbarHostState.showSnackbar("Cannot unlock: ${err.message}")
                                            }
                                        }
                                    },
                                    onOpenDispute = { orderToDispute = order },
                                    onViewListing = { onViewListingDetail(order.listingId) }
                                )
                            }
                        }
                    }

                    1 -> {
                        // 2. WALLET & IMMUTABLE LEDGER TAB
                        item {
                            WalletOverviewCard(
                                user = currentUser,
                                onDepositClick = { showDepositDialog = true },
                                onWithdrawClick = { showWithdrawDialog = true }
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Immutable Transaction Ledger", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                                Text("${transactions.size} records", fontSize = 11.sp, color = GamerTextMuted)
                            }
                        }

                        if (transactions.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    title = "Ledger is Empty",
                                    subtitle = "All wallet debits, credits, and refunds are permanently recorded here."
                                )
                            }
                        } else {
                            items(transactions, key = { it.id }) { txn ->
                                TransactionLedgerRow(txn = txn)
                            }
                        }
                    }

                    2 -> {
                        // 3. FAVOURITES TAB
                        if (favoriteListings.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    title = "No Bookmarked Listings",
                                    subtitle = "Tap the heart icon on any account card to save it here."
                                )
                            }
                        } else {
                            items(favoriteListings, key = { it.id }) { listing ->
                                ListingCard(
                                    listing = listing,
                                    userCurrency = currentUser.preferredCurrency,
                                    onClick = { onViewListingDetail(listing.id) }
                                )
                            }
                        }
                    }

                    3 -> {
                        // 4. SUPPORT TICKETS & DISPUTES TAB
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Support & Dispute Arbitration", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Button(
                                    onClick = { showNewTicketDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("New Ticket", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (tickets.isEmpty()) {
                            item {
                                EmptyStateCard(
                                    title = "No Support Tickets",
                                    subtitle = "Need help with account access or payment dispute? Open a ticket above."
                                )
                            }
                        } else {
                            items(tickets, key = { it.id }) { ticket ->
                                TicketCardView(ticket = ticket)
                            }
                        }
                    }

                    4 -> {
                        // 5. NOTIFICATIONS TAB
                        item {
                            NotificationCenterView()
                        }
                    }
                }
            }
        }

        // Unlocked Decrypted Vault Modal
        if (unlockedVaultInfo != null) {
            val vault = unlockedVaultInfo!!
            AlertDialog(
                onDismissRequest = { unlockedVaultInfo = null },
                containerColor = GamerSurface,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = NeonGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Decrypted Account Credentials", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                text = {
                    Column {
                        Text("Transfer Method: ${vault.transferMethod}", fontWeight = FontWeight.Bold, color = CyberCyan, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Credentials (AES-GCM Decrypted):", fontSize = 11.sp, color = GamerTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0F172A))
                                .border(1.dp, GamerCardBorder, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(vault.credentials, color = Color.White, fontSize = 11.sp, lineHeight = 16.sp)
                        }

                        if (vault.instructions.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Delivery Instructions: ${vault.instructions}", fontSize = 11.sp, color = GamerTextSecondary)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Credentials", vault.credentials))
                            scope.launch { snackbarHostState.showSnackbar("Credentials copied to clipboard!") }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy Credentials", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { unlockedVaultInfo = null }) {
                        Text("Close", color = GamerTextSecondary)
                    }
                }
            )
        }

        // Dispute Creation Modal
        if (orderToDispute != null) {
            val order = orderToDispute!!
            AlertDialog(
                onDismissRequest = { orderToDispute = null },
                containerColor = GamerSurface,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ReportProblem, contentDescription = null, tint = CrimsonAlert)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Escrow Dispute", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                text = {
                    Column {
                        Text("Order: ${order.orderNumber} (${order.gameTitle})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Gamer Bazaar Escrow holds funds safely. If the seller provided invalid credentials or account access issues arise, state your dispute details below for arbitration.",
                            fontSize = 11.sp,
                            color = GamerTextSecondary,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = disputeReasonInput,
                            onValueChange = { disputeReasonInput = it },
                            label = { Text("Dispute Details & Evidence") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = GamerTextPrimary,
                                unfocusedTextColor = GamerTextPrimary,
                                focusedBorderColor = CrimsonAlert,
                                unfocusedBorderColor = GamerCardBorder
                            ),
                            maxLines = 4
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                val result = orderRepository.openDispute(order.id, currentUser.id, disputeReasonInput)
                                result.onSuccess {
                                    snackbarHostState.showSnackbar("Dispute opened. Support team notified.")
                                    orderToDispute = null
                                    disputeReasonInput = ""
                                }.onFailure { err ->
                                    snackbarHostState.showSnackbar("Error: ${err.message}")
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert)
                    ) {
                        Text("Submit Dispute", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { orderToDispute = null }) {
                        Text("Cancel", color = GamerTextSecondary)
                    }
                }
            )
        }

        // Add Money UPI Modal
        if (showDepositDialog) {
            UpiPaymentDialog(
                amountInr = 1000.0,
                orderReference = "TOPUP-${currentUser.id.takeLast(4)}",
                onDismiss = { showDepositDialog = false },
                onSubmitUpi = { utr, screenshot ->
                    scope.launch {
                        val result = walletRepository.submitUpiDeposit(
                            userId = currentUser.id,
                            amountInr = 1000.0,
                            utrNumber = utr,
                            screenshotUrl = screenshot
                        )
                        showDepositDialog = false
                        result.onSuccess {
                            snackbarHostState.showSnackbar("UPI deposit submitted for merchant verification.")
                        }.onFailure { err ->
                            snackbarHostState.showSnackbar("Error: ${err.message}")
                        }
                    }
                }
            )
        }

        // Withdraw Modal
        if (showWithdrawDialog) {
            WithdrawFundsDialog(
                user = currentUser,
                onDismiss = { showWithdrawDialog = false },
                onConfirmWithdraw = { amount, destination ->
                    scope.launch {
                        val result = walletRepository.withdrawFunds(currentUser.id, amount, destination)
                        showWithdrawDialog = false
                        result.onSuccess {
                            snackbarHostState.showSnackbar("Withdrawal processed successfully!")
                        }.onFailure { err ->
                            snackbarHostState.showSnackbar("Withdrawal failed: ${err.message}")
                        }
                    }
                }
            )
        }

        // New Ticket Modal
        if (showNewTicketDialog) {
            NewSupportTicketDialog(
                user = currentUser,
                onDismiss = { showNewTicketDialog = false },
                onSubmit = { subject, message, cat ->
                    scope.launch {
                        supportTicketRepository.createTicket(
                            userId = currentUser.id,
                            userEmail = currentUser.email,
                            subject = subject,
                            message = message,
                            category = cat
                        )
                        showNewTicketDialog = false
                        snackbarHostState.showSnackbar("Support ticket submitted!")
                    }
                }
            )
        }
    }
}

@Composable
private fun OrderCardView(
    order: OrderEntity,
    onUnlockVault: () -> Unit,
    onOpenDispute: () -> Unit,
    onViewListing: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(0.5.dp, GamerCardBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = GamerSurface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Order Number & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.orderNumber.ifBlank { "GB-${order.id.takeLast(6)}" },
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = CyberCyan
                )

                // Status Badge mapping
                val (badgeBg, badgeText) = getOrderStatusColors(order.status)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeBg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(order.status, fontSize = 9.sp, fontWeight = FontWeight.Black, color = badgeText)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = order.listingTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = GamerTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${order.currency} ${order.amount}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonGreen
                )
                Text(
                    text = "Method: ${order.paymentMethod}",
                    fontSize = 11.sp,
                    color = GamerTextSecondary
                )
            }

            if (order.transactionReference.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Ref: ${order.transactionReference}",
                    fontSize = 10.sp,
                    color = GamerTextMuted
                )
            }

            if (order.disputeReason.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(CrimsonAlert.copy(alpha = 0.15f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Dispute: ${order.disputeReason}",
                        fontSize = 10.sp,
                        color = CrimsonAlert,
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (order.status == "Delivered" || order.paymentStatus == "PAID") {
                    Button(
                        onClick = onUnlockVault,
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delivery Vault", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (order.status != "Refunded" && order.disputeStatus != "OPEN") {
                    OutlinedButton(
                        onClick = onOpenDispute,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonAlert),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Dispute", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun WalletOverviewCard(
    user: UserEntity,
    onDepositClick: () -> Unit,
    onWithdrawClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, GamerCardBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = GamerSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Gamer Bazaar Escrow Wallet", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Available Balance", fontSize = 11.sp, color = GamerTextSecondary)
                    Text(
                        "${user.preferredCurrency} ${String.format("%.2f", user.walletBalance)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = NeonGreen
                    )
                }

                if (user.pendingBalance > 0.0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Pending Verification", fontSize = 11.sp, color = GoldLegendary)
                        Text(
                            "${user.preferredCurrency} ${String.format("%.2f", user.pendingBalance)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldLegendary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onDepositClick,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Add Money (UPI)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onWithdrawClick,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberCyan),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Withdraw", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TransactionLedgerRow(txn: WalletTransactionEntity) {
    val isCredit = txn.type.contains("CREDIT") || txn.type.contains("DEPOSIT")
    val displayColor = when {
        txn.status == "REJECTED" -> CrimsonAlert
        txn.status == "UNDER_REVIEW" -> GoldLegendary
        isCredit -> NeonGreen
        else -> Color.White
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(0.5.dp, GamerCardBorder, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = GamerSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(txn.description, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = GamerTextPrimary)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Ref: ${txn.id} • ${txn.status}", fontSize = 10.sp, color = GamerTextMuted)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isCredit) "+" else "-"}${txn.currency} ${String.format("%.2f", txn.amount)}",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = displayColor
                )
                Text(
                    text = "Bal: ${txn.currency} ${String.format("%.2f", txn.balanceAfter)}",
                    fontSize = 9.sp,
                    color = GamerTextMuted
                )
            }
        }
    }
}

@Composable
private fun TicketCardView(ticket: SupportTicketEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(0.5.dp, GamerCardBorder, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = GamerSurface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(ticket.subject, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                val badgeColor = if (ticket.status == "RESOLVED") NeonGreen else GoldLegendary
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(badgeColor.copy(alpha = 0.2f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(ticket.status, fontSize = 9.sp, fontWeight = FontWeight.Black, color = badgeColor)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("Inquiry: ${ticket.message}", fontSize = 11.sp, color = GamerTextSecondary)

            if (ticket.adminResponse.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1E1B33))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Admin Reply: ${ticket.adminResponse}",
                        fontSize = 11.sp,
                        color = CyberCyan
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationCenterView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.DoneAll, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.height(6.dp))
        Text("Notifications Synced", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
        Text("Real-time notifications sent for every verified payment, delivery release, and dispute update.", fontSize = 11.sp, color = GamerTextSecondary)
    }
}

@Composable
private fun EmptyStateCard(title: String, subtitle: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = GamerSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = GamerTextMuted, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, fontSize = 11.sp, color = GamerTextSecondary)
        }
    }
}

@Composable
private fun WithdrawFundsDialog(
    user: UserEntity,
    onDismiss: () -> Unit,
    onConfirmWithdraw: (amount: Double, destination: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var destinationText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GamerSurface,
        title = { Text("Withdraw Funds", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Available for withdrawal: ${user.preferredCurrency} ${String.format("%.2f", user.walletBalance)}",
                    fontSize = 12.sp,
                    color = NeonGreen
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Amount (${user.preferredCurrency})") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GamerTextPrimary,
                        unfocusedTextColor = GamerTextPrimary,
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = GamerCardBorder
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = destinationText,
                    onValueChange = { destinationText = it },
                    label = { Text("Destination (UPI ID, Bank Account, or Crypto Address)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GamerTextPrimary,
                        unfocusedTextColor = GamerTextPrimary,
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = GamerCardBorder
                    ),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0.0 && destinationText.isNotBlank()) {
                        onConfirmWithdraw(amount, destinationText.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
            ) {
                Text("Confirm Withdrawal", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel", color = GamerTextSecondary) }
        }
    )
}

@Composable
private fun NewSupportTicketDialog(
    user: UserEntity,
    onDismiss: () -> Unit,
    onSubmit: (subject: String, message: String, category: String) -> Unit
) {
    var subject by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("GENERAL") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GamerSurface,
        title = { Text("Create Support Ticket", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GamerTextPrimary,
                        unfocusedTextColor = GamerTextPrimary,
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = GamerCardBorder
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text("Describe the issue or inquiry") },
                    modifier = Modifier.height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GamerTextPrimary,
                        unfocusedTextColor = GamerTextPrimary,
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = GamerCardBorder
                    ),
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (subject.isNotBlank() && message.isNotBlank()) {
                        onSubmit(subject.trim(), message.trim(), category)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
            ) {
                Text("Submit Ticket", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel", color = GamerTextSecondary) }
        }
    )
}

private fun getOrderStatusColors(status: String): Pair<Color, Color> {
    return when (status) {
        "Paid" -> Color(0xFF064E3B) to Color(0xFF34D399)
        "Payment Under Review" -> Color(0xFF0C4A6E) to Color(0xFF38BDF8)
        "Pending Payment" -> Color(0xFF78350F) to Color(0xFFFBBF24)
        "Processing" -> Color(0xFF4C1D95) to Color(0xFFC084FC)
        "Delivered" -> Color(0xFF064E3B) to Color(0xFF10B981)
        "Refunded" -> Color(0xFF312E81) to Color(0xFF818CF8)
        "Disputed" -> Color(0xFF7F1D1D) to Color(0xFFF87171)
        else -> Color.DarkGray to Color.LightGray
    }
}
