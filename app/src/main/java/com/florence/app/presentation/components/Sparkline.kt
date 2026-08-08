package com.florence.app.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

/**
 * Mini çizgi grafik (sparkline): fiyat serisini normalize edip
 * degrade dolgulu + çizgili olarak çizer. Boyut küçük tutulur (hero kartlar).
 */
@Composable
fun Sparkline(
    values: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF2563EB),
    fill: Boolean = true,
) {
    if (values.size < 2) return
    val minV = values.minOrNull() ?: 0f
    val maxV = values.maxOrNull() ?: 1f
    val range = max(maxV - minV, 0.0001f)

    Canvas(modifier = modifier) {
        val stepX = size.width / (values.size - 1)
        val path = Path()
        values.forEachIndexed { i, v ->
            val x = i * stepX
            val y = size.height - ((v - minV) / range) * size.height
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        if (fill) {
            val fillPath = Path().apply {
                addPath(path)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.25f), lineColor.copy(alpha = 0f)),
                ),
            )
        }
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round),
        )
        // Son fiyat noktası
        val lastX = size.width
        val lastY = size.height - ((values.last() - minV) / range) * size.height
        drawCircle(color = lineColor, radius = 3.dp.toPx(), center = Offset(lastX, lastY))
    }
}

/** Serinin son değere göre rengi: son > ilk ise yükseliş rengi. */
fun sparklineColor(values: List<Float>, up: Color, down: Color): Color {
    if (values.size < 2) return up
    val first = values.first()
    val last = values.last()
    return if (last >= first) up else down
}
