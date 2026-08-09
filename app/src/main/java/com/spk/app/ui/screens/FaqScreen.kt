package com.spk.app.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spk.app.ui.theme.*

private data class FaqEntry(val question: String, val answer: String)

private val faqEntries = listOf(
    FaqEntry(
        "How does search know what I'm looking for?",
        "The item list is loaded from a shared items.json file that's checked for updates every time the app starts. Only entries marked as real, tradeable items are searchable — junk/duplicate entries in that file are filtered out automatically."
    ),
    FaqEntry(
        "Why did an item's history show sales for a slightly different item?",
        "The market API does a loose text search, so looking up something like \"Armadyl godsword\" can also surface an \"(or)\" variant. This app now filters history down to an exact name match (case doesn't matter, but the letters must line up), so you should only see sales for the exact item you opened."
    ),
    FaqEntry(
        "What does the coin price actually mean?",
        "Some sales are reported in units of 100 million gp per item rather than a flat gp amount. The app detects this automatically and converts it, so the price you see is always the real, true gp value — no math required on your end."
    ),
    FaqEntry(
        "How often does the app check for new sales?",
        "Roughly every 15 minutes — that's the shortest interval Android allows for background work without keeping the app open. Aggressive battery-saver modes on some phones (Samsung, Xiaomi, etc.) can delay it further; whitelisting the app from battery optimization helps."
    ),
    FaqEntry(
        "How does it know something was sold by me?",
        "Add your account name(s) on the Profile page. Every sale the app sees for a watched item is checked against that list — if the seller matches, it counts as yours and you'll get a notification."
    ),
    FaqEntry(
        "What's the quantity field for?",
        "When you watch an item, tell it how many units you're planning to sell. Every matching sale counts down from that number, so you always know how many are left. You can adjust it any time from the watchlist or the item page."
    ),
    FaqEntry(
        "What happens once everything's sold?",
        "If \"Auto-remove fully sold items\" is on (Profile page), the item quietly drops off your watchlist once quantity hits zero. Nothing is lost — a small record is kept so it still shows up under Recently Completed, and the full sale history is always counted in Stats."
    ),
    FaqEntry(
        "Do I need to keep the app open for it to work?",
        "No — background checking runs independently. Just make sure notifications are allowed and the app isn't being aggressively killed by your phone's battery manager."
    ),
)

@Composable
fun FaqScreen(onBack: () -> Unit) {
    var expanded by remember { mutableStateOf(setOf<Int>()) }

    Column(modifier = Modifier.fillMaxSize().background(BgDeep)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Spacer(Modifier.width(4.dp))
            Text("FAQ", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(listOf(AccentMint.copy(alpha = 0.18f), AccentBlue.copy(alpha = 0.14f))))
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.HelpOutline, contentDescription = null, tint = AccentMintSoft, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Got questions?", color = TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Here's how everything under the hood works", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))
            }

            items(faqEntries.size) { index ->
                val entry = faqEntries[index]
                val isExpanded = index in expanded

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(BgCard)
                        .animateContentSize()
                        .clickable {
                            expanded = if (isExpanded) expanded - index else expanded + index
                        }
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            entry.question,
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = null,
                            tint = AccentMint
                        )
                    }
                    if (isExpanded) {
                        Spacer(Modifier.height(8.dp))
                        Text(entry.answer, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
