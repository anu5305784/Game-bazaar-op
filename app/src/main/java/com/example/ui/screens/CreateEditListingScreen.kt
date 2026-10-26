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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.currency.CurrencyManager
import com.example.data.local.entities.GameEntity
import com.example.data.local.entities.ListingEntity
import com.example.data.local.entities.UserEntity
import com.example.data.repository.GameRepository
import com.example.data.repository.ListingRepository
import com.example.ui.components.ListingCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.GamerBg
import com.example.ui.theme.GamerCardBorder
import com.example.ui.theme.GamerSurface
import com.example.ui.theme.GamerSurfaceElevated
import com.example.ui.theme.GamerTextMuted
import com.example.ui.theme.GamerTextPrimary
import com.example.ui.theme.GamerTextSecondary
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import kotlinx.coroutines.launch

val LISTING_REGION_OPTIONS = listOf(
    "Global",
    "India",
    "Asia",
    "Europe",
    "North America",
    "South America",
    "Middle East",
    "Southeast Asia",
    "Latin America",
    "Oceania",
    "Other"
)

data class GameFormConfig(
    val titlePlaceholder: String,
    val rankLabel: String,
    val rankPlaceholder: String,
    val levelLabel: String,
    val levelPlaceholder: String,
    val skinsLabel: String,
    val skinsPlaceholder: String,
    val charactersLabel: String,
    val charactersPlaceholder: String,
    val itemsLabel: String,
    val itemsPlaceholder: String
)

