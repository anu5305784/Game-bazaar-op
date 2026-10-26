package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.data.repository.ListingFilter
import com.example.data.repository.SortOption
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.GamerCardBorder
import com.example.ui.theme.GamerSurface
import com.example.ui.theme.GamerSurfaceElevated
import com.example.ui.theme.GamerTextPrimary
import com.example.ui.theme.GamerTextSecondary
import com.example.ui.theme.NeonPurple

val REGION_OPTIONS = listOf(
    "All Regions", "Global", "India", "Asia", "Europe", "North America", "South America", "Middle East", "Southeast Asia", "Latin America", "Oceania", "Other"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    sheetState: SheetState,
    currentFilter: ListingFilter,
    onApplyFilter: (ListingFilter) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedRegion by remember { mutableStateOf(currentFilter.region) }
    var selectedCurrency by remember { mutableStateOf(currentFilter.currency) }
    var selectedSort by remember { mutableStateOf(currentFilter.sortBy) }
    var minPriceInput by remember { mutableStateOf(currentFilter.minPriceUsd?.toInt()?.toString() ?: "") }
    var maxPriceInput by remember { mutableStateOf(currentFilter.maxPriceUsd?.toInt()?.toString() ?: "") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = GamerSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = NeonPurple,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Filter & Sort Listings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = GamerTextPrimary
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = GamerTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sort By
            Text("Sort Order", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SortOption.entries.forEach { option ->
                    val isSelected = selectedSort == option
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) NeonPurple else GamerSurfaceElevated)
                            .border(
                                1.dp,
                                if (isSelected) NeonPurple else GamerCardBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedSort = option }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = option.label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else GamerTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Region
            Text("Region / Server", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                REGION_OPTIONS.forEach { reg ->
                    val isSelected = selectedRegion == reg
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) CyberCyan else GamerSurfaceElevated)
                            .border(
                                1.dp,
                                if (isSelected) CyberCyan else GamerCardBorder,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedRegion = reg }
                            .padding(horizontal = 10.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = reg,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.Black else GamerTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Original Currency
            Text("Original Listing Currency", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("All") + CurrencyManager.SUPPORTED_CURRENCIES.map { it.code }.forEach { currCode ->
                    val isSelected = selectedCurrency == currCode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) NeonPurple else GamerSurfaceElevated)
                            .border(
                                1.dp,
                                if (isSelected) NeonPurple else GamerCardBorder,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { selectedCurrency = currCode }
                            .padding(horizontal = 9.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = currCode,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else GamerTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Price Range (USD Equivalent)
            Text("Price Range (USD equivalent)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = minPriceInput,
                    onValueChange = { minPriceInput = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Min ($)", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GamerTextPrimary,
                        unfocusedTextColor = GamerTextPrimary,
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = GamerCardBorder
                    ),
                    singleLine = true
                )
                OutlinedTextField(
                    value = maxPriceInput,
                    onValueChange = { maxPriceInput = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Max ($)", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = GamerTextPrimary,
                        unfocusedTextColor = GamerTextPrimary,
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = GamerCardBorder
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        selectedRegion = "All Regions"
                        selectedCurrency = "All"
                        selectedSort = SortOption.NEWEST
                        minPriceInput = ""
                        maxPriceInput = ""
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GamerTextSecondary)
                ) {
                    Text("Reset")
                }

                Button(
                    onClick = {
                        val min = minPriceInput.toDoubleOrNull()
                        val max = maxPriceInput.toDoubleOrNull()
                        onApplyFilter(
                            currentFilter.copy(
                                region = selectedRegion,
                                currency = selectedCurrency,
                                sortBy = selectedSort,
                                minPriceUsd = min,
                                maxPriceUsd = max
                            )
                        )
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("apply_filter_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                ) {
                    Text("Apply Filters", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
