package com.florence.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Avatar yer tutucu (SVG çözücü eklenmediği sürece).
 *
 * Backend avatarları SVG dosyalarıdır ve gerçek görüntü için Coil + SVG çözücü
 * gerekir. Bağımlılık riskini sıfırlamak için şimdilik avatar_id'ye göre
 * belirlenen, renkli/tanımlayıcı bir daire çizeriz. Tıklama davranışı ve
 * PUT /profile/avatar akışı gerçek veriye bağlıdır; yalnızca çizim yer tutucudur.
 */
@Composable
fun AvatarArt(
    avatarId: String?,
    size: Dp,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true,
) {
    val index = avatarId
        ?.filter(Char::isDigit)
        ?.toIntOrNull()
        ?.coerceAtLeast(1)
        ?: 1
    val palette = avatarPalettes[(index - 1) % avatarPalettes.size]

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(listOf(palette.first, palette.second))
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (showLabel) {
            Text(
                text = avatarId?.let { id ->
                    when {
                        id.contains("baby") -> "👶"
                        id.contains("graduate") -> "🎓"
                        id.contains("scientist") -> "👨‍🔬"
                        else -> "🙂"
                    }
                } ?: "🙂",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
        } else {
            Text(
                text = "$index",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.9f),
            )
        }
    }
}

/** Avatar kimliğini görsel olarak ayırt etmek için renk paletleri. */
private val avatarPalettes: List<Pair<Color, Color>> = listOf(
    Color(0xFF2563EB) to Color(0xFF06B6D4),
    Color(0xFF7C3AED) to Color(0xFFEC4899),
    Color(0xFF059669) to Color(0xFF10B981),
    Color(0xFFF59E0B) to Color(0xFFF43F5E),
    Color(0xFF0EA5E9) to Color(0xFF6366F1),
    Color(0xFFEF4444) to Color(0xFFF97316),
    Color(0xFF14B8A6) to Color(0xFF84CC16),
    Color(0xFF8B5CF6) to Color(0xFF3B82F6),
    Color(0xFFEC4899) to Color(0xFFF59E0B),
    Color(0xFF06B6D4) to Color(0xFF8B5CF6),
    Color(0xFF22C55E) to Color(0xFF0EA5E9),
    Color(0xFFF43F5E) to Color(0xFF8B5CF6),
)
