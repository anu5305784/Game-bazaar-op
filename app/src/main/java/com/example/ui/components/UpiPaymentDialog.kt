package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Shield
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

@Composable
fun UpiPaymentDialog(
    amountInr: Double,
    orderReference: String,
    onDismiss: () -> Unit,
    onSubmitUpi: (utrNumber: String, screenshotUrl: String) -> Unit
) {
    val context = LocalContext.current
    val merchantUpiId = "gamerbazaar.escrow@icici"
    val merchantName = "Gamer Bazaar Global Escrow"

    var utrInput by remember { mutableStateOf("") }
    var screenshotInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GamerSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Payment, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("UPI Merchant Payment", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Merchant Details Box
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
                            Text("Total Payable", fontSize = 11.sp, color = GamerTextSecondary)
                            Text(
                                "₹${String.format("%.2f", amountInr)} INR",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = NeonGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Merchant UPI ID", fontSize = 10.sp, color = GamerTextSecondary)
                                Text(merchantUpiId, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                            }

                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("UPI ID", merchantUpiId))
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = CyberCyan, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Merchant: $merchantName", fontSize = 10.sp, color = GamerTextMuted)
                        Text("Order Ref: $orderReference", fontSize = 10.sp, color = GoldLegendary, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Fraud Prevention & Audit Notice (Requirement 5)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172A))
                        .border(0.5.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Merchant Verification Policy: Submissions are verified against bank statement logs before order release. Screenshots & UTRs alone do not grant immediate access.",
                            fontSize = 10.sp,
                            color = GamerTextSecondary,
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // UTR Number Input
                OutlinedTextField(
                    value = utrInput,
                    onValueChange = {
                        utrInput = it.filter { ch -> ch.isLetterOrDigit() }.take(16)
                        errorMessage = null
                    },
                    label = { Text("12-Digit Bank UTR / Ref Number *") },
                    placeholder = { Text("e.g. 428901239845", fontSize = 12.sp, color = GamerTextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upi_utr_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GamerTextPrimary,
                        unfocusedTextColor = GamerTextPrimary,
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = GamerCardBorder,
                        focusedLabelColor = CyberCyan,
                        unfocusedLabelColor = GamerTextSecondary
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Screenshot URL / Reference Input
                OutlinedTextField(
                    value = screenshotInput,
                    onValueChange = { screenshotInput = it },
                    label = { Text("Payment Screenshot URL (Optional)") },
                    placeholder = { Text("https://...", fontSize = 12.sp, color = GamerTextMuted) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upi_screenshot_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GamerTextPrimary,
                        unfocusedTextColor = GamerTextPrimary,
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = GamerCardBorder,
                        focusedLabelColor = CyberCyan,
                        unfocusedLabelColor = GamerTextSecondary
                    ),
                    singleLine = true
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(errorMessage!!, color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (utrInput.trim().length < 6) {
                        errorMessage = "Please enter a valid 12-digit UTR reference"
                    } else {
                        onSubmitUpi(utrInput.trim(), screenshotInput.trim())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                modifier = Modifier.testTag("upi_i_have_paid_btn")
            ) {
                Text("I Have Paid", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = GamerTextSecondary)
            }
        }
    )
}
