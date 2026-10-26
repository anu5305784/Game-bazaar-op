package com.example.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.currency.CurrencyManager
import com.example.data.local.entities.ListingEntity
import com.example.data.local.entities.UserEntity
import com.example.data.repository.ListingRepository
import com.example.data.repository.OrderRepository
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
import com.example.ui.theme.StatusApprovedBg
import com.example.ui.theme.StatusApprovedText
import com.example.ui.theme.StatusPendingBg
import com.example.ui.theme.StatusPendingText
import com.example.ui.theme.StatusSoldBg
import com.example.ui.theme.StatusSoldText
import kotlinx.coroutines.launch

val SELLER_TABS = listOf("All", "Approved", "Pending", "Sold", "Rejected", "Orders")

@Composable
fun SellerDashboardScreen(
    currentUser: UserEntity?,
    listingRepository: ListingRepository,
    orderRepository: OrderRepository,
    onCreateListing: () -> Unit,
    onEditListing: (ListingEntity) -> Unit,
    onViewListingDetail: (String) -> Unit,
    onSwitchToSellerRole: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    var listingToDelete by remember { mutableStateOf<ListingEntity?>(null) }
    val scope = rememberCoroutineScope()

    val sellerId = currentUser?.id ?: "usr_seller_01"
    val allSellerListings by listingRepository.getSellerListings(sellerId).collectAsState(initial = emptyList())
    val sellerOrders by orderRepository.getOrdersForSeller(sellerId).collectAsState(initial = emptyList())

    val approvedCount = allSellerListings.count { it.status == "APPROVED" }
    val pendingCount = allSellerListings.count { it.status == "PENDING" }
    val soldCount = allSellerListings.count { it.status == "SOLD" }
    val totalEarnings = sellerOrders.sumOf { it.amount }

    val filteredListings = remember(allSellerListings, selectedTabIndex) {
        when (SELLER_TABS[selectedTabIndex]) {
            "Approved" -> allSellerListings.filter { it.status == "APPROVED" }
            "Pending" -> allSellerListings.filter { it.status == "PENDING" }
            "Sold" -> allSellerListings.filter { it.status == "SOLD" }
            "Rejected" -> allSellerListings.filter { it.status == "REJECTED" }
            else -> allSellerListings
        }
    }

    Box(modifier = modifier.fillMaxSize().background(GamerBg)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Header
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Seller Dashboard",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Manage inventory, track review approvals, and monitor escrow earnings",
                        fontSize = 12.sp,
                        color = GamerTextSecondary
                    )

                    if (currentUser?.role == "BUYER") {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1E1B33))
                                .border(1.dp, NeonPurple, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Become a Seller", fontWeight = FontWeight.Bold, color = NeonPurple, fontSize = 13.sp)
                                    Text("Switch your role to list and sell accounts worldwide.", fontSize = 11.sp, color = GamerTextSecondary)
                                }
                                Button(
                                    onClick = onSwitchToSellerRole,
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Activate", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Stats Cards Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricCard(
                        title = "Wallet Balance",
                        value = "${currentUser?.preferredCurrency ?: "USD"} ${String.format("%.2f", currentUser?.walletBalance ?: 0.0)}",
                        icon = Icons.Default.AttachMoney,
                        accentColor = NeonGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Active / Approved",
                        value = "$approvedCount",
                        icon = Icons.Default.CheckCircle,
                        accentColor = CyberCyan,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatMetricCard(
                        title = "Pending Review",
                        value = "$pendingCount",
                        icon = Icons.Default.HourglassTop,
                        accentColor = GoldLegendary,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricCard(
                        title = "Accounts Sold",
                        value = "$soldCount",
                        icon = Icons.Default.ShoppingBag,
                        accentColor = NeonPurple,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Tabs Row
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = GamerBg,
                    contentColor = NeonPurple,
                    edgePadding = 16.dp,
                    divider = {}
                ) {
                    SELLER_TABS.forEachIndexed { index, tabName ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = tabName,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTabIndex == index) NeonPurple else GamerTextSecondary
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Content according to tab
            if (SELLER_TABS[selectedTabIndex] == "Orders") {
                if (sellerOrders.isEmpty()) {
                    item {
                        EmptyStateView(
                            title = "No Completed Orders Yet",
                            subtitle = "When a buyer purchases your listed account, earnings will be credited here."
                        )
                    }
                } else {
                    items(sellerOrders) { order ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            colors = CardDefaults.cardColors(containerColor = GamerSurface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(order.gameTitle, fontWeight = FontWeight.Bold, color = CyberCyan, fontSize = 12.sp)
                                    Text(
                                        "${order.currency} ${order.amount}",
                                        fontWeight = FontWeight.Black,
                                        color = NeonGreen,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(order.listingTitle, fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Buyer: ${order.buyerEmail} • Status: ${order.status}",
                                    fontSize = 11.sp,
                                    color = GamerTextSecondary
                                )
                            }
                        }
                    }
                }
            } else {
                if (filteredListings.isEmpty()) {
                    item {
                        EmptyStateView(
                            title = "No Listings in ${SELLER_TABS[selectedTabIndex]}",
                            subtitle = "Tap '+ Create Listing' below to publish a new gaming account."
                        )
                    }
                } else {
                    items(filteredListings, key = { it.id }) { listing ->
                        SellerListingRow(
                            listing = listing,
                            onClick = { onViewListingDetail(listing.id) },
                            onEdit = { onEditListing(listing) },
                            onDelete = { listingToDelete = listing }
                        )
                    }
                }
            }
        }

        // Floating Action Button to create listing
        FloatingActionButton(
            onClick = onCreateListing,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 100.dp)
                .testTag("create_listing_fab"),
            containerColor = NeonPurple,
            contentColor = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Listing")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Create Listing", fontWeight = FontWeight.Bold)
            }
        }

        // Delete Confirmation Dialog
        if (listingToDelete != null) {
            AlertDialog(
                onDismissRequest = { listingToDelete = null },
                containerColor = GamerSurface,
                title = { Text("Delete Listing", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Are you sure you want to remove '${listingToDelete?.title}' from the marketplace?",
                        color = GamerTextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                listingToDelete?.let { listingRepository.deleteListing(it) }
                                listingToDelete = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { listingToDelete = null }) {
                        Text("Cancel", color = GamerTextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun StatMetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, GamerCardBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = GamerSurface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 11.sp, color = GamerTextSecondary)
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
        }
    }
}

@Composable
private fun SellerListingRow(
    listing: ListingEntity,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(0.5.dp, GamerCardBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("seller_listing_${listing.id}"),
        colors = CardDefaults.cardColors(containerColor = GamerSurface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(listing.gameTitle, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = CyberCyan)

                // Status Badge
                val (bg, txt) = when (listing.status) {
                    "APPROVED" -> StatusApprovedBg to StatusApprovedText
                    "PENDING" -> StatusPendingBg to StatusPendingText
                    "SOLD" -> StatusSoldBg to StatusSoldText
                    else -> Color.DarkGray to Color.LightGray
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(bg)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(listing.status, fontSize = 9.sp, fontWeight = FontWeight.Black, color = txt)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = listing.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = GamerTextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${listing.originalPrice} ${listing.originalCurrency}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CyberCyan, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CrimsonAlert, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateView(
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ListAlt,
            contentDescription = null,
            tint = GamerTextSecondary,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
        Spacer(modifier = Modifier.height(4.dp))
        Text(subtitle, fontSize = 12.sp, color = GamerTextSecondary)
    }
}
