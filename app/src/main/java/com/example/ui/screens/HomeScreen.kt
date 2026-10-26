package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.GameEntity
import com.example.data.local.entities.ListingEntity
import com.example.data.repository.GameRepository
import com.example.data.repository.ListingFilter
import com.example.data.repository.ListingRepository
import com.example.ui.components.FilterBottomSheet
import com.example.ui.components.GameCard
import com.example.ui.components.ListingCard
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

val CATEGORIES = listOf(
    "All", "Battle Royale", "Tactical FPS & Shooters", "MOBA",
    "Mobile Strategy & RPG", "MMORPG", "Sandbox & Survival", "Sports & Racing", "Fighting & Action", "Card & Strategy"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    listingRepository: ListingRepository,
    gameRepository: GameRepository,
    userCurrency: String,
    onListingClick: (String) -> Unit,
    onGameClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var filter by remember { mutableStateOf(ListingFilter()) }
    var selectedCategoryIndex by remember { mutableStateOf(0) }
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val filteredListings by listingRepository.getFilteredListings(filter).collectAsState(initial = emptyList())
    val popularGames by gameRepository.getPopularGames().collectAsState(initial = emptyList())
    val featuredListings by listingRepository.getFeaturedListings().collectAsState(initial = emptyList())

    Box(modifier = modifier.fillMaxSize().background(GamerBg)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // 1. Hero Esports Banner
            item {
                HeroBanner()
            }

            // 2. Search & Filter Bar
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = filter.query,
                            onValueChange = { filter = filter.copy(query = it) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_search_input"),
                            placeholder = { Text("Search by game, rank, skins, title...", fontSize = 13.sp, color = GamerTextMuted) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                if (filter.query.isNotBlank()) {
                                    IconButton(onClick = { filter = filter.copy(query = "") }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear",
                                            tint = GamerTextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = GamerTextPrimary,
                                unfocusedTextColor = GamerTextPrimary,
                                focusedContainerColor = GamerSurface,
                                unfocusedContainerColor = GamerSurface,
                                focusedBorderColor = NeonPurple,
                                unfocusedBorderColor = GamerCardBorder
                            ),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Filter button with active indicator
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (filter.region != "All Regions" || filter.currency != "All" || filter.minPriceUsd != null) NeonPurple else GamerSurface)
                                .border(1.dp, GamerCardBorder, RoundedCornerShape(12.dp))
                                .clickable { showFilterSheet = true }
                                .testTag("home_filter_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filter",
                                tint = if (filter.region != "All Regions" || filter.currency != "All" || filter.minPriceUsd != null) Color.White else CyberCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // Active Filters Chips
                    if (filter.gameTitle != "All Games" || filter.region != "All Regions" || filter.currency != "All" || filter.query.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (filter.gameTitle != "All Games") {
                                ActiveFilterChip(
                                    text = filter.gameTitle,
                                    onRemove = { filter = filter.copy(gameTitle = "All Games") }
                                )
                            }
                            if (filter.region != "All Regions") {
                                ActiveFilterChip(
                                    text = filter.region,
                                    onRemove = { filter = filter.copy(region = "All Regions") }
                                )
                            }
                            if (filter.currency != "All") {
                                ActiveFilterChip(
                                    text = "Orig: ${filter.currency}",
                                    onRemove = { filter = filter.copy(currency = "All") }
                                )
                            }
                        }
                    }
                }
            }

            // 3. Popular Games Carousel
            item {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Popular Games",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = GamerTextPrimary
                        )
                        Text(
                            text = "350+ Titles",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(popularGames) { game ->
                            GameCard(
                                game = game,
                                onClick = {
                                    filter = filter.copy(gameTitle = game.title)
                                }
                            )
                        }
                    }
                }
            }

            // 4. Category Tabs
            item {
                ScrollableTabRow(
                    selectedTabIndex = selectedCategoryIndex,
                    containerColor = GamerBg,
                    contentColor = NeonPurple,
                    edgePadding = 16.dp,
                    divider = {}
                ) {
                    CATEGORIES.forEachIndexed { index, category ->
                        Tab(
                            selected = selectedCategoryIndex == index,
                            onClick = {
                                selectedCategoryIndex = index
                                filter = filter.copy(category = category)
                            },
                            text = {
                                Text(
                                    text = category,
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedCategoryIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedCategoryIndex == index) NeonPurple else GamerTextSecondary
                                )
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 5. Featured Listings Section
            if (featuredListings.isNotEmpty() && filter.gameTitle == "All Games" && filter.query.isBlank()) {
                item {
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Featured Accounts",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldLegendary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "★ Top Tier",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonPurple
                            )
                        }

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(featuredListings) { listing ->
                                ListingCard(
                                    listing = listing,
                                    userCurrency = userCurrency,
                                    onClick = { onListingClick(listing.id) },
                                    modifier = Modifier.width(280.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 6. Recently Added Listings Section Title
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (filter.gameTitle != "All Games") "${filter.gameTitle} Listings" else "Recently Added Listings",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = GamerTextPrimary
                    )
                    Text(
                        text = "${filteredListings.size} Accounts",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CyberCyan
                    )
                }
            }

            // 7. Listings List
            if (filteredListings.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No listings match your criteria",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = GamerTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Try clearing filters or search for another game title.",
                            fontSize = 12.sp,
                            color = GamerTextSecondary
                        )
                    }
                }
            } else {
                items(filteredListings) { listing ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        ListingCard(
                            listing = listing,
                            userCurrency = userCurrency,
                            onClick = { onListingClick(listing.id) }
                        )
                    }
                }
            }
        }

        // Filter Bottom Sheet
        if (showFilterSheet) {
            FilterBottomSheet(
                sheetState = sheetState,
                currentFilter = filter,
                onApplyFilter = { newFilter -> filter = newFilter },
                onDismiss = { showFilterSheet = false }
            )
        }
    }
}

@Composable
private fun HeroBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF2E1065),
                        Color(0xFF1E1B4B),
                        Color(0xFF0F172A)
                    )
                )
            )
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(NeonPurple, CyberCyan)),
                RoundedCornerShape(16.dp)
            )
            .padding(18.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(NeonPurple.copy(alpha = 0.3f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "GLOBAL MARKETPLACE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = CyberCyan,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Buy & Sell Verified\nGaming Accounts",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                lineHeight = 26.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Instant Escrow release • Multi-currency pricing • 350+ titles",
                fontSize = 12.sp,
                color = GamerTextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TrustBadge(icon = Icons.Default.VerifiedUser, label = "0% Fraud Guarantee", color = NeonGreen)
                TrustBadge(icon = Icons.Default.Security, label = "Encrypted Vault", color = CyberCyan)
                TrustBadge(icon = Icons.Default.Speed, label = "Instant Release", color = GoldLegendary)
            }
        }
    }
}

@Composable
private fun TrustBadge(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

@Composable
private fun ActiveFilterChip(
    text: String,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(NeonPurple.copy(alpha = 0.2f))
            .border(0.5.dp, NeonPurple, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, fontSize = 11.sp, color = CyberCyan, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.width(4.dp))
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Remove filter",
            tint = GamerTextSecondary,
            modifier = Modifier
                .size(12.dp)
                .clickable(onClick = onRemove)
        )
    }
}
