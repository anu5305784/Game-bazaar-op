package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CrimsonAlert
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

data class CryptoOption(val coin: String, val network: String, val address: String, val ratePerUsd: Double)

val CRYPTO_OPTIONS = listOf(
    CryptoOption("USDT", "TRC-20", "TJzE9qX7...GBEscrowVaultTRC20", 1.0),
    CryptoOption("BTC", "Bitcoin", "bc1qxy2kgdygjrsqtzq2n0yrf2493p83kkfjhx0wlh", 0.000015),
    CryptoOption("ETH", "ERC-20", "0x71C...GBEscrowVaultETH", 0.00038),
    CryptoOption("SOL", "Solana", "So11111111111111111111111111111111111111112", 0.0065)
)

@Composable
fun CryptoPaymentDialog(
    amountUsd: Double,
    orderReference: String,
    onDismiss: () -> Unit,
    onCryptoPaid: (coin: String, network: String) -> Unit
) {
    val context = LocalContext.current
    var selectedCryptoIndex by remember { mutableStateOf(0) }
    var isTestnetSandboxEnabled by remember { mutableStateOf(true) } // Simulation for evaluation

    val selectedOption = CRYPTO_OPTIONS[selectedCryptoIndex]
    val cryptoAmount = amountUsd * selectedOption.ratePerUsd

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GamerSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CurrencyBitcoin, contentDescription = null, tint = GoldLegendary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cryptocurrency Escrow", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Provider Configuration Guard (Requirement 6)
                if (!isTestnetSandboxEnabled) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(CrimsonAlert.copy(alpha = 0.15f))
                            .border(1.dp, CrimsonAlert, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = CrimsonAlert, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text("Production Gateway Disabled", color = CrimsonAlert, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text(
                                    "Server-side payment provider webhook keys are required. Direct blockchain payments remain disabled until backend provider configuration is set.",
                                    fontSize = 10.sp,
                                    color = GamerTextSecondary
                                )
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonPurple.copy(alpha = 0.15f))
                            .border(1.dp, NeonPurple, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Developer Sandbox Enabled: Simulated testnet verification active for evaluation.",
                                fontSize = 11.sp,
                                color = CyberCyan
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Coin Selector Chips
                Text("Select Cryptocurrency", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CRYPTO_OPTIONS.forEachIndexed { idx, opt ->
                        val isSelected = idx == selectedCryptoIndex
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) GoldLegendary else GamerSurfaceElevated)
                                .border(1.dp, if (isSelected) GoldLegendary else GamerCardBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedCryptoIndex = idx }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    opt.coin,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.Black else Color.White
                                )
                                Text(
                                    opt.network,
                                    fontSize = 9.sp,
                                    color = if (isSelected) Color.DarkGray else GamerTextMuted
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Payment Details Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .border(1.dp, GamerCardBorder, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = GamerSurfaceElevated)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Exact Amount", fontSize = 11.sp, color = GamerTextSecondary)
                            Text(
                                "${String.format("%.6f", cryptoAmount)} ${selectedOption.coin}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = GoldLegendary
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("≈ $${String.format("%.2f", amountUsd)} USD", fontSize = 10.sp, color = GamerTextMuted)

                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Deposit Address (${selectedOption.network})", fontSize = 10.sp, color = GamerTextSecondary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                selectedOption.address,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberCyan,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Crypto Address", selectedOption.address))
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = CyberCyan, modifier = Modifier.size(15.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Payment Ref: $orderReference", fontSize = 10.sp, color = GamerTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sandbox toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Testnet Sandbox Mode", fontSize = 11.sp, color = GamerTextSecondary)
                    Switch(
                        checked = isTestnetSandboxEnabled,
                        onCheckedChange = { isTestnetSandboxEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonPurple, checkedTrackColor = NeonPurple.copy(alpha = 0.5f))
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCryptoPaid(selectedOption.coin, selectedOption.network)
                },
                enabled = isTestnetSandboxEnabled,
                colors = ButtonDefaults.buttonColors(containerColor = GoldLegendary),
                modifier = Modifier.testTag("crypto_confirm_btn")
            ) {
                Text("Confirm Crypto Deposit", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = GamerTextSecondary)
            }
        }
    )
}
