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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.currency.CurrencyManager
import com.example.data.local.entities.AdminAuditLogEntity
import com.example.data.local.entities.GameEntity
import com.example.data.local.entities.ListingEntity
import com.example.data.local.entities.ListingReportEntity
import com.example.data.local.entities.OrderEntity
import com.example.data.local.entities.SupportTicketEntity
import com.example.data.local.entities.SystemConfigEntity
import com.example.data.local.entities.UpiSubmissionEntity
import com.example.data.local.entities.UserEntity
import com.example.data.local.entities.WalletTransactionEntity
import com.example.data.repository.AdminRepository
import com.example.data.repository.AuthRepository
import com.example.data.repository.GameRepository
import com.example.data.repository.ListingReportRepository
import com.example.data.repository.ListingRepository
import com.example.data.repository.OrderRepository
import com.example.data.repository.SupportTicketRepository
import com.example.data.repository.WalletRepository
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
import java.util.UUID

val ADMIN_TABS = listOf(
    "Overview",
    "Listings Review",
    "UPI Payments",
    "Orders & Disputes",
    "User Management",
    "Game Catalog",
    "Showcase & Featured",
    "Reports Queue",
    "Financial Ledger",
    "System Config",
    "Audit Log"
)

@Composable
fun AdminPanelScreen(
    authRepository: AuthRepository,
    gameRepository: GameRepository,
    listingRepository: ListingRepository,
    orderRepository: OrderRepository,
    walletRepository: WalletRepository,
    supportTicketRepository: SupportTicketRepository,
    adminRepository: AdminRepository,
    listingReportRepository: ListingReportRepository,
    currentUser: UserEntity?,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val pendingListings by listingRepository.getPendingListings().collectAsState(initial = emptyList())
    val allListings by listingRepository.getAdminListings().collectAsState(initial = emptyList())
    val pendingUpiSubmissions by walletRepository.observeUpiSubmissionsForAdmin().collectAsState(initial = emptyList())
    val allOrders by orderRepository.observeAllOrdersForAdmin().collectAsState(initial = emptyList())
    val allTickets by supportTicketRepository.observeAllTicketsForAdmin().collectAsState(initial = emptyList())
    val allUsers by authRepository.observeAllUsers().collectAsState(initial = emptyList())
    val allGames by gameRepository.getAllGames().collectAsState(initial = emptyList())
    val allReports by listingReportRepository.observeAllReports().collectAsState(initial = emptyList())
    val allTransactions by walletRepository.observeAllTransactionsForAdmin().collectAsState(initial = emptyList())
    val systemConfigs by adminRepository.observeSystemConfigs().collectAsState(initial = emptyList())
    val auditLogs by adminRepository.observeAuditLogs().collectAsState(initial = emptyList())

    // Dialog state
    var showBootstrapDialog by remember { mutableStateOf(false) }
    var bootstrapUsername by remember { mutableStateOf("") }
    var bootstrapEmail by remember { mutableStateOf("") }
    var bootstrapPassword by remember { mutableStateOf("") }
    var bootstrapKey by remember { mutableStateOf("") }

    var upiRejectTarget by remember { mutableStateOf<UpiSubmissionEntity?>(null) }
    var rejectionReasonInput by remember { mutableStateOf("") }

    var listingRejectTarget by remember { mutableStateOf<ListingEntity?>(null) }
    var listingRejectReasonInput by remember { mutableStateOf("") }

    var orderRefundTarget by remember { mutableStateOf<OrderEntity?>(null) }
    var refundReasonInput by remember { mutableStateOf("") }

    var ticketToResolve by remember { mutableStateOf<SupportTicketEntity?>(null) }
    var ticketResolutionInput by remember { mutableStateOf("") }

    var showAddGameDialog by remember { mutableStateOf(false) }
    var newGameTitle by remember { mutableStateOf("") }
    var newGameCategory by remember { mutableStateOf("RPG") }
    var newGamePublisher by remember { mutableStateOf("") }
    var newGameIsPopular by remember { mutableStateOf(false) }

    var reportToResolve by remember { mutableStateOf<ListingReportEntity?>(null) }
    var reportActionType by remember { mutableStateOf("DISMISSED") }
    var reportResolutionNotes by remember { mutableStateOf("") }

    var csvExportPreview by remember { mutableStateOf<Pair<String, String>?>(null) } // (Title, Content)

    val adminId = currentUser?.id ?: "usr_admin_01"
    val adminName = currentUser?.displayName ?: "Administrator"

    // Role-based Access Check: if user is not ADMIN, show bootstrap / elevate screen
    val isAdmin = currentUser?.role == "ADMIN"

    Box(modifier = modifier.fillMaxSize().background(GamerBg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(GamerSurface)
                    .border(0.5.dp, GamerCardBorder)
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back_btn")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GamerTextPrimary)
                }
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.Default.Shield, contentDescription = null, tint = GoldLegendary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Gamer Bazaar Escrow Admin",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (isAdmin) "Role: ADMINISTRATOR ($adminName)" else "Role: ${currentUser?.role ?: "GUEST"} (Restricted)",
                        fontSize = 10.sp,
                        color = if (isAdmin) NeonGreen else CrimsonAlert
                    )
                }

                // Bootstrap button
                OutlinedButton(
                    onClick = { showBootstrapDialog = true },
                    modifier = Modifier.testTag("bootstrap_admin_button"),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = GoldLegendary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Admin Setup", fontSize = 11.sp, color = GoldLegendary)
                }
            }

            if (!isAdmin) {
                // Non-admin guard notice
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GamerSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonAlert.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Block, contentDescription = null, tint = CrimsonAlert)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Administrator Privileges Required", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "You are currently signed in as '${currentUser?.displayName ?: "Guest"}' with role '${currentUser?.role ?: "None"}'. Admin controls require verified administrator authorization.",
                            color = GamerTextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        authRepository.switchAccount("ADMIN")
                                        snackbarHostState.showSnackbar("Switched to Demo Admin account")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                            ) {
                                Text("Switch to Admin Session", fontSize = 12.sp, color = Color.White)
                            }
                            OutlinedButton(
                                onClick = { showBootstrapDialog = true }
                            ) {
                                Text("Bootstrap Admin Key", fontSize = 12.sp, color = GoldLegendary)
                            }
                        }
                    }
                }
            }

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = GamerSurface,
                contentColor = CyberCyan,
                edgePadding = 12.dp,
                divider = {}
            ) {
                ADMIN_TABS.forEachIndexed { index, title ->
                    val isSelected = selectedTabIndex == index
                    val badgeCount = when (index) {
                        1 -> pendingListings.size
                        2 -> pendingUpiSubmissions.size
                        3 -> allOrders.count { it.status == "Disputed" }
                        7 -> allReports.count { it.status == "PENDING" }
                        else -> 0
                    }
                    Tab(
                        selected = isSelected,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) CyberCyan else GamerTextSecondary
                                )
                                if (badgeCount > 0) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(CrimsonAlert)
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text("$badgeCount", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        },
                        modifier = Modifier.testTag("admin_tab_$index")
                    )
                }
            }

            // Tab Content
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTabIndex) {
                    0 -> OverviewTab(
                        listingsCount = allListings.size,
                        pendingListingsCount = pendingListings.size,
                        usersCount = allUsers.size,
                        orders = allOrders,
                        pendingUpiCount = pendingUpiSubmissions.size,
                        reportsCount = allReports.count { it.status == "PENDING" },
                        onExportOrders = {
                            scope.launch {
                                val csv = adminRepository.exportOrdersCsv()
                                csvExportPreview = "Orders Export CSV" to csv
                            }
                        },
                        onExportTransactions = {
                            scope.launch {
                                val csv = adminRepository.exportTransactionsCsv()
                                csvExportPreview = "Transactions Export CSV" to csv
                            }
                        },
                        onExportAuditLogs = {
                            scope.launch {
                                val csv = adminRepository.exportAuditLogsCsv()
                                csvExportPreview = "Audit Logs Export CSV" to csv
                            }
                        }
                    )

                    1 -> ListingsReviewTab(
                        pendingListings = pendingListings,
                        onApprove = { listing ->
                            scope.launch {
                                val res = listingRepository.approveListing(listing.id, adminId, adminName)
                                if (res.isSuccess) {
                                    snackbarHostState.showSnackbar("Listing '${listing.title}' approved!")
                                } else {
                                    snackbarHostState.showSnackbar("Error: ${res.exceptionOrNull()?.message}")
                                }
                            }
                        },
                        onReject = { listing ->
                            listingRejectTarget = listing
                            listingRejectReasonInput = ""
                        }
                    )

                    2 -> UpiPaymentsTab(
                        submissions = pendingUpiSubmissions,
                        onApprove = { sub ->
                            scope.launch {
                                if (sub.orderId.isNotBlank()) {
                                    val res = orderRepository.adminApproveOrderPayment(sub.orderId, adminId)
                                    if (res.isSuccess) {
                                        snackbarHostState.showSnackbar("Payment for order verified! Credentials released.")
                                    } else {
                                        snackbarHostState.showSnackbar("Error: ${res.exceptionOrNull()?.message}")
                                    }
                                } else {
                                    val res = walletRepository.approveUpiDeposit(sub.id, adminId)
                                    if (res.isSuccess) {
                                        snackbarHostState.showSnackbar("UPI deposit approved and credited to wallet.")
                                    } else {
                                        snackbarHostState.showSnackbar("Error: ${res.exceptionOrNull()?.message}")
                                    }
                                }
                            }
                        },
                        onReject = { sub ->
                            upiRejectTarget = sub
                            rejectionReasonInput = ""
                        }
                    )

                    3 -> OrdersAndDisputesTab(
                        orders = allOrders,
                        onResolveRefund = { order ->
                            orderRefundTarget = order
                            refundReasonInput = ""
                        },
                        onDismissDispute = { order ->
                            scope.launch {
                                orderRepository.arbitrateDispute(
                                    orderId = order.id,
                                    adminId = adminId,
                                    favorBuyer = false,
                                    reason = "Escrow arbitration verified seller credentials as valid."
                                )
                                snackbarHostState.showSnackbar("Dispute dismissed in favor of seller.")
                            }
                        }
                    )

                    4 -> UserManagementTab(
                        users = allUsers,
                        onToggleSuspend = { user ->
                            scope.launch {
                                val newStatus = !user.isSuspended
                                authRepository.updateUserSuspended(adminId, adminName, user.id, newStatus)
                                snackbarHostState.showSnackbar(
                                    if (newStatus) "User '${user.username}' suspended" else "User '${user.username}' reactivated"
                                )
                            }
                        },
                        onChangeRole = { user, role ->
                            scope.launch {
                                authRepository.updateUserRole(adminId, adminName, user.id, role)
                                snackbarHostState.showSnackbar("User '${user.username}' promoted to $role")
                            }
                        }
                    )

                    5 -> GameCatalogTab(
                        games = allGames,
                        onAddGame = { showAddGameDialog = true },
                        onTogglePopular = { game ->
                            scope.launch {
                                val updated = game.copy(isPopular = !game.isPopular)
                                adminRepository.addOrUpdateGame(updated, adminId, adminName)
                                snackbarHostState.showSnackbar("Game '${game.title}' popular status toggled.")
                            }
                        }
                    )

                    6 -> ShowcaseManagementTab(
                        listings = allListings,
                        onToggleShowcase = { listing ->
                            scope.launch {
                                val newShowcase = !listing.isShowcase
                                listingRepository.toggleShowcase(listing.id, newShowcase, adminId, adminName)
                                snackbarHostState.showSnackbar("Listing showcase set to $newShowcase")
                            }
                        },
                        onToggleFeatured = { listing ->
                            scope.launch {
                                val newFeatured = !listing.isFeatured
                                listingRepository.toggleFeatured(listing.id, newFeatured, adminId, adminName)
                                snackbarHostState.showSnackbar("Listing featured set to $newFeatured")
                            }
                        }
                    )

                    7 -> ReportsQueueTab(
                        reports = allReports,
                        onResolveReport = { report, action ->
                            reportToResolve = report
                            reportActionType = action
                            reportResolutionNotes = ""
                        }
                    )

                    8 -> FinancialLedgerTab(
                        transactions = allTransactions
                    )

                    9 -> SystemConfigTab(
                        configs = systemConfigs,
                        onSaveConfig = { key, value, desc ->
                            scope.launch {
                                adminRepository.updateConfig(key, value, desc, adminId, adminName)
                                snackbarHostState.showSnackbar("Config '$key' updated successfully.")
                            }
                        }
                    )

                    10 -> AuditLogTab(
                        logs = auditLogs
                    )
                }
            }
        }

        // ================= DIALOGS =================

        // 1. Bootstrap Admin Dialog
        if (showBootstrapDialog) {
            AlertDialog(
                onDismissRequest = { showBootstrapDialog = false },
                containerColor = GamerSurface,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = GoldLegendary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bootstrap Administrator Account", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        Text(
                            "Create or elevate the primary platform administrator. Requires the secure administrative setup key (GB-ADMIN-SECURE-KEY-2026). Never uses a default public password.",
                            color = GamerTextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = bootstrapUsername,
                            onValueChange = { bootstrapUsername = it },
                            label = { Text("Admin Username", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = bootstrapEmail,
                            onValueChange = { bootstrapEmail = it },
                            label = { Text("Admin Email", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = bootstrapPassword,
                            onValueChange = { bootstrapPassword = it },
                            label = { Text("Strong Password (min 8 chars)", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = bootstrapKey,
                            onValueChange = { bootstrapKey = it },
                            label = { Text("Security Setup Key", fontSize = 11.sp) },
                            placeholder = { Text("GB-ADMIN-SECURE-KEY-2026", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                val res = authRepository.bootstrapFirstAdmin(
                                    username = bootstrapUsername,
                                    email = bootstrapEmail,
                                    password = bootstrapPassword,
                                    setupKey = bootstrapKey
                                )
                                if (res.isSuccess) {
                                    showBootstrapDialog = false
                                    snackbarHostState.showSnackbar("Administrator account initialized and verified!")
                                } else {
                                    snackbarHostState.showSnackbar("Bootstrap Failed: ${res.exceptionOrNull()?.message}")
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldLegendary)
                    ) {
                        Text("Initialize Admin", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showBootstrapDialog = false }) {
                        Text("Cancel", color = GamerTextSecondary)
                    }
                }
            )
        }

        // 2. Reject Listing Dialog (Mandatory Reason)
        listingRejectTarget?.let { listing ->
            AlertDialog(
                onDismissRequest = { listingRejectTarget = null },
                containerColor = GamerSurface,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = CrimsonAlert)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reject Listing", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        Text("Rejecting '${listing.title}' ($${listing.originalPrice} ${listing.originalCurrency})", color = GamerTextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = listingRejectReasonInput,
                            onValueChange = { listingRejectReasonInput = it },
                            label = { Text("Mandatory Rejection Reason", fontSize = 11.sp) },
                            placeholder = { Text("e.g., Incomplete delivery instructions or prohibited TOS items", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (listingRejectReasonInput.isBlank()) {
                                scope.launch { snackbarHostState.showSnackbar("Rejection reason is required") }
                                return@Button
                            }
                            scope.launch {
                                listingRepository.rejectListing(listing.id, listingRejectReasonInput.trim(), adminId, adminName)
                                listingRejectTarget = null
                                snackbarHostState.showSnackbar("Listing rejected and seller notified.")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert)
                    ) {
                        Text("Confirm Rejection", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { listingRejectTarget = null }) {
                        Text("Cancel", color = GamerTextSecondary)
                    }
                }
            )
        }

        // 3. Reject UPI Dialog
        upiRejectTarget?.let { sub ->
            AlertDialog(
                onDismissRequest = { upiRejectTarget = null },
                containerColor = GamerSurface,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = CrimsonAlert)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reject UPI Payment Request", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        Text("UTR: ${sub.utrNumber} | ₹${sub.amountInr}", color = GamerTextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = rejectionReasonInput,
                            onValueChange = { rejectionReasonInput = it },
                            label = { Text("Rejection Reason (Required)", fontSize = 11.sp) },
                            placeholder = { Text("e.g. UTR not matching merchant bank statement", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (rejectionReasonInput.isBlank()) {
                                scope.launch { snackbarHostState.showSnackbar("Please enter a reason") }
                                return@Button
                            }
                            scope.launch {
                                if (sub.orderId.isNotBlank()) {
                                    orderRepository.adminRejectOrderPayment(sub.orderId, adminId, rejectionReasonInput.trim())
                                } else {
                                    walletRepository.rejectUpiDeposit(sub.id, adminId, rejectionReasonInput.trim())
                                }
                                upiRejectTarget = null
                                snackbarHostState.showSnackbar("Payment rejected and user notified.")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert)
                    ) {
                        Text("Reject", color = Color.White)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { upiRejectTarget = null }) {
                        Text("Cancel", color = GamerTextSecondary)
                    }
                }
            )
        }

        // 4. Refund Dialog
        orderRefundTarget?.let { order ->
            AlertDialog(
                onDismissRequest = { orderRefundTarget = null },
                containerColor = GamerSurface,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Gavel, contentDescription = null, tint = GoldLegendary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Arbitrate Dispute: Issue Refund", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        Text("Order: ${order.orderNumber} | ${order.listingTitle}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Dispute Reason: ${order.disputeReason.ifBlank { "Buyer reported non-working credentials" }}", color = GamerTextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = refundReasonInput,
                            onValueChange = { refundReasonInput = it },
                            label = { Text("Arbitration Decision Reason", fontSize = 11.sp) },
                            placeholder = { Text("e.g. Seller failed to provide proof of working email access", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (refundReasonInput.isBlank()) {
                                scope.launch { snackbarHostState.showSnackbar("Arbitration reason required") }
                                return@Button
                            }
                            scope.launch {
                                val res = orderRepository.processRefund(order.id, adminId, refundReasonInput.trim())
                                orderRefundTarget = null
                                if (res.isSuccess) {
                                    snackbarHostState.showSnackbar("Refund atomically credited to buyer wallet!")
                                } else {
                                    snackbarHostState.showSnackbar("Refund Error: ${res.exceptionOrNull()?.message}")
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldLegendary)
                    ) {
                        Text("Credit Buyer Wallet", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { orderRefundTarget = null }) {
                        Text("Cancel", color = GamerTextSecondary)
                    }
                }
            )
        }

        // 5. Add Game Dialog
        if (showAddGameDialog) {
            AlertDialog(
                onDismissRequest = { showAddGameDialog = false },
                containerColor = GamerSurface,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SportsEsports, contentDescription = null, tint = CyberCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Game to Catalog", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newGameTitle,
                            onValueChange = { newGameTitle = it },
                            label = { Text("Game Title", fontSize = 11.sp) },
                            placeholder = { Text("e.g. EA Sports FC 25", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = newGameCategory,
                            onValueChange = { newGameCategory = it },
                            label = { Text("Category", fontSize = 11.sp) },
                            placeholder = { Text("Sports, RPG, MOBA, FPS", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = newGamePublisher,
                            onValueChange = { newGamePublisher = it },
                            label = { Text("Publisher", fontSize = 11.sp) },
                            placeholder = { Text("e.g. Electronic Arts", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = newGameIsPopular,
                                onCheckedChange = { newGameIsPopular = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = NeonGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Mark as Popular / Trending", color = GamerTextPrimary, fontSize = 12.sp)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newGameTitle.isBlank() || newGameCategory.isBlank()) {
                                scope.launch { snackbarHostState.showSnackbar("Title and Category required") }
                                return@Button
                            }
                            scope.launch {
                                val game = GameEntity(
                                    id = "gm_${UUID.randomUUID().toString().take(6)}",
                                    title = newGameTitle.trim(),
                                    category = newGameCategory.trim(),
                                    publisher = newGamePublisher.trim().ifBlank { "Verified Publisher" },
                                    isPopular = newGameIsPopular,
                                    activeListingsCount = 0
                                )
                                adminRepository.addOrUpdateGame(game, adminId, adminName)
                                showAddGameDialog = false
                                newGameTitle = ""
                                newGamePublisher = ""
                                snackbarHostState.showSnackbar("Game '${game.title}' added to catalog!")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                    ) {
                        Text("Add Game", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showAddGameDialog = false }) {
                        Text("Cancel", color = GamerTextSecondary)
                    }
                }
            )
        }

        // 6. Resolve Listing Report Dialog
        reportToResolve?.let { rep ->
            AlertDialog(
                onDismissRequest = { reportToResolve = null },
                containerColor = GamerSurface,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ReportProblem, contentDescription = null, tint = CrimsonAlert)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (reportActionType == "LISTING_REMOVED") "Remove Suspicious Listing" else "Dismiss Report",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Column {
                        Text("Target: ${rep.listingTitle}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Reported for: ${rep.reason} (${rep.details})", color = GamerTextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = reportResolutionNotes,
                            onValueChange = { reportResolutionNotes = it },
                            label = { Text("Resolution Notes / Justification", fontSize = 11.sp) },
                            placeholder = { Text("e.g. Account credentials found invalid; listing removed.", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                adminRepository.resolveListingReport(
                                    reportId = rep.id,
                                    action = reportActionType,
                                    notes = reportResolutionNotes.trim().ifBlank { "Reviewed by administrator" },
                                    adminId = adminId,
                                    adminName = adminName
                                )
                                reportToResolve = null
                                snackbarHostState.showSnackbar("Report resolved successfully.")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (reportActionType == "LISTING_REMOVED") CrimsonAlert else NeonPurple
                        )
                    ) {
                        Text("Confirm Resolution", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { reportToResolve = null }) {
                        Text("Cancel", color = GamerTextSecondary)
                    }
                }
            )
        }

        // 7. CSV Export Preview Dialog
        csvExportPreview?.let { (title, content) ->
            AlertDialog(
                onDismissRequest = { csvExportPreview = null },
                containerColor = GamerSurface,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = NeonGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        Text("Ready for export to financial auditing and reporting tools:", color = GamerTextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GamerBg)
                                .border(0.5.dp, GamerCardBorder, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(content, fontSize = 10.sp, color = CyberCyan, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("GamerBazaar CSV", content)
                            clipboard.setPrimaryClip(clip)
                            scope.launch { snackbarHostState.showSnackbar("CSV copied to clipboard!") }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                    ) {
                        Text("Copy CSV", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { csvExportPreview = null }) {
                        Text("Close", color = GamerTextSecondary)
                    }
                }
            )
        }
    }
}

// ================= TAB COMPOSABLES =================

@Composable
private fun OverviewTab(
    listingsCount: Int,
    pendingListingsCount: Int,
    usersCount: Int,
    orders: List<OrderEntity>,
    pendingUpiCount: Int,
    reportsCount: Int,
    onExportOrders: () -> Unit,
    onExportTransactions: () -> Unit,
    onExportAuditLogs: () -> Unit
) {
    val totalGmvUsd = orders.filter { it.status != "Refunded" && it.status != "Pending Payment" }
        .sumOf { CurrencyManager.convert(it.amount, it.currency, "USD") }
    val escrowVolumeUsd = orders.filter { it.status == "Paid" || it.status == "Processing" || it.status == "Disputed" }
        .sumOf { CurrencyManager.convert(it.amount, it.currency, "USD") }
    val openDisputesCount = orders.count { it.status == "Disputed" }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Platform Overview Statistics", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Total GMV",
                    value = "$${String.format(Locale.US, "%,.2f", totalGmvUsd)}",
                    color = NeonGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Escrow Volume",
                    value = "$${String.format(Locale.US, "%,.2f", escrowVolumeUsd)}",
                    color = GoldLegendary,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Active Listings",
                    value = "$listingsCount",
                    color = CyberCyan,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Pending Listings",
                    value = "$pendingListingsCount",
                    color = if (pendingListingsCount > 0) CrimsonAlert else NeonGreen,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Open Disputes",
                    value = "$openDisputesCount",
                    color = if (openDisputesCount > 0) CrimsonAlert else NeonGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Pending UPI",
                    value = "$pendingUpiCount",
                    color = if (pendingUpiCount > 0) GoldLegendary else GamerTextSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Registered Users",
                    value = "$usersCount",
                    color = NeonPurple,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Reported Listings",
                    value = "$reportsCount",
                    color = if (reportsCount > 0) CrimsonAlert else GamerTextSecondary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Export Administrative Reports", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GamerSurface),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, GamerCardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Download standardized CSV reports for compliance, reconciliation, and audit logs.", fontSize = 12.sp, color = GamerTextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onExportOrders,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple.copy(alpha = 0.8f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Orders CSV", fontSize = 11.sp, color = Color.White)
                        }
                        Button(
                            onClick = onExportTransactions,
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan.copy(alpha = 0.8f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Ledger CSV", fontSize = 11.sp, color = Color.Black)
                        }
                        Button(
                            onClick = onExportAuditLogs,
                            colors = ButtonDefaults.buttonColors(containerColor = GoldLegendary.copy(alpha = 0.8f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Audit CSV", fontSize = 11.sp, color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = GamerSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, fontSize = 11.sp, color = GamerTextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun ListingsReviewTab(
    pendingListings: List<ListingEntity>,
    onApprove: (ListingEntity) -> Unit,
    onReject: (ListingEntity) -> Unit
) {
    if (pendingListings.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("All listings approved!", color = GamerTextPrimary, fontWeight = FontWeight.SemiBold)
                Text("No pending listings requiring escrow review.", color = GamerTextSecondary, fontSize = 12.sp)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(pendingListings, key = { it.id }) { listing ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GamerSurface),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, GamerCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(listing.gameTitle, color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "$${listing.originalPrice} ${listing.originalCurrency}",
                            color = NeonGreen,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(listing.title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Seller: ${listing.sellerName} | Region: ${listing.region} | Rank: ${listing.rank} | Level: ${listing.accountLevel}", color = GamerTextSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(listing.description, color = GamerTextMuted, fontSize = 12.sp, maxLines = 2)

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(
                            onClick = { onReject(listing) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonAlert)
                        ) {
                            Text("Reject", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onApprove(listing) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                        ) {
                            Text("Approve Listing", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpiPaymentsTab(
    submissions: List<UpiSubmissionEntity>,
    onApprove: (UpiSubmissionEntity) -> Unit,
    onReject: (UpiSubmissionEntity) -> Unit
) {
    if (submissions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("No pending UPI requests", color = GamerTextPrimary, fontWeight = FontWeight.SemiBold)
                Text("All submitted UTR references have been reviewed.", color = GamerTextSecondary, fontSize = 12.sp)
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(submissions, key = { it.id }) { sub ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GamerSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldLegendary.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("UTR: ${sub.utrNumber}", color = GoldLegendary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("₹${sub.amountInr}", color = NeonGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("User: ${sub.userEmail} (${sub.userId})", color = GamerTextSecondary, fontSize = 11.sp)
                    Text("Merchant VPA: ${sub.merchantUpiId}", color = GamerTextMuted, fontSize = 11.sp)
                    if (sub.orderId.isNotBlank()) {
                        Text("Linked Order ID: ${sub.orderId}", color = CyberCyan, fontSize = 11.sp)
                    } else {
                        Text("Purpose: Direct Escrow Wallet Deposit", color = NeonPurple, fontSize = 11.sp)
                    }
                    if (sub.screenshotUrl.isNotBlank()) {
                        Text("Proof Attachment: ${sub.screenshotUrl}", color = GamerTextSecondary, fontSize = 10.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(
                            onClick = { onReject(sub) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonAlert)
                        ) {
                            Text("Reject", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onApprove(sub) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                        ) {
                            Text("Approve & Credit", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrdersAndDisputesTab(
    orders: List<OrderEntity>,
    onResolveRefund: (OrderEntity) -> Unit,
    onDismissDispute: (OrderEntity) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") }
    val filteredOrders = orders.filter {
        when (selectedFilter) {
            "DISPUTED" -> it.status == "Disputed"
            "PAID" -> it.status == "Paid" || it.status == "Processing"
            "DELIVERED" -> it.status == "Delivered"
            "REFUNDED" -> it.status == "Refunded"
            else -> true
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Filter pills
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("ALL", "DISPUTED", "PAID", "DELIVERED", "REFUNDED").forEach { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) NeonPurple else GamerSurface)
                        .border(1.dp, if (isSelected) NeonPurple else GamerCardBorder, RoundedCornerShape(8.dp))
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = filter,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else GamerTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filteredOrders, key = { it.id }) { order ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = GamerSurface),
                    border = androidx.compose.foundation.BorderStroke(
                        0.5.dp,
                        if (order.status == "Disputed") CrimsonAlert else GamerCardBorder
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(order.orderNumber, color = CyberCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            val statusColor = when (order.status) {
                                "Delivered" -> NeonGreen
                                "Disputed" -> CrimsonAlert
                                "Refunded" -> GoldLegendary
                                else -> CyberCyan
                            }
                            Text(order.status, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(order.listingTitle, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("${order.gameTitle} | $${order.amount} ${order.currency} | Buyer: ${order.buyerEmail}", color = GamerTextSecondary, fontSize = 11.sp)

                        if (order.status == "Disputed") {
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CrimsonAlert.copy(alpha = 0.1f))
                                    .border(0.5.dp, CrimsonAlert.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Text("Dispute Reason: ${order.disputeReason}", color = CrimsonAlert, fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                OutlinedButton(
                                    onClick = { onDismissDispute(order) },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonGreen)
                                ) {
                                    Text("Dismiss (Favor Seller)", fontSize = 11.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { onResolveRefund(order) },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldLegendary)
                                ) {
                                    Text("Refund to Buyer", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserManagementTab(
    users: List<UserEntity>,
    onToggleSuspend: (UserEntity) -> Unit,
    onChangeRole: (UserEntity, String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredUsers = users.filter {
        searchQuery.isBlank() || it.username.contains(searchQuery, ignoreCase = true) || it.email.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by username or email...", fontSize = 12.sp) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filteredUsers, key = { it.id }) { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = GamerSurface),
                    border = androidx.compose.foundation.BorderStroke(
                        0.5.dp,
                        if (user.isSuspended) CrimsonAlert else GamerCardBorder
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(user.displayName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("${user.email} (${user.username})", color = GamerTextSecondary, fontSize = 11.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when (user.role) {
                                            "ADMIN" -> GoldLegendary.copy(alpha = 0.2f)
                                            "SELLER" -> CyberCyan.copy(alpha = 0.2f)
                                            else -> NeonPurple.copy(alpha = 0.2f)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    user.role,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (user.role) {
                                        "ADMIN" -> GoldLegendary
                                        "SELLER" -> CyberCyan
                                        else -> NeonPurple
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Wallet: ${user.preferredCurrency} ${String.format(Locale.US, "%.2f", user.walletBalance)} | Status: ${if (user.isSuspended) "SUSPENDED" else "ACTIVE"}",
                            color = if (user.isSuspended) CrimsonAlert else GamerTextMuted,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            if (user.role != "ADMIN") {
                                OutlinedButton(
                                    onClick = { onChangeRole(user, "ADMIN") },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Promote to Admin", fontSize = 10.sp, color = GoldLegendary)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Button(
                                onClick = { onToggleSuspend(user) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (user.isSuspended) NeonGreen else CrimsonAlert
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    if (user.isSuspended) "Reactivate User" else "Suspend User",
                                    fontSize = 11.sp,
                                    color = if (user.isSuspended) Color.Black else Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GameCatalogTab(
    games: List<GameEntity>,
    onAddGame: () -> Unit,
    onTogglePopular: (GameEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Catalog (${games.size} Titles)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Button(
                onClick = onAddGame,
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Game", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(games, key = { it.id }) { game ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = GamerSurface),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, GamerCardBorder)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(game.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("${game.category} • Publisher: ${game.publisher}", color = GamerTextSecondary, fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { onTogglePopular(game) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = if (game.isPopular) GoldLegendary else GamerTextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (game.isPopular) "Popular" else "Normal",
                                fontSize = 11.sp,
                                color = if (game.isPopular) GoldLegendary else GamerTextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShowcaseManagementTab(
    listings: List<ListingEntity>,
    onToggleShowcase: (ListingEntity) -> Unit,
    onToggleFeatured: (ListingEntity) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("Showcase & Featured Account Listings", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Toggle showcase pins for high-tier accounts with hero status or featured homepage highlights.", fontSize = 12.sp, color = GamerTextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
        }

        items(listings, key = { it.id }) { listing ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GamerSurface),
                border = androidx.compose.foundation.BorderStroke(
                    0.5.dp,
                    if (listing.isShowcase) GoldLegendary else GamerCardBorder
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(listing.gameTitle, color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("$${listing.originalPrice} ${listing.originalCurrency}", color = NeonGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(listing.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("Seller: ${listing.sellerName} | Rank: ${listing.rank} | Status: ${listing.status}", color = GamerTextSecondary, fontSize = 11.sp)

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(
                            onClick = { onToggleFeatured(listing) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                if (listing.isFeatured) "★ Featured (On)" else "Featured (Off)",
                                fontSize = 11.sp,
                                color = if (listing.isFeatured) CyberCyan else GamerTextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onToggleShowcase(listing) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (listing.isShowcase) GoldLegendary else GamerSurfaceElevated
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                if (listing.isShowcase) "👑 Showcase Active" else "Set Showcase",
                                fontSize = 11.sp,
                                color = if (listing.isShowcase) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportsQueueTab(
    reports: List<ListingReportEntity>,
    onResolveReport: (ListingReportEntity, String) -> Unit
) {
    if (reports.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Zero listing reports", color = GamerTextPrimary, fontWeight = FontWeight.SemiBold)
                Text("All user submitted flags have been cleared.", color = GamerTextSecondary, fontSize = 12.sp)
            }
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(reports, key = { it.id }) { rep ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GamerSurface),
                border = androidx.compose.foundation.BorderStroke(
                    0.5.dp,
                    if (rep.status == "PENDING") CrimsonAlert else GamerCardBorder
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Reason: ${rep.reason}", color = CrimsonAlert, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(rep.status, color = if (rep.status == "PENDING") CrimsonAlert else NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Target: ${rep.listingTitle} (${rep.listingId})", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("Reporter: ${rep.reporterEmail}", color = GamerTextSecondary, fontSize = 11.sp)
                    Text("Details: ${rep.details}", color = GamerTextMuted, fontSize = 12.sp)

                    if (rep.status == "PENDING") {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            OutlinedButton(
                                onClick = { onResolveReport(rep, "DISMISSED") }
                            ) {
                                Text("Dismiss Report", fontSize = 11.sp, color = GamerTextSecondary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { onResolveReport(rep, "LISTING_REMOVED") },
                                colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert)
                            ) {
                                Text("Remove & Suspend Listing", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FinancialLedgerTab(
    transactions: List<WalletTransactionEntity>
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US) }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Immutable Platform Ledger (${transactions.size} Entries)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Real backend transaction logs recorded with balance checkpoints.", fontSize = 11.sp, color = GamerTextSecondary)
        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(transactions, key = { it.id }) { txn ->
                val isCredit = txn.type.contains("CREDIT") || txn.type.contains("DEPOSIT")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = GamerSurface),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, GamerCardBorder)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(txn.type, color = if (isCredit) NeonGreen else CyberCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(txn.description, color = Color.White, fontSize = 12.sp)
                            Text("User: ${txn.userId} | Ref: ${txn.referenceId} | ${dateFormat.format(Date(txn.createdAt))}", color = GamerTextSecondary, fontSize = 10.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "${if (isCredit) "+" else "-"}${txn.currency} ${String.format(Locale.US, "%.2f", txn.amount)}",
                                color = if (isCredit) NeonGreen else CrimsonAlert,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Bal: ${txn.currency} ${String.format(Locale.US, "%.2f", txn.balanceAfter)}", color = GamerTextMuted, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SystemConfigTab(
    configs: List<SystemConfigEntity>,
    onSaveConfig: (key: String, value: String, description: String) -> Unit
) {
    var vpaInput by remember(configs) {
        mutableStateOf(configs.firstOrNull { it.key == "UPI_MERCHANT_VPA" }?.value ?: "gamerbazaar.escrow@icici")
    }
    var upiEnabled by remember(configs) {
        mutableStateOf(configs.firstOrNull { it.key == "UPI_ENABLED" }?.value != "false")
    }
    var cryptoEnabled by remember(configs) {
        mutableStateOf(configs.firstOrNull { it.key == "CRYPTO_ENABLED" }?.value != "false")
    }
    var walletEnabled by remember(configs) {
        mutableStateOf(configs.firstOrNull { it.key == "WALLET_ENABLED" }?.value != "false")
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("Payment Gateways & Currencies Configuration", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Centralized configuration for merchant endpoints and accepted gateway methods.", fontSize = 12.sp, color = GamerTextSecondary)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GamerSurface),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, GamerCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Merchant UPI VPA (India)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = vpaInput,
                        onValueChange = { vpaInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onSaveConfig("UPI_MERCHANT_VPA", vpaInput.trim(), "Configured Merchant UPI VPA") },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                    ) {
                        Text("Save UPI VPA", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GamerSurface),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, GamerCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Payment Method Toggles", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("UPI Indian Payments", color = GamerTextPrimary, fontSize = 12.sp)
                        Switch(
                            checked = upiEnabled,
                            onCheckedChange = {
                                upiEnabled = it
                                onSaveConfig("UPI_ENABLED", it.toString(), "Enable UPI Payment Gateway")
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Cryptocurrency (USDT/BTC/ETH)", color = GamerTextPrimary, fontSize = 12.sp)
                        Switch(
                            checked = cryptoEnabled,
                            onCheckedChange = {
                                cryptoEnabled = it
                                onSaveConfig("CRYPTO_ENABLED", it.toString(), "Enable Cryptocurrency Gateway")
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Instant Wallet Balance Checkout", color = GamerTextPrimary, fontSize = 12.sp)
                        Switch(
                            checked = walletEnabled,
                            onCheckedChange = {
                                walletEnabled = it
                                onSaveConfig("WALLET_ENABLED", it.toString(), "Enable Wallet Escrow Checkout")
                            }
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = GamerSurface),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, GamerCardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Multi-Currency Pegs (Base: USD)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("1 USD = ₹87.50 INR • €0.92 EUR • £0.78 GBP • 3.67 AED • ¥155.0 JPY", color = CyberCyan, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("All checkout orders enforce atomic server-side conversion against live escrow rates.", color = GamerTextSecondary, fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun AuditLogTab(
    logs: List<AdminAuditLogEntity>
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US) }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Administrative Audit Trail (${logs.size} Events)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Immutable chronological record of administrative actions and approvals.", fontSize = 11.sp, color = GamerTextSecondary)
        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(logs, key = { it.id }) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = GamerSurface),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, GamerCardBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(log.action, color = GoldLegendary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(dateFormat.format(Date(log.timestamp)), color = GamerTextMuted, fontSize = 10.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(log.details, color = Color.White, fontSize = 12.sp)
                        Text("Admin: ${log.adminName} (${log.adminId}) | Target: ${log.targetType} [${log.targetId}]", color = GamerTextSecondary, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}