fun getGameFormConfig(gameTitle: String?): GameFormConfig {
    val title = gameTitle?.lowercase() ?: ""
    return when {
        title.contains("clash of clans") -> GameFormConfig(
            titlePlaceholder = "TH16 Max Account | Max Heroes",
            rankLabel = "League / Rank (e.g. Legend League)",
            rankPlaceholder = "e.g. Legend League",
            levelLabel = "Town Hall (TH)",
            levelPlaceholder = "e.g. 16",
            skinsLabel = "Hero Skins",
            skinsPlaceholder = "e.g. 24",
            charactersLabel = "Heroes Count",
            charactersPlaceholder = "e.g. 4",
            itemsLabel = "Troops, Pets & Defense Levels",
            itemsPlaceholder = "e.g. Max TH16 Defenses, Lvl 10 Pets, 1500 War Stars"
        )
        title.contains("clash royale") -> GameFormConfig(
            titlePlaceholder = "9000 Trophies | Max-Level Cards",
            rankLabel = "Arena / League (e.g. Ultimate Champion)",
            rankPlaceholder = "e.g. Ultimate Champion",
            levelLabel = "King Level",
            levelPlaceholder = "e.g. 15",
            skinsLabel = "Tower Skins",
            skinsPlaceholder = "e.g. 30",
            charactersLabel = "Max Cards",
            charactersPlaceholder = "e.g. 110",
            itemsLabel = "Card Evolutions & Magic Items",
            itemsPlaceholder = "e.g. 12 Evolutions, 50k Elite Wild Cards"
        )
        title.contains("brawl stars") -> GameFormConfig(
            titlePlaceholder = "50000 Trophies | 80 Brawlers Unlocked",
            rankLabel = "Highest Rank (e.g. Masters / Legendary)",
            rankPlaceholder = "e.g. Masters",
            levelLabel = "Trophies",
            levelPlaceholder = "e.g. 52000",
            skinsLabel = "Skins Count",
            skinsPlaceholder = "e.g. 140",
            charactersLabel = "Brawlers",
            charactersPlaceholder = "e.g. 82",
            itemsLabel = "Hypercharges & Star Powers",
            itemsPlaceholder = "e.g. 35 Hypercharges, All Star Powers unlocked"
        )
        title.contains("free fire") -> GameFormConfig(
            titlePlaceholder = "Level 70 | Rare Bundles | Legendary Skins",
            rankLabel = "Rank / Tier (e.g. Grandmaster)",
            rankPlaceholder = "e.g. Grandmaster",
            levelLabel = "Level",
            levelPlaceholder = "e.g. 75",
            skinsLabel = "Bundles/Skins",
            skinsPlaceholder = "e.g. 120",
            charactersLabel = "Characters",
            charactersPlaceholder = "e.g. 45",
            itemsLabel = "Rare Bundles & Gun Skins",
            itemsPlaceholder = "e.g. Sakura Bundle, Hip Hop, Evo Gun Skins Max"
        )
        title.contains("bgmi") -> GameFormConfig(
            titlePlaceholder = "Conqueror Rank | Rare Outfits",
            rankLabel = "Rank / Tier (e.g. Conqueror)",
            rankPlaceholder = "e.g. Conqueror",
            levelLabel = "Account Level",
            levelPlaceholder = "e.g. 78",
            skinsLabel = "Outfits Count",
            skinsPlaceholder = "e.g. 95",
            charactersLabel = "Upgradable Guns",
            charactersPlaceholder = "e.g. 12",
            itemsLabel = "Glacier, X-Suits & Rare Vehicles",
            itemsPlaceholder = "e.g. M416 Glacier Max, Pharaoh X-Suit, Koenigsegg"
        )
        title.contains("pubg") -> GameFormConfig(
            titlePlaceholder = "High Tier Account | Rare Skins",
            rankLabel = "Rank / Tier (e.g. Conqueror / Ace)",
            rankPlaceholder = "e.g. Conqueror",
            levelLabel = "Account Level",
            levelPlaceholder = "e.g. 80",
            skinsLabel = "Outfits Count",
            skinsPlaceholder = "e.g. 110",
            charactersLabel = "Upgradable Guns",
            charactersPlaceholder = "e.g. 15",
            itemsLabel = "Mythic Outfits & Upgradable Guns",
            itemsPlaceholder = "e.g. M416 Fool Max, Blood Raven X-Suit"
        )
        title.contains("valorant") -> GameFormConfig(
            titlePlaceholder = "Immortal Rank | Premium Skins",
            rankLabel = "Competitive Rank (e.g. Radiant)",
            rankPlaceholder = "e.g. Immortal 3",
            levelLabel = "Account Level",
            levelPlaceholder = "e.g. 165",
            skinsLabel = "Weapon Skins",
            skinsPlaceholder = "e.g. 65",
            charactersLabel = "Agents",
            charactersPlaceholder = "e.g. 26",
            itemsLabel = "Knife & Weapon Skins",
            itemsPlaceholder = "e.g. Prime Vandal, Kuronami Vandal, Champions Karambit"
        )
        title.contains("fortnite") -> GameFormConfig(
            titlePlaceholder = "Rare Skins | High-Level Account",
            rankLabel = "Rank / Division (e.g. Unreal)",
            rankPlaceholder = "e.g. Unreal",
            levelLabel = "Account Level",
            levelPlaceholder = "e.g. 350",
            skinsLabel = "Outfits Count",
            skinsPlaceholder = "e.g. 210",
            charactersLabel = "Pickaxes",
            charactersPlaceholder = "e.g. 85",
            itemsLabel = "OG & Rare Skins",
            itemsPlaceholder = "e.g. Renegade Raider, Black Knight, Travis Scott, Minty Axe"
        )
        title.contains("genshin") -> GameFormConfig(
            titlePlaceholder = "AR 60 | Multiple C6 5-Star Characters",
            rankLabel = "World Level / Abyss",
            rankPlaceholder = "e.g. WL 8 | 36-Star Abyss",
            levelLabel = "Adventure Rank",
            levelPlaceholder = "e.g. 60",
            skinsLabel = "5-Star Weapons",
            skinsPlaceholder = "e.g. 18",
            charactersLabel = "5-Star Chars",
            charactersPlaceholder = "e.g. 25",
            itemsLabel = "Constellations & Primogems",
            itemsPlaceholder = "e.g. C6 Raiden, C2 Furina, 15k Primogems"
        )
        title.contains("league of legends") || title.contains("wild rift") -> GameFormConfig(
            titlePlaceholder = "Challenger Rank | 150+ Skins",
            rankLabel = "Rank / Tier (e.g. Challenger)",
            rankPlaceholder = "e.g. Challenger",
            levelLabel = "Summoner Lvl",
            levelPlaceholder = "e.g. 280",
            skinsLabel = "Skins Count",
            skinsPlaceholder = "e.g. 180",
            charactersLabel = "Champions",
            charactersPlaceholder = "e.g. 168",
            itemsLabel = "Prestige & Mythic Skins",
            itemsPlaceholder = "e.g. Prestige K/DA, Hextech Annie, 100k Blue Essence"
        )
        title.contains("roblox") -> GameFormConfig(
            titlePlaceholder = "Stacked Account | Limiteds & Robux",
            rankLabel = "Account Status / Badges",
            rankPlaceholder = "e.g. Verified / 2017 Account",
            levelLabel = "Account Age (Yrs)",
            levelPlaceholder = "e.g. 7",
            skinsLabel = "Limited Items",
            skinsPlaceholder = "e.g. 45",
            charactersLabel = "Game Passes",
            charactersPlaceholder = "e.g. 30",
            itemsLabel = "Rare Limiteds & RAP Value",
            itemsPlaceholder = "e.g. Dominus, Valkyrie Helm, 250k RAP"
        )
        title.contains("call of duty") || title.contains("codm") || title.contains("warzone") -> GameFormConfig(
            titlePlaceholder = "Damascus / Mythic Weapons | Legendary Rank",
            rankLabel = "Rank / Tier (e.g. Legendary)",
            rankPlaceholder = "e.g. Legendary",
            levelLabel = "Player Level",
            levelPlaceholder = "e.g. 200",
            skinsLabel = "Camos/Blueprints",
            skinsPlaceholder = "e.g. 80",
            charactersLabel = "Operators",
            charactersPlaceholder = "e.g. 20",
            itemsLabel = "Mythic & Legendary Weapons",
            itemsPlaceholder = "e.g. M13 Morningstar, Kilo Demonsong, Damascus unlocked"
        )
        else -> GameFormConfig(
            titlePlaceholder = "High Tier Account | Verified Items",
            rankLabel = "Rank / Tier (e.g. Master / Diamond)",
            rankPlaceholder = "e.g. Diamond",
            levelLabel = "Level",
            levelPlaceholder = "e.g. 50",
            skinsLabel = "Skins",
            skinsPlaceholder = "e.g. 20",
            charactersLabel = "Characters",
            charactersPlaceholder = "e.g. 15",
            itemsLabel = "Rare Items / Inventory Details",
            itemsPlaceholder = "e.g. Exclusive cosmetics, rare gear, unlocked achievements"
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateEditListingScreen(
    editingListing: ListingEntity?,
    currentUser: UserEntity?,
    gameRepository: GameRepository,
    listingRepository: ListingRepository,
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val allGames by gameRepository.getAllGames().collectAsState(initial = emptyList())

    var selectedGame by remember {
        mutableStateOf<GameEntity?>(null)
    }
    var title by remember { mutableStateOf(editingListing?.title ?: "") }
    var description by remember { mutableStateOf(editingListing?.description ?: "") }
    var originalCurrency by remember { mutableStateOf(editingListing?.originalCurrency ?: currentUser?.preferredCurrency ?: "USD") }
    var priceText by remember { mutableStateOf(editingListing?.originalPrice?.toString() ?: "") }
    var rank by remember { mutableStateOf(editingListing?.rank ?: "") }
    var levelText by remember { mutableStateOf(editingListing?.accountLevel?.toString() ?: "") }
    var skinsText by remember { mutableStateOf(editingListing?.skinsCount?.toString() ?: "") }
    var charactersText by remember { mutableStateOf(editingListing?.charactersCount?.toString() ?: "") }
    var itemsDescription by remember { mutableStateOf(editingListing?.itemsDescription ?: "") }
    var region by remember { mutableStateOf(editingListing?.region ?: "Global") }
    var imageUrls by remember { mutableStateOf(editingListing?.imageUrlsJson?.split(",")?.filter { it.isNotBlank() } ?: listOf("https://images.unsplash.com/photo-1542751371-adc38448a05e?w=800")) }
    var newImageUrl by remember { mutableStateOf("") }

    // Protected Delivery Fields
    var deliveryCredentials by remember { mutableStateOf("") }
    var transferInstructions by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }

    var isPreviewMode by remember { mutableStateOf(false) }
    var showGamePicker by remember { mutableStateOf(false) }
    var showCurrencyPicker by remember { mutableStateOf(false) }
    var showRegionPicker by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    // Match initial game if editing
    if (selectedGame == null && allGames.isNotEmpty()) {
        selectedGame = if (editingListing != null) {
            allGames.firstOrNull { it.id == editingListing.gameId } ?: allGames.first()
        } else {
            allGames.firstOrNull { it.title == "Clash of Clans" } ?: allGames.first()
        }
    }

    val gameConfig = remember(selectedGame?.title) { getGameFormConfig(selectedGame?.title) }

    Box(modifier = modifier.fillMaxSize().background(GamerBg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(GamerSurface)
                    .border(0.5.dp, GamerCardBorder)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("create_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GamerTextPrimary)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (editingListing != null) "Edit Listing" else "Create Account Listing",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Preview Toggle Button
                OutlinedButton(
                    onClick = { isPreviewMode = !isPreviewMode },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isPreviewMode) NeonPurple else CyberCyan
                    ),
                    modifier = Modifier.testTag("toggle_preview_button")
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isPreviewMode) "Edit Form" else "Preview", fontSize = 12.sp)
                }
            }

            if (isPreviewMode) {
                // Listing Live Preview Card Mode
                val previewListing = ListingEntity(
                    id = "preview_listing",
                    sellerId = currentUser?.id ?: "seller_preview",
                    sellerName = currentUser?.displayName ?: "Verified Seller",
                    sellerRating = 4.9f,
                    gameId = selectedGame?.id ?: "game_preview",
                    gameTitle = selectedGame?.title ?: "Select Game",
                    title = title.ifBlank { gameConfig.titlePlaceholder },
                    description = description.ifBlank { "No description entered yet." },
                    originalPrice = priceText.toDoubleOrNull() ?: 100.0,
                    originalCurrency = originalCurrency,
                    accountLevel = levelText.toIntOrNull() ?: 50,
                    rank = rank.ifBlank { gameConfig.rankPlaceholder },
                    skinsCount = skinsText.toIntOrNull() ?: 15,
                    charactersCount = charactersText.toIntOrNull() ?: 10,
                    itemsDescription = itemsDescription,
                    region = region,
                    status = "PENDING",
                    imageUrlsJson = imageUrls.joinToString(",")
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text("Listing Card Preview", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                    Spacer(modifier = Modifier.height(10.dp))
                    ListingCard(
                        listing = previewListing,
                        userCurrency = originalCurrency,
                        onClick = {}
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { isPreviewMode = false },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Back to Editing", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            } else {
                // Form Mode
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // 1. Select Game
                    Text("Game Title *", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(GamerSurface)
                                .border(1.dp, GamerCardBorder, RoundedCornerShape(10.dp))
                                .clickable { showGamePicker = true }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = selectedGame?.title ?: "Select from 350+ Games",
                                color = GamerTextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = CyberCyan)
                        }

                        DropdownMenu(
                            expanded = showGamePicker,
                            onDismissRequest = { showGamePicker = false },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .height(300.dp)
                                .background(GamerSurfaceElevated)
                        ) {
                            allGames.forEach { game ->
                                DropdownMenuItem(
                                    text = {
                                        Text("${game.title} (${game.category})", color = Color.White, fontSize = 13.sp)
                                    },
                                    onClick = {
                                        selectedGame = game
                                        showGamePicker = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Listing Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Listing Title * (e.g. ${gameConfig.titlePlaceholder})") },
                        placeholder = { Text(gameConfig.titlePlaceholder, fontSize = 13.sp, color = GamerTextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("listing_title_input"),
                        colors = customFieldColors(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Price & Currency
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            label = { Text("Price *") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("listing_price_input"),
                            colors = customFieldColors(),
                            singleLine = true
                        )

                        // Currency Dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = originalCurrency,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Currency *") },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { showCurrencyPicker = true },
                                        modifier = Modifier.testTag("currency_arrow_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Select Currency",
                                            tint = CyberCyan
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("listing_currency_input"),
                                colors = customFieldColors(),
                                singleLine = true
                            )

                            // Clickable transparent overlay covering entire field to capture taps anywhere
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .testTag("listing_currency_dropdown_trigger")
                                    .clickable { showCurrencyPicker = true }
                            )

                            DropdownMenu(
                                expanded = showCurrencyPicker,
                                onDismissRequest = { showCurrencyPicker = false },
                                modifier = Modifier
                                    .background(GamerSurfaceElevated)
                                    .heightIn(max = 350.dp)
                                    .border(1.dp, GamerCardBorder, RoundedCornerShape(8.dp))
                            ) {
                                CurrencyManager.SUPPORTED_CURRENCIES.forEach { curr ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(curr.flagEmoji, fontSize = 16.sp)
                                                Text(
                                                    text = "${curr.code} (${curr.symbol})",
                                                    color = if (curr.code == originalCurrency) CyberCyan else Color.White,
                                                    fontWeight = if (curr.code == originalCurrency) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 13.sp
                                                )
                                                Text(
                                                    text = curr.name,
                                                    color = GamerTextSecondary,
                                                    fontSize = 11.sp,
                                                    maxLines = 1
                                                )
                                            }
                                        },
                                        onClick = {
                                            originalCurrency = curr.code
                                            showCurrencyPicker = false
                                        },
                                        modifier = Modifier.testTag("currency_option_${curr.code}")
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4. Rank & Region
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = rank,
                            onValueChange = { rank = it },
                            label = { Text(gameConfig.rankLabel) },
                            placeholder = { Text(gameConfig.rankPlaceholder, fontSize = 12.sp, color = GamerTextMuted) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("listing_rank_input"),
                            colors = customFieldColors(),
                            singleLine = true
                        )

                        // Region Dropdown
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = region,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Region") },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { showRegionPicker = true },
                                        modifier = Modifier.testTag("region_arrow_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Select Region",
                                            tint = CyberCyan
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("listing_region_input"),
                                colors = customFieldColors(),
                                singleLine = true
                            )

                            // Clickable transparent overlay covering entire field to capture taps anywhere
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .testTag("listing_region_dropdown_trigger")
                                    .clickable { showRegionPicker = true }
                            )

                            DropdownMenu(
                                expanded = showRegionPicker,
                                onDismissRequest = { showRegionPicker = false },
                                modifier = Modifier
                                    .background(GamerSurfaceElevated)
                                    .heightIn(max = 350.dp)
                                    .border(1.dp, GamerCardBorder, RoundedCornerShape(8.dp))
                            ) {
                                LISTING_REGION_OPTIONS.forEach { reg ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = reg,
                                                color = if (reg == region) CyberCyan else Color.White,
                                                fontWeight = if (reg == region) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 13.sp
                                            )
                                        },
                                        onClick = {
                                            region = reg
                                            showRegionPicker = false
                                        },
                                        modifier = Modifier.testTag("region_option_${reg.replace(" ", "_")}")
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 5. Level, Skins, Characters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = levelText,
                            onValueChange = { levelText = it.filter { ch -> ch.isDigit() } },
                            label = { Text(gameConfig.levelLabel) },
                            placeholder = { Text(gameConfig.levelPlaceholder, fontSize = 11.sp, color = GamerTextMuted) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("listing_level_input"),
                            colors = customFieldColors(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = skinsText,
                            onValueChange = { skinsText = it.filter { ch -> ch.isDigit() } },
                            label = { Text(gameConfig.skinsLabel) },
                            placeholder = { Text(gameConfig.skinsPlaceholder, fontSize = 11.sp, color = GamerTextMuted) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("listing_skins_input"),
                            colors = customFieldColors(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = charactersText,
                            onValueChange = { charactersText = it.filter { ch -> ch.isDigit() } },
                            label = { Text(gameConfig.charactersLabel) },
                            placeholder = { Text(gameConfig.charactersPlaceholder, fontSize = 11.sp, color = GamerTextMuted) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("listing_characters_input"),
                            colors = customFieldColors(),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 6. Rare Items & Inventory
                    OutlinedTextField(
                        value = itemsDescription,
                        onValueChange = { itemsDescription = it },
                        label = { Text(gameConfig.itemsLabel) },
                        placeholder = { Text(gameConfig.itemsPlaceholder, fontSize = 11.sp, color = GamerTextMuted) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("listing_items_input"),
                        colors = customFieldColors(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 7. Full Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Account Description *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("listing_desc_input"),
                        colors = customFieldColors(),
                        maxLines = 5
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 8. Image URLs
                    Text("Account Screenshots & Image URLs", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newImageUrl,
                            onValueChange = { newImageUrl = it },
                            placeholder = { Text("https://...", fontSize = 12.sp, color = GamerTextMuted) },
                            modifier = Modifier.weight(1f),
                            colors = customFieldColors(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newImageUrl.isNotBlank()) {
                                    imageUrls = imageUrls + newImageUrl.trim()
                                    newImageUrl = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Image")
                        }
                    }

                    // Existing image chips
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        imageUrls.forEachIndexed { idx, url ->
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(GamerSurfaceElevated)
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Image #${idx + 1}", fontSize = 11.sp, color = GamerTextSecondary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = GamerTextMuted,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable {
                                            imageUrls = imageUrls.toMutableList().also { it.removeAt(idx) }
                                        }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 9. PROTECTED DELIVERY CREDENTIALS VAULT (Requirement 7)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, NeonPurple.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF130E26))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Protected Delivery Information Vault",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Credentials are encrypted with AES-GCM. They are NEVER shown in public listings and are released ONLY to the verified buyer upon completed escrow payment.",
                                fontSize = 11.sp,
                                color = GamerTextSecondary,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = deliveryCredentials,
                                onValueChange = { deliveryCredentials = it },
                                label = { Text("Account Login / Email / Password / 2FA Codes *") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(95.dp)
                                    .testTag("delivery_credentials_input"),
                                colors = customFieldColors(),
                                maxLines = 4
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = transferInstructions,
                                onValueChange = { transferInstructions = it },
                                label = { Text("Account Handover Instructions (Optional)") },
                                placeholder = { Text("e.g. Login via Supercell ID, link personal email...", fontSize = 11.sp, color = GamerTextMuted) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = customFieldColors(),
                                singleLine = true
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 10. Rules Compliance & Fair Play Checkbox
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(GamerSurface)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = termsAccepted,
                            onCheckedChange = { termsAccepted = it },
                            colors = CheckboxDefaults.colors(checkedColor = NeonPurple)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "I certify this account listing complies with Gamer Bazaar trade policies and contains verified, non-stolen credentials.",
                            fontSize = 11.sp,
                            color = GamerTextPrimary,
                            lineHeight = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 11. Submit Button
                    Button(
                        onClick = {
                            val price = priceText.toDoubleOrNull()
                            val validationError = when {
                                selectedGame == null -> "Please select a game"
                                title.isBlank() -> "Please enter a listing title"
                                price == null || price <= 0.0 -> "Please enter a valid price"
                                editingListing == null && deliveryCredentials.isBlank() -> "Please provide account credentials in the delivery vault"
                                !termsAccepted -> "Please accept the trade compliance certification"
                                else -> null
                            }

                            if (validationError != null) {
                                scope.launch { snackbarHostState.showSnackbar(validationError) }
                                return@Button
                            }

                            isSubmitting = true
                            scope.launch {
                                val sellerId = currentUser?.id ?: "usr_seller_01"
                                val sellerName = currentUser?.displayName ?: "ApexTrader Official"
                                val autoApprove = currentUser?.role == "ADMIN"
                                val finalCurrency = originalCurrency.uppercase()

                                val result = if (editingListing != null) {
                                    val updated = editingListing.copy(
                                        gameId = selectedGame!!.id,
                                        gameTitle = selectedGame!!.title,
                                        title = title.trim(),
                                        description = description.trim(),
                                        originalPrice = price!!,
                                        originalCurrency = finalCurrency,
                                        accountLevel = levelText.toIntOrNull() ?: 1,
                                        rank = rank.ifBlank { "Unranked" },
                                        skinsCount = skinsText.toIntOrNull() ?: 0,
                                        charactersCount = charactersText.toIntOrNull() ?: 0,
                                        itemsDescription = itemsDescription.trim(),
                                        region = region,
                                        imageUrlsJson = imageUrls.joinToString(",")
                                    )
                                    listingRepository.updateListing(updated).map { updated }
                                } else {
                                    listingRepository.createListing(
                                        sellerId = sellerId,
                                        sellerName = sellerName,
                                        gameId = selectedGame!!.id,
                                        gameTitle = selectedGame!!.title,
                                        title = title.trim(),
                                        description = description.trim(),
                                        originalPrice = price!!,
                                        originalCurrency = finalCurrency,
                                        accountLevel = levelText.toIntOrNull() ?: 1,
                                        rank = rank.ifBlank { "Unranked" },
                                        skinsCount = skinsText.toIntOrNull() ?: 0,
                                        charactersCount = charactersText.toIntOrNull() ?: 0,
                                        itemsDescription = itemsDescription.trim(),
                                        region = region,
                                        imageUrls = imageUrls,
                                        deliveryCredentialsPlain = deliveryCredentials,
                                        transferInstructions = transferInstructions,
                                        autoApprove = autoApprove
                                    )
                                }
                                isSubmitting = false
                                result.onSuccess {
                                    snackbarHostState.showSnackbar(if (editingListing != null) "Listing updated successfully!" else "Listing submitted successfully!")
                                    onSuccess()
                                }.onFailure { err ->
                                    snackbarHostState.showSnackbar("Error: ${err.message}")
                                }
                            }
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_listing_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                    ) {
                        Text(
                            text = if (currentUser?.role == "ADMIN") "Publish Listing (Admin Instant)" else "Submit Listing for Review",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
private fun customFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = GamerTextPrimary,
    unfocusedTextColor = GamerTextPrimary,
    focusedContainerColor = GamerSurface,
    unfocusedContainerColor = GamerSurface,
    focusedBorderColor = NeonPurple,
    unfocusedBorderColor = GamerCardBorder,
    focusedLabelColor = CyberCyan,
    unfocusedLabelColor = GamerTextSecondary
)
