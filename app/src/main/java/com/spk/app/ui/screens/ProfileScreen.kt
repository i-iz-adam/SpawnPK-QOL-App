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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.spk.app.data.db.entity.WatchedItemEntity
import com.spk.app.data.repository.SalesRepository
import com.spk.app.data.repository.SettingsRepository
import com.spk.app.ui.components.QuantityDialog
import com.spk.app.ui.theme.*
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.Date

@Composable
fun ProfileScreen(
    onItemClick: (String) -> Unit,
    onNavigateFaq: () -> Unit,
    onNavigateDonate: () -> Unit
) {
    val context = LocalContext.current
    val repo = remember { SalesRepository.getInstance(context) }
    val settings = remember { SettingsRepository.getInstance(context) }
    val scope = rememberCoroutineScope()

    val accounts by repo.observeTrackedAccounts().collectAsState(initial = emptyList())
    val watched by repo.observeWatchedItems().collectAsState(initial = emptyList())
    val completed by repo.observeCompletedWatches().collectAsState(initial = emptyList())
    val autoRemove by settings.autoRemoveCompleted.collectAsState()

    var newAccount by remember { mutableStateOf("") }
    var editingItem by remember { mutableStateOf<WatchedItemEntity?>(null) }

    editingItem?.let { item ->
        QuantityDialog(
            initialValue = item.quantity,
            title = "Update quantity",
            confirmLabel = "Update",
            onDismiss = { editingItem = null },
            onConfirm = { qty ->
                scope.launch {
                    repo.updateQuantity(item.itemName, qty)
                    editingItem = null
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(BgDeep).padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 20.dp, bottom = 90.dp)
    ) {
        item {
            Text("Profile", style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
            Text(
                "Your account names & watchlist",
                color = TextTertiary,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(20.dp))

            Text("Your accounts", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Text(
                "Sales are matched against these names to detect what you've sold",
                color = TextTertiary,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newAccount,
                    onValueChange = { newAccount = it },
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(14.dp)),
                    placeholder = { Text("account name", color = TextTertiary) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
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
                Spacer(Modifier.width(8.dp))
                FilledIconButton(
                    onClick = {
                        val nameToAdd = newAccount.trim()
                        if (nameToAdd.isNotBlank()) {
                            newAccount = ""
                            scope.launch { repo.addAccount(nameToAdd) }
                        }
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = AccentMint)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add account", tint = BgDeep)
                }
            }

            Spacer(Modifier.height(12.dp))
        }

        if (accounts.isEmpty()) {
            item {
                Text("No accounts added yet", color = TextTertiary, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(20.dp))
            }
        } else {
            items(accounts, key = { it.name }) { account ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(BgCard)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(32.dp).clip(CircleShape).background(BgSurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Person, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(account.name, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
                    }
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Remove",
                        tint = TextTertiary,
                        modifier = Modifier.clickable { scope.launch { repo.removeAccount(account.name) } }
                    )
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(BgCard)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Auto-remove fully sold items", color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "Once quantity hits 0, take it off the watchlist automatically",
                        color = TextTertiary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(
                    checked = autoRemove,
                    onCheckedChange = { settings.setAutoRemoveCompleted(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = BgDeep,
                        checkedTrackColor = AccentMint,
                        uncheckedThumbColor = TextTertiary,
                        uncheckedTrackColor = BgSurfaceElevated
                    )
                )
            }
            Spacer(Modifier.height(24.dp))

            Text("Watchlist", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Text(
                "Items you're tracking · tap the row to open, adjust quantity inline",
                color = TextTertiary,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(10.dp))
        }

        if (watched.isEmpty()) {
            item {
                Text("Nothing watched yet. Search for an item and tap the bookmark icon.", color = TextTertiary, style = MaterialTheme.typography.bodySmall)
            }
        } else {
            items(watched, key = { it.itemName }) { w ->
                WatchedItemRow(
                    item = w,
                    onOpen = { onItemClick(w.itemName) },
                    onDecrement = { scope.launch { repo.updateQuantity(w.itemName, (w.quantity - 1).coerceAtLeast(0)) } },
                    onIncrement = { scope.launch { repo.updateQuantity(w.itemName, w.quantity + 1) } },
                    onEditQuantity = { editingItem = w },
                    onRemove = { scope.launch { repo.unwatchItem(w.itemName) } }
                )
            }
        }

        if (completed.isNotEmpty()) {
            item {
                Spacer(Modifier.height(24.dp))
                Text("Recently completed", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Text(
                    "Auto-removed once fully sold · full history still counted in Stats",
                    color = TextTertiary,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(10.dp))
            }
            items(completed, key = { it.id }) { c ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(BgCard)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = AccentMint, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(c.itemName.replaceFirstChar { it.uppercase() }, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
                            Text("${c.quantitySold} sold", color = TextTertiary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Text(
                        DateFormat.getDateInstance(DateFormat.SHORT).format(Date(c.completedAt)),
                        color = TextTertiary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(28.dp))
            Text("More", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            Spacer(Modifier.height(10.dp))

            NavRow(icon = Icons.Filled.HelpOutline, label = "FAQ", accent = AccentBlue, onClick = onNavigateFaq)
            Spacer(Modifier.height(8.dp))
            NavRow(icon = Icons.Filled.Favorite, label = "Support this project", accent = AccentGold, onClick = onNavigateDonate)
        }
    }
}

@Composable
private fun WatchedItemRow(
    item: WatchedItemEntity,
    onOpen: () -> Unit,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    onEditQuantity: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(BgCard)
            .clickable(onClick = onOpen)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            item.itemName.replaceFirstChar { it.uppercase() },
            color = TextPrimary,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecrement, modifier = Modifier.size(30.dp)) {
                Icon(Icons.Filled.Remove, contentDescription = "Decrease", tint = TextSecondary, modifier = Modifier.size(16.dp))
            }
            Text(
                item.quantity.toString(),
                color = AccentMintSoft,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .clickable(onClick = onEditQuantity)
                    .padding(horizontal = 6.dp)
            )
            IconButton(onClick = onIncrement, modifier = Modifier.size(30.dp)) {
                Icon(Icons.Filled.Add, contentDescription = "Increase", tint = TextSecondary, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.Filled.Close,
                contentDescription = "Stop watching",
                tint = TextTertiary,
                modifier = Modifier.clickable(onClick = onRemove).size(18.dp)
            )
        }
    }
}

@Composable
private fun NavRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, accent: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BgCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(label, color = TextPrimary, style = MaterialTheme.typography.bodyLarge)
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextTertiary)
    }
}