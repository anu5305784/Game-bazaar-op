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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.currency.CurrencyManager
import com.example.data.local.entities.UserEntity
import com.example.data.repository.AuthRepository
import com.example.data.repository.DecryptedDeliveryInfo
import com.example.data.repository.DeliveryRepository
import com.example.data.repository.OrderRepository
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

@Composable
fun ProfileScreen(
    currentUser: UserEntity?,
    authRepository: AuthRepository,
    orderRepository: OrderRepository,
    deliveryRepository: DeliveryRepository,
    onNavigateToAuth: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    if (currentUser == null) {
        Box(
            modifier = modifier.fillMaxSize().background(GamerBg),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("You are not signed in", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Sign in or register to manage your account and view purchases", fontSize = 12.sp, color = GamerTextSecondary)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onNavigateToAuth,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                ) {
                    Text("Sign In / Register", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    val userOrders by orderRepository.getOrdersForBuyer(currentUser.id).collectAsState(initial = emptyList())
    var unlockedVaultInfo by remember { mutableStateOf<DecryptedDeliveryInfo?>(null) }
    var showCurrencyDropdown by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    val roleColor = when (currentUser.role.uppercase()) {
        "ADMIN" -> GoldLegendary
        "SELLER" -> NeonPurple
        else -> CyberCyan
    }

    Box(modifier = modifier.fillMaxSize().background(GamerBg)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // User Header Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, GamerCardBorder, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = GamerSurface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(roleColor.copy(alpha = 0.2f))
                                    .border(1.5.dp, roleColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = currentUser.displayName.take(1).uppercase(),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = roleColor
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentUser.displayName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = currentUser.email,
                                    fontSize = 12.sp,
                                    color = GamerTextSecondary
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Role badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(roleColor.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(currentUser.role, fontSize = 9.sp, fontWeight = FontWeight.Black, color = roleColor)
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Email verified badge
                                    if (currentUser.isEmailVerified) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("Verified Email", fontSize = 10.sp, color = NeonGreen)
                                        }
                                    } else {
                                        Text("Unverified", fontSize = 10.sp, color = GoldLegendary)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Wallet & Escrow Stats
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(GamerSurfaceElevated)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Wallet Balance", fontSize = 11.sp, color = GamerTextSecondary)
                                Text(
                                    "${currentUser.preferredCurrency} ${String.format("%.2f", currentUser.walletBalance)}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NeonGreen
                                )
                            }

                            Button(
                                onClick = {
                                    scope.launch {
                                        authRepository.updateProfile(
                                            displayName = currentUser.displayName,
                                            preferredCurrency = currentUser.preferredCurrency,
                                            role = currentUser.role
                                        )
                                        snackbarHostState.showSnackbar("Deposit gateway simulated: funds ready for escrow.")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPurpleDark),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Add Credits", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Quick Role Switcher (Buyer / Seller / Admin)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(0.5.dp, GamerCardBorder, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = GamerSurface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Active Role & Permissions", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("BUYER", "SELLER", "ADMIN").forEach { roleName ->
                                val isSelected = currentUser.role.equals(roleName, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) NeonPurple else GamerSurfaceElevated)
                                        .border(1.dp, if (isSelected) NeonPurple else GamerCardBorder, RoundedCornerShape(8.dp))
                                        .clickable {
                                            scope.launch {
                                                authRepository.updateProfile(
                                                    displayName = currentUser.displayName,
                                                    preferredCurrency = currentUser.preferredCurrency,
                                                    role = roleName
                                                )
                                                snackbarHostState.showSnackbar("Switched profile permissions to $roleName")
                                            }
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(roleName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onNavigateToAdmin,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentUser.role == "ADMIN") GoldLegendary else GamerSurfaceElevated
                            ),
                            modifier = Modifier.fillMaxWidth().testTag("open_admin_panel_btn")
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (currentUser.role == "ADMIN") Color.Black else GoldLegendary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (currentUser.role == "ADMIN") "Open Admin Moderation Panel" else "Admin Escrow Panel & Security Setup",
                                color = if (currentUser.role == "ADMIN") Color.Black else Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // My Purchases & Unlocked Delivery Vaults (Requirement 7)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("My Purchases & Account Vaults", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("${userOrders.size} Orders", fontSize = 12.sp, color = CyberCyan, fontWeight = FontWeight.Bold)
                }
            }

            if (userOrders.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = GamerSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = GamerTextMuted, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Purchased Accounts Yet", fontWeight = FontWeight.Bold, color = GamerTextPrimary, fontSize = 14.sp)
                            Text("When you buy an account, your decrypted credentials will appear securely here.", fontSize = 11.sp, color = GamerTextSecondary)
                        }
                    }
                }
            } else {
                items(userOrders) { order ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = GamerSurface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(order.gameTitle, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CyberCyan)
                                Text("${order.currency} ${order.amount}", fontWeight = FontWeight.Black, fontSize = 13.sp, color = NeonGreen)
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(order.listingTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Order ID: ${order.id} • Status: ${order.status}", fontSize = 10.sp, color = GamerTextSecondary)

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    scope.launch {
                                        val result = deliveryRepository.getAuthorizedDeliveryInfo(
                                            listingId = order.listingId,
                                            requestingUserId = currentUser.id,
                                            requestingUserRole = currentUser.role
                                        )
                                        result.onSuccess { info ->
                                            unlockedVaultInfo = info
                                        }.onFailure { err ->
                                            snackbarHostState.showSnackbar("Access Denied: ${err.message}")
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reveal & Decrypt Delivery Vault", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Settings & Logout
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(0.5.dp, GamerCardBorder, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = GamerSurface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { authRepository.logout() }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Log Out", tint = Color.Red, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Sign Out", fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Unlocked Decrypted Vault Modal (Requirement 7)
        if (unlockedVaultInfo != null) {
            val vault = unlockedVaultInfo!!
            AlertDialog(
                onDismissRequest = { unlockedVaultInfo = null },
                containerColor = GamerSurface,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = NeonGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Decrypted Delivery Vault", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                },
                text = {
                    Column {
                        Text("Transfer Method: ${vault.transferMethod}", fontWeight = FontWeight.Bold, color = CyberCyan, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Account Credentials (AES-GCM Decrypted):", fontSize = 11.sp, color = GamerTextSecondary)
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
                            Text("Instructions: ${vault.instructions}", fontSize = 11.sp, color = GamerTextSecondary)
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
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
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
    }
}
