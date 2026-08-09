package com.spk.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spk.app.data.repository.SalesRepository
import com.spk.app.ui.components.CoinPrice
import com.spk.app.ui.theme.*
import com.spk.app.util.PriceUtils

@Composable
fun StatsScreen() {
    val context = LocalContext.current
    val repo = remember { SalesRepository.getInstance(context) }
    val matchedSales by repo.observeMatchedSales().collectAsState(initial = emptyList())

    val totalValue = matchedSales.sumOf { PriceUtils.totalPrice(it.price, it.currency, it.amount) }
    val totalCount = matchedSales.size
    val perAccount = matchedSales.groupBy { it.matchedAccount ?: "Unknown" }
        .mapValues { (_, sales) -> sales.sumOf { PriceUtils.totalPrice(it.price, it.currency, it.amount) } to sales.size }
        .toList()
        .sortedByDescending { it.second.first }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BgDeep).padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 90.dp)
    ) {
        item {
            Text("Stats", style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
            Text("Everything you've sold, across every account", color = TextTertiary, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(BgCard)
                        .padding(16.dp)
                ) {
                    CoinPrice(value = totalValue, iconSize = 18.dp, style = MaterialTheme.typography.titleLarge, color = AccentMint)
                    Spacer(Modifier.height(4.dp))
                    Text("Total value", color = TextTertiary, style = MaterialTheme.typography.bodySmall)
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(BgCard)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Filled.Sell, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.height(10.dp))
                    Text(totalCount.toString(), color = TextPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Items sold", color = TextTertiary, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("By account", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(Modifier.height(10.dp))
        }

        if (perAccount.isEmpty()) {
            item {
                Text(
                    "No matched sales yet. Add your account names in Profile and watch some items — this fills in automatically.",
                    color = TextTertiary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            items(perAccount, key = { it.first }) { (account, stats) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(BgCard)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(account, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                        Text("${stats.second} sales", color = TextTertiary, style = MaterialTheme.typography.bodySmall)
                    }
                    CoinPrice(value = stats.first, color = AccentMint, style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        item {
            Spacer(Modifier.height(24.dp))
            Text("Recent activity", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(Modifier.height(10.dp))
        }

        if (matchedSales.isEmpty()) {
            item { Text("Nothing here yet", color = TextTertiary, style = MaterialTheme.typography.bodySmall) }
        } else {
            items(matchedSales.take(50), key = { it.id }) { sale ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(BgCard)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(sale.itemName.replaceFirstChar { it.uppercase() }, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
                        Text("sold by ${sale.matchedAccount}", color = TextTertiary, style = MaterialTheme.typography.bodySmall)
                    }
                    CoinPrice(value = PriceUtils.totalPrice(sale.price, sale.currency, sale.amount), style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
