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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.data.local.entities.GameEntity
import com.example.data.local.entities.UserEntity
import com.example.data.repository.GameRepository
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.GamerBg
import com.example.ui.theme.GamerCardBorder
import com.example.ui.theme.GamerSurface
import com.example.ui.theme.GamerSurfaceElevated
import com.example.ui.theme.GamerTextMuted
import com.example.ui.theme.GamerTextPrimary
import com.example.ui.theme.GamerTextSecondary
import com.example.ui.theme.GoldLegendary
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleDark
import kotlinx.coroutines.launch

@Composable
fun GameCatalogScreen(
    gameRepository: GameRepository,
    currentUser: UserEntity?,
    onGameSelected: (GameEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryIndex by remember { mutableStateOf(0) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingGame by remember { mutableStateOf<GameEntity?>(null) }
    var gameToDelete by remember { mutableStateOf<GameEntity?>(null) }
    val scope = rememberCoroutineScope()

    val games by gameRepository.searchGames(searchQuery).collectAsState(initial = emptyList())

    val filteredGames = remember(games, selectedCategoryIndex) {
        val selectedCat = CATEGORIES.getOrElse(selectedCategoryIndex) { "All" }
        if (selectedCat == "All") games else games.filter { it.category == selectedCat }
    }

    val isAdmin = currentUser?.role?.equals("ADMIN", ignoreCase = true) == true

    Box(modifier = modifier.fillMaxSize().background(GamerBg)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Game Catalog",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "${filteredGames.size} Titles Available",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                    }

                    if (isAdmin) {
                        Button(
                            onClick = {
                                editingGame = null
                                showAddEditDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("admin_add_game_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Game", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("game_catalog_search"),
                    placeholder = { Text("Search 350+ game titles (e.g. Clash, Valorant, Free Fire...)", fontSize = 13.sp, color = GamerTextMuted) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = GamerTextSecondary, modifier = Modifier.size(18.dp))
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
            }

            // Categories Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedCategoryIndex,
                containerColor = GamerBg,
                contentColor = NeonPurple,
                edgePadding = 16.dp,
                divider = {}
            ) {
                CATEGORIES.forEachIndexed { idx, cat ->
                    Tab(
                        selected = selectedCategoryIndex == idx,
                        onClick = { selectedCategoryIndex = idx },
                        text = {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = if (selectedCategoryIndex == idx) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedCategoryIndex == idx) NeonPurple else GamerTextSecondary
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Games List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredGames, key = { it.id }) { game ->
                    GameRowItem(
                        game = game,
                        isAdmin = isAdmin,
                        onClick = { onGameSelected(game) },
                        onEdit = {
                            editingGame = game
                            showAddEditDialog = true
                        },
                        onDelete = {
                            gameToDelete = game
                        }
                    )
                }
            }
        }

        // Admin Add / Edit Dialog
        if (showAddEditDialog) {
            AddEditGameDialog(
                initialGame = editingGame,
                onDismiss = { showAddEditDialog = false },
                onSave = { title, category, publisher, isPopular ->
                    scope.launch {
                        if (editingGame != null) {
                            gameRepository.updateGame(
                                editingGame!!.copy(
                                    title = title,
                                    category = category,
                                    publisher = publisher,
                                    isPopular = isPopular
                                )
                            )
                        } else {
                            gameRepository.addGame(
                                title = title,
                                category = category,
                                publisher = publisher,
                                isPopular = isPopular
                            )
                        }
                        showAddEditDialog = false
                    }
                }
            )
        }

        // Admin Delete Confirmation Dialog
        if (gameToDelete != null) {
            AlertDialog(
                onDismissRequest = { gameToDelete = null },
                containerColor = GamerSurface,
                title = { Text("Delete Game", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Are you sure you want to remove '${gameToDelete?.title}' from the Gamer Bazaar catalog?",
                        color = GamerTextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            scope.launch {
                                gameToDelete?.let { gameRepository.deleteGame(it) }
                                gameToDelete = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { gameToDelete = null }) {
                        Text("Cancel", color = GamerTextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun GameRowItem(
    game: GameEntity,
    isAdmin: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(0.5.dp, GamerCardBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("catalog_item_${game.id}"),
        colors = CardDefaults.cardColors(containerColor = GamerSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        Brush.linearGradient(listOf(NeonPurpleDark, Color(0xFF1E1B4B)))
                    )
                    .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = game.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = GamerTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (game.isPopular) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(GoldLegendary.copy(alpha = 0.2f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("POPULAR", fontSize = 9.sp, fontWeight = FontWeight.Black, color = GoldLegendary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "${game.category} • ${game.publisher}",
                    fontSize = 11.sp,
                    color = GamerTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (game.activeListingsCount > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF1C182E))
                        .border(0.5.dp, NeonPurple.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${game.activeListingsCount} listings",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonPurple
                    )
                }
            }

            if (isAdmin) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = CyberCyan, modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun AddEditGameDialog(
    initialGame: GameEntity?,
    onDismiss: () -> Unit,
    onSave: (title: String, category: String, publisher: String, isPopular: Boolean) -> Unit
) {
    var title by remember { mutableStateOf(initialGame?.title ?: "") }
    var category by remember { mutableStateOf(initialGame?.category ?: "Mobile Strategy & RPG") }
    var publisher by remember { mutableStateOf(initialGame?.publisher ?: "") }
    var isPopular by remember { mutableStateOf(initialGame?.isPopular ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GamerSurface,
        title = {
            Text(
                text = if (initialGame != null) "Edit Game Title" else "Add New Game to Catalog",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Game Title *") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GamerTextPrimary,
                        unfocusedTextColor = GamerTextPrimary,
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = GamerCardBorder
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = publisher,
                    onValueChange = { publisher = it },
                    label = { Text("Publisher / Studio") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GamerTextPrimary,
                        unfocusedTextColor = GamerTextPrimary,
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = GamerCardBorder
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (e.g. Battle Royale, MOBA)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GamerTextPrimary,
                        unfocusedTextColor = GamerTextPrimary,
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = GamerCardBorder
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isPopular,
                        onCheckedChange = { isPopular = it },
                        colors = CheckboxDefaults.colors(checkedColor = NeonPurple)
                    )
                    Text("Highlight in Popular Carousel", fontSize = 12.sp, color = GamerTextPrimary)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title, category, publisher, isPopular)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
            ) {
                Text("Save Game", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = GamerTextSecondary)
            }
        }
    )
}
