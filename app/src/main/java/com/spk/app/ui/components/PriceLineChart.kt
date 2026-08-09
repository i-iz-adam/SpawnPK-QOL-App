package com.spk.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.spk.app.ui.theme.AccentMint
import com.spk.app.ui.theme.AccentMintSoft
import com.spk.app.ui.theme.DividerColor
import kotlin.math.max
import kotlin.math.min

/**
 * A lightweight, dependency-free line chart for showing price history.
 * `points` should be ordered oldest -> newest.
 */
@Composable
fun PriceLineChart(
    points: List<Long>,
    modifier: Modifier = Modifier,
    lineColor: Color = AccentMint,
    fillColor: Color = AccentMintSoft
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = androidx.compose.animation.core.tween(700))
    }

    Box(modifier = modifier.fillMaxWidth().height(160.dp)) {
        if (points.size < 2) {
            return@Box
        }
        val minVal = points.min()
        val maxVal = points.max()
        val range = max(1L, maxVal - minVal)

        Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
            val w = size.width
            val h = size.height
            val paddingV = 12f

            // grid lines
            val gridSteps = 3
            for (i in 0..gridSteps) {
                val y = paddingV + (h - paddingV * 2) * i / gridSteps
                drawLine(
                    color = DividerColor,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f
                )
            }

            val stepX = if (points.size > 1) w / (points.size - 1) else 0f
            val animatedCount = max(2, (points.size * progress.value).toInt())
            val visible = points.take(min(points.size, animatedCount))

            val path = Path()
            val fillPath = Path()
            visible.forEachIndexed { index, value ->
                val x = index * stepX
                val normalized = (value - minVal).toFloat() / range.toFloat()
                val y = h - paddingV - normalized * (h - paddingV * 2)
                if (index == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, h)
                    fillPath.lineTo(x, y)
                } else {
                    path.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
                if (index == visible.lastIndex) {
                    fillPath.lineTo(x, h)
                    fillPath.close()
                }
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(fillColor.copy(alpha = 0.35f), fillColor.copy(alpha = 0f))
                )
            )
            drawPath(path = path, color = lineColor, style = Stroke(width = 4f))
        }
    }
}
