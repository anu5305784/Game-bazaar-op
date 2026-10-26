package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.data.local.entities.UserEntity
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.GamerCardBorder
import com.example.ui.theme.GamerSurface
import com.example.ui.theme.GamerTextPrimary
import com.example.ui.theme.GoldLegendary
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple

@Composable
fun GamerBazaarAppBar(
    currentUser: UserEntity?,
    activeCurrency: String,
    onCurrencySelected: (String) -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCurrencyMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(GamerSurface)
            .border(width = 0.5.dp, color = GamerCardBorder)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        GamerBazaarLogo(compact = true)

        Row(verticalAlignment = Alignment.CenterVertically) {
            // Search button
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier
                    .size(38.dp)
                    .testTag("appbar_search_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search listings",
                    tint = GamerTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Currency Selector Chip
            Box {
                val currInfo = CurrencyManager.getCurrency(activeCurrency)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E1B33))
                        .border(1.dp, GamerCardBorder, RoundedCornerShape(8.dp))
                        .clickable { showCurrencyMenu = true }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("currency_selector_chip"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${currInfo.flagEmoji} ${currInfo.code}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan
                    )
                }

                DropdownMenu(
                    expanded = showCurrencyMenu,
                    onDismissRequest = { showCurrencyMenu = false },
                    modifier = Modifier.background(Color(0xFF181528))
                ) {
                    CurrencyManager.SUPPORTED_CURRENCIES.forEach { currency ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(currency.flagEmoji, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "${currency.code} (${currency.symbol})",
                                        fontWeight = if (currency.code == activeCurrency) FontWeight.Bold else FontWeight.Normal,
                                        color = if (currency.code == activeCurrency) NeonPurple else GamerTextPrimary,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(currency.name, fontSize = 11.sp, color = Color.Gray)
                                }
                            },
                            onClick = {
                                onCurrencySelected(currency.code)
                                showCurrencyMenu = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // User Profile / Role avatar
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onProfileClick)
                    .padding(4.dp)
                    .testTag("profile_avatar_button"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentUser != null) {
                    val roleColor = when (currentUser.role.uppercase()) {
                        "ADMIN" -> GoldLegendary
                        "SELLER" -> NeonPurple
                        else -> CyberCyan
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(roleColor.copy(alpha = 0.2f))
                            .border(1.dp, roleColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentUser.displayName.take(1).uppercase(),
                            color = roleColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Sign In",
                        tint = CyberCyan,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}
