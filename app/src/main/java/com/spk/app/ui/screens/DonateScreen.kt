package com.spk.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spk.app.AppConfig
import com.spk.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun DonateScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = BgDeep,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(BgDeep)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Spacer(Modifier.width(4.dp))
                Text("Support this project", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.linearGradient(listOf(AccentGold.copy(alpha = 0.22f), AccentMint.copy(alpha = 0.12f))))
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(AccentGold, AccentMint))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Favorite, contentDescription = null, tint = BgDeep, modifier = Modifier.size(28.dp))
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(
                            "This app is free — support is optional",
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "If you'd like to chip in toward this and other projects, reach out on Discord first — everything's arranged there.",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                InfoCard(
                    icon = Icons.Filled.Forum,
                    iconTint = AccentBlue,
                    title = "Message me on Discord",
                    body = "@${AppConfig.DONATE_DISCORD_USERNAME}"
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = {
                                clipboard.setText(AnnotatedString(AppConfig.DONATE_DISCORD_USERNAME))
                                scope.launch { snackbarHostState.showSnackbar("Copied Discord username") }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue, contentColor = BgDeep)
                        ) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Copy username")
                        }

                        if (AppConfig.DONATE_DISCORD_INVITE_URL.isNotBlank()) {
                            Spacer(Modifier.width(10.dp))
                            OutlinedButton(
                                onClick = {
                                    runCatching {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.DONATE_DISCORD_INVITE_URL)))
                                    }
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                            ) {
                                Text("Open Discord")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                InfoCard(
                    icon = Icons.Filled.AccountBalance,
                    iconTint = AccentGold,
                    title = "How it works",
                    body = "Contributions are handled as a direct bank transfer / wire — there's no in-app purchase or card checkout. Payments go straight into a secondary bank account that funds this and other projects, so we'll sort out the details together over Discord."
                )

                Spacer(Modifier.height(14.dp))

                InfoCard(
                    icon = Icons.Filled.Info,
                    iconTint = TextSecondary,
                    title = "No pressure",
                    body = "This app works exactly the same whether or not you ever reach out here. Nothing is gated behind support — this page exists purely for anyone who wants to say thanks."
                )

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun InfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    title: String,
    body: String,
    extra: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(BgCard)
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(title, color = TextPrimary, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(8.dp))
        Text(body, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        if (extra != null) {
            Spacer(Modifier.height(14.dp))
            extra()
        }
    }
}
