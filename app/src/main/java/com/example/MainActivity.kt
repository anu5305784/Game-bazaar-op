package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.currency.CurrencyManager
import com.example.data.local.entities.ListingEntity
import com.example.data.repository.AdminRepository
import com.example.data.repository.AuthRepository
import com.example.data.repository.DeliveryRepository
import com.example.data.repository.FavoriteRepository
import com.example.data.repository.GameRepository
import com.example.data.repository.ListingReportRepository
import com.example.data.repository.ListingRepository
import com.example.data.repository.OrderRepository
import com.example.data.repository.SupportTicketRepository
import com.example.data.repository.WalletRepository
import com.example.ui.components.GamerBazaarAppBar
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BuyerDashboardScreen
import com.example.ui.screens.CreateEditListingScreen
import com.example.ui.screens.GameCatalogScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ListingDetailScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SellerDashboardScreen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.GamerBg
import com.example.ui.theme.GamerCardBorder
import com.example.ui.theme.GamerSurface
import com.example.ui.theme.GamerTextPrimary
import com.example.ui.theme.GamerTextSecondary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonPurple
import kotlinx.coroutines.launch

enum class MainDestination(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    HOME("Home", Icons.Default.Home),
    CATALOG("Games", Icons.Default.Category),
    BUYER("Orders", Icons.Default.ShoppingBag),
    SELLER("Sell", Icons.Default.Storefront),
    PROFILE("Profile", Icons.Default.Person)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                GamerBazaarApp()
            }
        }
    }
}

