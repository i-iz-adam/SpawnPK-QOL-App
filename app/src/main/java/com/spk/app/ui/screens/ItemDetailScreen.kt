package com.spk.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.spk.app.data.model.SaleDto
import com.spk.app.data.repository.ItemsRepository
import com.spk.app.data.repository.SalesRepository
import com.spk.app.ui.components.CoinPrice
import com.spk.app.ui.components.PriceLineChart
import com.spk.app.ui.components.QuantityDialog
import com.spk.app.ui.theme.*
import com.spk.app.util.PriceUtils
import com.spk.app.worker.SalesCheckWorker
import kotlinx.coroutines.launch

@Composable
fun ItemDetailScreen(itemName: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val itemsRepo = remember { ItemsRepository.getInstance(context) }
    val salesRepo = remember { SalesRepository.getInstance(context) }
    val scope = rememberCoroutineScope()

    val marketItem = remember(itemName) { itemsRepo.findByName(itemName) }
    var sales by remember { mutableStateOf<List<SaleDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var watched by remember { mutableStateOf(false) }
    var watchedQuantity by remember { mutableStateOf<Int?>(null) }
    var showQuantityDialog by remember { mutableStateOf(false) }

    suspend fun refreshWatchState() {
        watched = salesRepo.isWatched(itemName)
        watchedQuantity = if (watched) salesRepo.getWatchedQuantity(itemName) else null
    }

    LaunchedEffect(itemName) {
        loading = true
        sales = salesRepo.fetchHistory(itemName)
        refreshWatchState()
        loading = false
    }

    if (showQuantityDialog) {
        QuantityDialog(
            initialValue = watchedQuantity ?: 1,
            title = if (watched) "Update quantity" else "How many are you tracking?",
            confirmLabel = if (watched) "Update" else "Watch",
            onDismiss = { showQuantityDialog = false },
            onConfirm = { qty ->
                scope.launch {
                    if (watched) {
                        salesRepo.updateQuantity(itemName, qty)
                    } else {
                        salesRepo.watchItem(itemName, marketItem?.itemId, qty)
                        SalesCheckWorker.runOnceNow(context)
                    }
                    refreshWatchState()
                    showQuantityDialog = false
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(BgDeep)) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Spacer(Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    itemName.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary
                )
                if (watched && watchedQuantity != null) {
                    Text(
                        "watching · $watchedQuantity left to sell · tap to edit",
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentMintSoft
                    )
                }
            }
            IconButton(onClick = { showQuantityDialog = true }) {
                Icon(
                    imageVector = if (watched) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    contentDescription = "Watch",
                    tint = if (watched) AccentMint else TextSecondary
                )
            }
            if (watched) {
                TextButton(onClick = {
                    scope.launch {
                        salesRepo.unwatchItem(itemName)
                        refreshWatchState()
                    }
                }) {
                    Text("Stop", color = AccentRed, style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        if (loading) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 80.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentMint)
            }
            return@Column
        }

        if (sales.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 80.dp), contentAlignment = Alignment.Center) {
                Text("No recent sales found for this item", color = TextTertiary)
            }
            return@Column
        }

        val chronological = sales.reversed() // API returns newest-first
        val latest = sales.first()
        val previous = sales.getOrNull(1)
        val latestUnit = PriceUtils.unitPrice(latest)
        val previousUnit = previous?.let { PriceUtils.unitPrice(it) }
        val delta = previousUnit?.let { latestUnit - it }

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            CoinPrice(
                value = latestUnit,
                style = MaterialTheme.typography.headlineLarge,
                iconSize = 26.dp
            )
            Text(
                "per unit · last sale",
                color = TextTertiary,
                style = MaterialTheme.typography.bodySmall
            )
            if (delta != null) {
                val positive = delta >= 0
                Text(
                    (if (positive) "▲ " else "▼ ") + PriceUtils.format(kotlin.math.abs(delta)) + " vs previous sale",
                    color = if (positive) AccentMint else AccentRed,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(20.dp))

            PriceLineChart(points = chronological.map { PriceUtils.unitPrice(it) })

            Spacer(Modifier.height(24.dp))

            Text("Recent sales", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(Modifier.height(10.dp))
        }

        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sales, key = { it.id }) { sale ->
                SaleRow(sale)
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun SaleRow(sale: SaleDto) {
    val unit = PriceUtils.unitPrice(sale)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BgCard)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            CoinPrice(value = unit, style = MaterialTheme.typography.titleMedium)
            Text(
                "${sale.seller} → ${sale.buyer}" + if (sale.amount > 1) " · x${sale.amount}" else "",
                color = TextSecondary,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Text(
            sale.time.substringBefore("."),
            color = TextTertiary,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
