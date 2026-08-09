package com.spk.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.spk.app.data.repository.ItemsRepository
import com.spk.app.data.repository.MarketItem
import com.spk.app.ui.theme.*

@Composable
fun SearchScreen(onItemClick: (MarketItem) -> Unit) {
    val context = LocalContext.current
    val repo = remember { ItemsRepository.getInstance(context) }
    val allItems by repo.items.collectAsState()
    var query by remember { mutableStateOf("") }

    val results by remember(query, allItems) {
        derivedStateOf { repo.search(query) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDeep)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(20.dp))
        Text(
            "Search items",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            "${allItems.size} tracked items available",
            style = MaterialTheme.typography.bodySmall,
            color = TextTertiary
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp)),
            placeholder = { Text("e.g. guthix halo", color = TextTertiary) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    Icon(
                        Icons.Filled.Clear,
                        contentDescription = "Clear",
                        tint = TextSecondary,
                        modifier = Modifier.clickable { query = "" }
                    )
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = BgSurfaceElevated,
                unfocusedContainerColor = BgSurface,
                focusedBorderColor = AccentMint,
                unfocusedBorderColor = DividerColor,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = AccentMint
            )
        )

        Spacer(Modifier.height(16.dp))

        if (results.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                Text(
                    if (query.isBlank()) "Start typing to search the market" else "No items match \"$query\"",
                    color = TextTertiary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(results, key = { it.name }) { item ->
                    ItemRow(item = item, onClick = { onItemClick(item) })
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun ItemRow(item: MarketItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(BgSurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.ShowChart, contentDescription = null, tint = AccentMint, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.name.replaceFirstChar { it.uppercase() },
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "ID ${item.itemId}",
                color = TextTertiary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
