package com.spk.app.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.spk.app.data.repository.ItemsLoadState
import com.spk.app.data.repository.ItemsRepository
import com.spk.app.ui.theme.*

@Composable
fun SplashScreen(onReady: () -> Unit) {
    val context = LocalContext.current
    val repo = remember { ItemsRepository.getInstance(context) }
    val state by repo.loadState.collectAsState()

    var reloadKey by remember { mutableIntStateOf(0) }
    var checkingSeconds by remember { mutableIntStateOf(0) }

    LaunchedEffect(reloadKey) {
        // Watchdog: guarantee the splash can never sit on "Checking…" forever,
        // even if the network call gets stuck in a spot OkHttp timeouts can't reach (DNS).
        val done = try {
            kotlinx.coroutines.withTimeoutOrNull(25_000) { repo.initialize() }
        } catch (ce: kotlinx.coroutines.CancellationException) {
            throw ce
        } catch (t: Throwable) {
            null
        }
        if (done == null) {
            repo.continueOffline()
        }
    }

    LaunchedEffect(state) {
        if (state is ItemsLoadState.Loading) {
            while (true) {
                kotlinx.coroutines.delay(1000)
                checkingSeconds++
            }
        } else {
            checkingSeconds = 0
        }
    }

    LaunchedEffect(state) {
        if (state is ItemsLoadState.Ready) {
            kotlinx.coroutines.delay(350)
            onReady()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "splash-rotate")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing)),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(BgDeep, BgSurface))
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(AccentMint, AccentBlue))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.TrendingUp,
                    contentDescription = null,
                    tint = BgDeep,
                    modifier = Modifier
                        .size(42.dp)
                        .rotate(if (state is ItemsLoadState.Loading) rotation else 0f)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "SpawnPk",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            val message = when (val s = state) {
                is ItemsLoadState.Loading -> s.message
                is ItemsLoadState.Ready -> when {
                    s.updatedFromRemote -> "Item list updated · ${s.itemCount} items"
                    s.updateFailed -> "${s.itemCount} items ready · using saved list"
                    else -> "${s.itemCount} items ready"
                }
                is ItemsLoadState.Error -> s.message
                ItemsLoadState.Idle -> "Starting up…"
            }

            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            if (state is ItemsLoadState.Loading && checkingSeconds >= 5) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "Still checking — your connection seems slow",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary
                )
            }

            Spacer(Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .width(160.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(BgSurfaceElevated)
            ) {
                val fraction = when (state) {
                    is ItemsLoadState.Idle -> 0.1f
                    is ItemsLoadState.Loading -> 0.55f
                    is ItemsLoadState.Ready -> 1f
                    is ItemsLoadState.Error -> 1f
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Brush.horizontalGradient(listOf(AccentMint, AccentBlue)))
                )
            }

            if (state is ItemsLoadState.Loading && checkingSeconds >= 5) {
                Spacer(Modifier.height(20.dp))
                TextButton(onClick = { repo.continueOffline() }) {
                    Text("Continue with saved items", color = AccentMintSoft)
                }
            }

            if (state is ItemsLoadState.Error) {
                Spacer(Modifier.height(20.dp))
                TextButton(onClick = { reloadKey++ }) {
                    Text("Retry", color = AccentMintSoft)
                }
                TextButton(onClick = { repo.continueOffline() }) {
                    Text("Continue offline", color = AccentMintSoft)
                }
            }
        }
    }
}
