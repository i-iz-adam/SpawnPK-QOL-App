package com.spk.app.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.spk.app.R
import com.spk.app.ui.theme.TextPrimary
import com.spk.app.util.PriceUtils

/**
 * Coin icon + a formatted gp value (100k, 100.5m, etc). Used everywhere a price is shown.
 *
 * Loaded as a raw ImageBitmap (rather than via painterResource) so we can force
 * FilterQuality.None and keep the pixel-art coin icon crisp instead of blurred when scaled.
 */
@Composable
fun CoinPrice(
    value: Long,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = TextPrimary,
    iconSize: androidx.compose.ui.unit.Dp = 16.dp
) {
    val context = LocalContext.current
    val coinPainter = remember {
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_coin).asImageBitmap()
        BitmapPainter(bitmap, filterQuality = FilterQuality.None)
    }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = coinPainter,
            contentDescription = "gp",
            modifier = Modifier.width(iconSize)
        )
        Spacer(Modifier.width(5.dp))
        Text(PriceUtils.format(value), style = style, color = color)
    }
}