package com.florence.app.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.florence.app.core.theme.BrandGradient
import com.florence.app.core.theme.DownColor
import com.florence.app.core.theme.TextSecondary
import com.florence.app.core.theme.UpColor
import java.util.Locale

/** Yuvarlatılmış, ince çerçeveli Florence kartı. */
@Composable
fun FlorenceCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(16.dp)
    val base = Modifier
        .clip(shape)
        .background(MaterialTheme.colorScheme.surfaceContainer)
        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
    val clickable = if (onClick != null) {
        base.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
        )
    } else base
    Box(modifier = modifier.then(clickable)) { content() }
}

/** Marka logosu: gradyan zemin üzerinde mum grafik motif. */
@Composable
fun LogoMark(size: Dp = 40.dp) {
    Canvas(modifier = Modifier.size(size)) {
        val r = size.toPx() * 0.28f
        drawRoundRect(
            brush = BrandGradient,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r),
        )
        val w = size.toPx()
        val barW = w * 0.09f
        val gap = w * 0.10f
        val bars = listOf(0.38f, 0.55f, 0.30f, 0.68f, 0.48f)
        val baseY = w * 0.78f
        bars.forEachIndexed { i, h ->
            val x = w * 0.18f + i * (barW + gap)
            drawRoundRect(
                color = Color.White,
                topLeft = androidx.compose.ui.geometry.Offset(x, baseY - w * h),
                size = androidx.compose.ui.geometry.Size(barW, w * h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barW * 0.35f, barW * 0.35f),
            )
        }
    }
}

/** Fiyat + yüzde değişim; renk otomatik (yeşil/kırmızı). */
@Composable
fun PriceText(
    price: Double?,
    changePct: Double?,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleMedium,
    showChange: Boolean = true,
) {
    val positive = (changePct ?: 0.0) >= 0
    val color = when {
        changePct == null -> MaterialTheme.colorScheme.onSurface
        positive -> UpColor
        else -> DownColor
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = if (price != null) formatPrice(price) else "—",
            style = style,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        if (showChange && changePct != null) {
            Text(
                text = String.format(Locale.US, "%+.2f%%", changePct),
                style = MaterialTheme.typography.labelMedium,
                color = color,
            )
        }
    }
}

/** Yeşil/kırmızı değişim rozeti. */
@Composable
fun ChangePill(changePct: Double?, modifier: Modifier = Modifier) {
    if (changePct == null) return
    val positive = changePct >= 0
    val color = if (positive) UpColor else DownColor
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = String.format(Locale.US, "%+.2f%%", changePct),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
        if (actionText != null && onAction != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onAction)
                    .padding(4.dp),
            )
        }
    }
}

/** İstatistik kartı (pano üstü). */
@Composable
fun StatCard(
    label: String,
    value: String,
    sub: String? = null,
    subColor: Color = TextSecondary,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
) {
    FlorenceCard(modifier = modifier.width(148.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = accent,
                maxLines = 1,
            )
            if (sub != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = sub,
                    style = MaterialTheme.typography.labelSmall,
                    color = subColor,
                    maxLines = 1,
                )
            }
        }
    }
}

/** Yükleme iskeleti (pulse animasyonlu). */
@Composable
fun SkeletonBox(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.75f,
        animationSpec = infiniteRepeatable(tween(700, easing = LinearEasing), RepeatMode.Reverse),
        label = "skeletonAlpha",
    )
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = alpha)),
    )
}

@Composable
fun EmptyState(text: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}

/** Değişim oku + renk (küçük liste satırları için). */
@Composable
fun ChangeArrow(changePct: Double?, modifier: Modifier = Modifier) {
    if (changePct == null) return
    val positive = changePct >= 0
    val color = if (positive) UpColor else DownColor
    Text(
        text = if (positive) "▲" else "▼",
        style = MaterialTheme.typography.labelMedium,
        color = color,
        modifier = modifier,
    )
}

// ---- Format yardımcıları ----

fun formatPrice(value: Double): String =
    if (value >= 1000) String.format(Locale.US, "%,.2f", value)
    else String.format(Locale.US, "%.2f", value)

/** Piyasa değeri vb. için kompakt format: 423006371840 → "423,0 Mr" */
fun formatCompact(value: Double?): String {
    if (value == null) return "—"
    return when {
        value >= 1e12 -> String.format(Locale.US, "%.1f T", value / 1e12)
        value >= 1e9 -> String.format(Locale.US, "%.1f Mr", value / 1e9)
        value >= 1e6 -> String.format(Locale.US, "%.1f Mn", value / 1e6)
        else -> String.format(Locale.US, "%.0f", value)
    }
}

/** Görünmez tıklama sarmalayıcısı (kart içi butonlar). */
@Composable
fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.then(
        Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
        ),
    )
