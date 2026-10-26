package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.data.local.entities.ListingEntity
import com.example.data.local.entities.UserEntity
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.GamerCardBorder
import com.example.ui.theme.GamerSurface
import com.example.ui.theme.GamerSurfaceElevated
import com.example.ui.theme.GamerTextMuted
import com.example.ui.theme.GamerTextPrimary
import com.example.ui.theme.GamerTextSecondary
import com.example.ui.theme.GoldLegendary
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple

enum class PaymentMethodType {
    WALLET,
    UPI,
    CRYPTO
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutBottomSheet(
    sheetState: SheetState,
    listing: ListingEntity,
    currentUser: UserEntity,
    userCurrency: String,
    isProcessing: Boolean,
    onDismiss: () -> Unit,
    onPayWithWallet: () -> Unit,
    onSelectUpi: (amountInr: Double, orderReference: String) -> Unit,
    onSelectCrypto: (amountUsd: Double, orderReference: String) -> Unit
) {
    var selectedMethod by remember { mutableStateOf(PaymentMethodType.WALLET) }

    val priceDisplay = CurrencyManager.formatDisplayPrice(
        originalPrice = listing.originalPrice,
        originalCurrency = listing.originalCurrency,
        userCurrency = userCurrency
    )

    val priceInBuyerCurrency = CurrencyManager.convert(
        amount = listing.originalPrice,
        fromCode = listing.originalCurrency,
        toCode = currentUser.preferredCurrency
    )
    val hasEnoughWalletBalance = currentUser.walletBalance >= priceInBuyerCurrency

    val amountInr = CurrencyManager.convert(listing.originalPrice, listing.originalCurrency, "INR")
    val amountUsd = CurrencyManager.convert(listing.originalPrice, listing.originalCurrency, "USD")
    val tempOrderRef = "GB-${listing.id.takeLast(6).uppercase()}"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = GamerSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Secure Escrow Checkout", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = GamerTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Listing Mini Summary
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(GamerSurfaceElevated)
                    .border(1.dp, GamerCardBorder, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(listing.gameTitle, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = CyberCyan)
                        Text(priceDisplay.primaryPrice, fontWeight = FontWeight.Black, fontSize = 15.sp, color = NeonGreen)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(listing.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = GamerTextPrimary, maxLines = 1)
                    if (priceDisplay.secondaryPrice != null) {
                        Text(priceDisplay.secondaryPrice, fontSize = 10.sp, color = GamerTextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Select Payment Method", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
            Spacer(modifier = Modifier.height(8.dp))

            // Method 1: Pay with Wallet
            PaymentMethodCard(
                title = "Pay with Gamer Bazaar Wallet",
                subtitle = "Available: ${currentUser.preferredCurrency} ${String.format("%.2f", currentUser.walletBalance)}",
                badgeText = if (hasEnoughWalletBalance) "Sufficient Funds" else "Insufficient Funds",
                badgeColor = if (hasEnoughWalletBalance) NeonGreen else Color.Red,
                icon = Icons.Default.AccountBalanceWallet,
                isSelected = selectedMethod == PaymentMethodType.WALLET,
                onClick = { selectedMethod = PaymentMethodType.WALLET }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Method 2: UPI
            PaymentMethodCard(
                title = "Pay with UPI (GPay, PhonePe, Paytm)",
                subtitle = "Total: ₹${String.format("%.2f", amountInr)} INR • Manual Merchant Verification",
                badgeText = "Fast In India",
                badgeColor = CyberCyan,
                icon = Icons.Default.Payment,
                isSelected = selectedMethod == PaymentMethodType.UPI,
                onClick = { selectedMethod = PaymentMethodType.UPI }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Method 3: Crypto
            PaymentMethodCard(
                title = "Cryptocurrency Escrow",
                subtitle = "USDT (TRC-20), BTC, ETH, SOL",
                badgeText = "Global Web3",
                badgeColor = GoldLegendary,
                icon = Icons.Default.CurrencyBitcoin,
                isSelected = selectedMethod == PaymentMethodType.CRYPTO,
                onClick = { selectedMethod = PaymentMethodType.CRYPTO }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Checkout Action Button
            Button(
                onClick = {
                    when (selectedMethod) {
                        PaymentMethodType.WALLET -> onPayWithWallet()
                        PaymentMethodType.UPI -> onSelectUpi(amountInr, tempOrderRef)
                        PaymentMethodType.CRYPTO -> onSelectCrypto(amountUsd, tempOrderRef)
                    }
                },
                enabled = !isProcessing && (selectedMethod != PaymentMethodType.WALLET || hasEnoughWalletBalance),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("checkout_continue_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Processing Order...", color = Color.White, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (selectedMethod) {
                            PaymentMethodType.WALLET -> "Confirm & Pay with Wallet"
                            PaymentMethodType.UPI -> "Proceed to UPI Payment"
                            PaymentMethodType.CRYPTO -> "Proceed to Crypto Escrow"
                        },
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun PaymentMethodCard(
    title: String,
    subtitle: String,
    badgeText: String,
    badgeColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(
                1.5.dp,
                if (isSelected) NeonPurple else GamerCardBorder,
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF1D1735) else GamerSurfaceElevated
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) NeonPurple else CyberCyan,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GamerTextPrimary)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(badgeText, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = badgeColor)
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, fontSize = 10.sp, color = GamerTextSecondary)
            }
        }
    }
}
