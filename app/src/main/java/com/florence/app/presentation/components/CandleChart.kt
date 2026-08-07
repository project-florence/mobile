package com.florence.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.florence.app.core.theme.DownColor
import com.florence.app.core.theme.TextSecondary
import com.florence.app.core.theme.UpColor
import com.florence.app.data.model.Candle
import java.util.Locale

/**
 * Saf Compose Canvas ile çizilen mum grafiği (web'deki klinecharts karşılığı).
 * Yeşil/kırmızı renkler web'in themes.ts up/downColor değerleridir.
 */
@Composable
fun CandleChart(
    candles: List<Candle>,
    modifier: Modifier = Modifier,
) {
    if (candles.isEmpty()) return

    val minPrice = candles.mapNotNull { it.low }.minOrNull() ?: return
    val maxPrice = candles.mapNotNull { it.high }.maxOrNull() ?: return
    val range = (maxPrice - minPrice).takeIf { it > 0 } ?: 1.0
    val pad = range * 0.06
    val lo = minPrice - pad
    val hi = maxPrice + pad
    val totalRange = hi - lo

    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val last = candles.last()
    val lastClose = last.close

    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = String.format(Locale.US, "%.2f", maxPrice),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = String.format(Locale.US, "%.2f", minPrice),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
            )
        }
        Spacer(Modifier.height(4.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
        ) {
            val step = size.height / 4f
            // Yatay ızgara çizgileri
            for (i in 0..4) {
                val y = i * step
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f,
                )
            }

            val n = candles.size
            val slotW = size.width / n
            val bodyW = (slotW * 0.62f).coerceAtMost(14f)
            val wickW = 1.2f

            candles.forEachIndexed { index, c ->
                val open = c.open ?: return@forEachIndexed
                val high = c.high ?: return@forEachIndexed
                val low = c.low ?: return@forEachIndexed
                val close = c.close ?: return@forEachIndexed
                val rising = close >= open
                val color = if (rising) UpColor else DownColor

                val xCenter = slotW * (index + 0.5f)
                val yHigh = ((hi - high) / totalRange * size.height).toFloat()
                val yLow = ((hi - low) / totalRange * size.height).toFloat()
                val yOpen = ((hi - open) / totalRange * size.height).toFloat()
                val yClose = ((hi - close) / totalRange * size.height).toFloat()

                // Fitil (high-low)
                drawLine(
                    color = color,
                    start = Offset(xCenter, yHigh),
                    end = Offset(xCenter, yLow),
                    strokeWidth = wickW,
                )
                // Gövde (open-close)
                val top = minOf(yOpen, yClose)
                val bodyHeight = maxOf(kotlin.math.abs(yClose - yOpen), 1.5f)
                drawRoundRect(
                    color = color,
                    topLeft = Offset(xCenter - bodyW / 2f, top),
                    size = Size(bodyW, bodyHeight),
                    cornerRadius = CornerRadius(1.5f, 1.5f),
                )
            }

            // Son fiyat çizgisi (kesikli)
            val lastY = ((hi - (lastClose ?: return@Canvas)) / totalRange * size.height).toFloat()
            drawLine(
                color = TextSecondary.copy(alpha = 0.8f),
                start = Offset(0f, lastY),
                end = Offset(size.width, lastY),
                strokeWidth = 1.2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)),
            )
        }
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            candles.firstOrNull()?.ts?.let {
                Text(
                    text = it.take(10).substring(5).replace("-", "."),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = String.format(Locale.US, "%.2f", lastClose),
                style = MaterialTheme.typography.labelSmall,
                color = if ((last.close ?: 0.0) >= (last.open ?: 0.0)) UpColor else DownColor,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