@Composable
fun GamerBazaarApp() {
    val context = LocalContext.current
    val authRepository = remember { AuthRepository(context) }
    val gameRepository = remember { GameRepository(context) }
    val listingRepository = remember { ListingRepository(context) }
    val orderRepository = remember { OrderRepository(context) }
    val deliveryRepository = remember { DeliveryRepository(context) }
    val walletRepository = remember { WalletRepository(context) }
    val favoriteRepository = remember { FavoriteRepository(context) }
    val supportTicketRepository = remember { SupportTicketRepository(context) }
    val adminRepository = remember { AdminRepository(context) }
    val listingReportRepository = remember { ListingReportRepository(context) }

    val currentUser by authRepository.currentUser.collectAsState()
    var activeCurrency by remember { mutableStateOf("USD") }

    // Navigation Stack State
    var currentDestination by remember { mutableStateOf(MainDestination.HOME) }
    var selectedListingId by remember { mutableStateOf<String?>(null) }
    var isCreatingOrEditingListing by remember { mutableStateOf(false) }
    var editingListing by remember { mutableStateOf<ListingEntity?>(null) }
    var showAuthScreen by remember { mutableStateOf(false) }
    var showAdminPanel by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Sync user preferred currency if changed
    if (currentUser != null && currentUser!!.preferredCurrency.isNotBlank() && activeCurrency == "USD" && currentUser!!.preferredCurrency != "USD") {
        activeCurrency = currentUser!!.preferredCurrency
    }

    // Handle Hardware Back Button
    BackHandler(
        enabled = selectedListingId != null || isCreatingOrEditingListing || showAuthScreen || showAdminPanel || currentDestination != MainDestination.HOME
    ) {
        when {
            showAuthScreen -> showAuthScreen = false
            showAdminPanel -> showAdminPanel = false
            selectedListingId != null -> selectedListingId = null
            isCreatingOrEditingListing -> {
                isCreatingOrEditingListing = false
                editingListing = null
            }
            currentDestination != MainDestination.HOME -> currentDestination = MainDestination.HOME
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(GamerBg)) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = GamerBg,
            topBar = {
                // Show App Bar only on main destinations when not inside full sub-screen
                if (selectedListingId == null && !isCreatingOrEditingListing && !showAuthScreen && !showAdminPanel) {
                    GamerBazaarAppBar(
                        currentUser = currentUser,
                        activeCurrency = activeCurrency,
                        onCurrencySelected = { newCurrency ->
                            activeCurrency = newCurrency
                            scope.launch {
                                currentUser?.let { user ->
                                    authRepository.updateProfile(user.displayName, newCurrency, user.role)
                                }
                            }
                        },
                        onSearchClick = {
                            currentDestination = MainDestination.CATALOG
                        },
                        onProfileClick = {
                            if (currentUser == null) {
                                showAuthScreen = true
                            } else {
                                currentDestination = MainDestination.PROFILE
                            }
                        },
                        onNotificationsClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Notifications: 24/7 Escrow Protection Active")
                            }
                        }
                    )
                }
            },
            bottomBar = {
                // Standard Bottom Navigation for compact mobile devices
                if (!isWideScreen && selectedListingId == null && !isCreatingOrEditingListing && !showAuthScreen && !showAdminPanel) {
                    NavigationBar(
                        containerColor = GamerSurface,
                        contentColor = GamerTextPrimary,
                        modifier = Modifier.navigationBarsPadding()
                    ) {
                        MainDestination.entries.forEach { dest ->
                            val isSelected = currentDestination == dest
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { currentDestination = dest },
                                icon = {
                                    Icon(
                                        imageVector = dest.icon,
                                        contentDescription = dest.label,
                                        tint = if (isSelected) NeonPurple else GamerTextSecondary
                                    )
                                },
                                label = {
                                    Text(
                                        text = dest.label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) NeonPurple else GamerTextSecondary
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = NeonPurple.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.testTag("nav_item_${dest.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Adaptive NavigationRail for tablet/foldable/desktop screens
                if (isWideScreen && selectedListingId == null && !isCreatingOrEditingListing && !showAuthScreen && !showAdminPanel) {
                    NavigationRail(
                        containerColor = GamerSurface,
                        contentColor = GamerTextPrimary,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        MainDestination.entries.forEach { dest ->
                            val isSelected = currentDestination == dest
                            NavigationRailItem(
                                selected = isSelected,
                                onClick = { currentDestination = dest },
                                icon = {
                                    Icon(
                                        imageVector = dest.icon,
                                        contentDescription = dest.label,
                                        tint = if (isSelected) NeonPurple else GamerTextSecondary
                                    )
                                },
                                label = {
                                    Text(
                                        text = dest.label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) NeonPurple else GamerTextSecondary
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    indicatorColor = NeonPurple.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier.testTag("rail_item_${dest.name.lowercase()}")
                            )
                        }
                    }
                }

                // Main Content Body Router
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    when {
                        showAuthScreen -> {
                            AuthScreen(
                                authRepository = authRepository,
                                onBack = { showAuthScreen = false },
                                onAuthSuccess = { showAuthScreen = false },
                                snackbarHostState = snackbarHostState
                            )
                        }

                        showAdminPanel -> {
                            AdminPanelScreen(
                                authRepository = authRepository,
                                gameRepository = gameRepository,
                                listingRepository = listingRepository,
                                orderRepository = orderRepository,
                                walletRepository = walletRepository,
                                supportTicketRepository = supportTicketRepository,
                                adminRepository = adminRepository,
                                listingReportRepository = listingReportRepository,
                                currentUser = currentUser,
                                onBack = { showAdminPanel = false },
                                snackbarHostState = snackbarHostState
                            )
                        }

                        selectedListingId != null -> {
                            ListingDetailScreen(
                                listingId = selectedListingId!!,
                                listingRepository = listingRepository,
                                orderRepository = orderRepository,
                                favoriteRepository = favoriteRepository,
                                listingReportRepository = listingReportRepository,
                                currentUser = currentUser,
                                userCurrency = activeCurrency,
                                onBack = { selectedListingId = null },
                                onOrderSuccess = {
                                    selectedListingId = null
                                    currentDestination = MainDestination.BUYER
                                },
                                snackbarHostState = snackbarHostState
                            )
                        }

                        isCreatingOrEditingListing -> {
                            CreateEditListingScreen(
                                editingListing = editingListing,
                                currentUser = currentUser,
                                gameRepository = gameRepository,
                                listingRepository = listingRepository,
                                onBack = {
                                    isCreatingOrEditingListing = false
                                    editingListing = null
                                },
                                onSuccess = {
                                    isCreatingOrEditingListing = false
                                    editingListing = null
                                    currentDestination = MainDestination.SELLER
                                },
                                snackbarHostState = snackbarHostState
                            )
                        }

                        else -> {
                            when (currentDestination) {
                                MainDestination.HOME -> {
                                    HomeScreen(
                                        listingRepository = listingRepository,
                                        gameRepository = gameRepository,
                                        userCurrency = activeCurrency,
                                        onListingClick = { id -> selectedListingId = id },
                                        onGameClick = { gameTitle ->
                                            currentDestination = MainDestination.CATALOG
                                        }
                                    )
                                }

                                MainDestination.CATALOG -> {
                                    GameCatalogScreen(
                                        gameRepository = gameRepository,
                                        currentUser = currentUser,
                                        onGameSelected = { game ->
                                            currentDestination = MainDestination.HOME
                                        }
                                    )
                                }

                                MainDestination.BUYER -> {
                                    if (currentUser == null) {
                                        Box(
                                            modifier = Modifier.fillMaxSize().background(GamerBg),
                                            contentAlignment = androidx.compose.ui.Alignment.Center
                                        ) {
                                            androidx.compose.foundation.layout.Column(
                                                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                                                modifier = Modifier.padding(24.dp)
                                            ) {
                                                androidx.compose.material3.Text(
                                                    "Sign in to access Buyer Dashboard",
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                androidx.compose.material3.Text(
                                                    "Track your purchases across all 7 statuses, manage your wallet ledger, reveal decrypted vault credentials, and manage disputes.",
                                                    fontSize = 12.sp,
                                                    color = GamerTextSecondary,
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                )
                                                Spacer(modifier = Modifier.height(16.dp))
                                                androidx.compose.material3.Button(
                                                    onClick = { showAuthScreen = true },
                                                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = NeonPurple)
                                                ) {
                                                    androidx.compose.material3.Text("Sign In / Register", color = Color.White, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    } else {
                                        BuyerDashboardScreen(
                                            currentUser = currentUser!!,
                                            orderRepository = orderRepository,
                                            walletRepository = walletRepository,
                                            favoriteRepository = favoriteRepository,
                                            supportTicketRepository = supportTicketRepository,
                                            deliveryRepository = deliveryRepository,
                                            onViewListingDetail = { id -> selectedListingId = id },
                                            snackbarHostState = snackbarHostState
                                        )
                                    }
                                }

                                MainDestination.SELLER -> {
                                    SellerDashboardScreen(
                                        currentUser = currentUser,
                                        listingRepository = listingRepository,
                                        orderRepository = orderRepository,
                                        onCreateListing = {
                                            editingListing = null
                                            isCreatingOrEditingListing = true
                                        },
                                        onEditListing = { listing ->
                                            editingListing = listing
                                            isCreatingOrEditingListing = true
                                        },
                                        onViewListingDetail = { id -> selectedListingId = id },
                                        onSwitchToSellerRole = {
                                            scope.launch {
                                                currentUser?.let { user ->
                                                    authRepository.updateProfile(user.displayName, user.preferredCurrency, "SELLER")
                                                    snackbarHostState.showSnackbar("Seller profile activated!")
                                                }
                                            }
                                        }
                                    )
                                }

                                MainDestination.PROFILE -> {
                                    ProfileScreen(
                                        currentUser = currentUser,
                                        authRepository = authRepository,
                                        orderRepository = orderRepository,
                                        deliveryRepository = deliveryRepository,
                                        onNavigateToAuth = { showAuthScreen = true },
                                        onNavigateToAdmin = { showAdminPanel = true },
                                        snackbarHostState = snackbarHostState
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
