package com.florence.app.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

/**
 * Web arayüzündeki (project-florence/web) el yapımı temalarla birebir eşleşen
 * Material 3 paletleri. Renkler src/config/themes.ts'ten alınmıştır.
 */
@Immutable
data class FlorencePalette(
    val name: String,
    val isDark: Boolean,
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val background: Color,
    val surface: Color,
)

object FlorencePalettes {
    val Florence = FlorencePalette(
        name = "Florence", isDark = true,
        primary = Color(0xFF2563EB), secondary = Color(0xFFF59E0B),
        tertiary = Color(0xFF22C55E), background = Color(0xFF0A0A0F), surface = Color(0xFF14141C),
    )
    val Ocean = FlorencePalette(
        name = "Okyanus", isDark = true,
        primary = Color(0xFF06B6D4), secondary = Color(0xFF2DD4BF),
        tertiary = Color(0xFF22C55E), background = Color(0xFF0A0F1A), surface = Color(0xFF12202E),
    )
    val Emerald = FlorencePalette(
        name = "Zümrüt", isDark = true,
        primary = Color(0xFF10B981), secondary = Color(0xFF34D399),
        tertiary = Color(0xFF22C55E), background = Color(0xFF0A0F0F), surface = Color(0xFF12201C),
    )
    val Midnight = FlorencePalette(
        name = "Geceyarısı", isDark = true,
        primary = Color(0xFF8B5CF6), secondary = Color(0xFFA78BFA),
        tertiary = Color(0xFF22C55E), background = Color(0xFF0A0A0F), surface = Color(0xFF171226),
    )
    val Sunset = FlorencePalette(
        name = "Günbatımı", isDark = true,
        primary = Color(0xFFF59E0B), secondary = Color(0xFFFB923C),
        tertiary = Color(0xFF22C55E), background = Color(0xFF0F0F0A), surface = Color(0xFF1C170F),
    )
    val Sepia = FlorencePalette(
        name = "Sepya", isDark = false,
        primary = Color(0xFF8F5A1E), secondary = Color(0xFF4F7A5A),
        tertiary = Color(0xFF4A8A5C), background = Color(0xFFECE1CC), surface = Color(0xFFF4ECDC),
    )

    val all: List<FlorencePalette> = listOf(Florence, Ocean, Emerald, Midnight, Sunset, Sepia)
}

/** Borsa renkleri: yükseliş / düşüş (web'deki themes.ts up/downColor ile aynı). */
val UpColor = Color(0xFF22C55E)
val DownColor = Color(0xFFEF4444)

@Composable
fun FlorenceTheme(
    palette: FlorencePalette = FlorencePalettes.Florence,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (palette.isDark) {
        darkColorScheme(
            primary = palette.primary,
            secondary = palette.secondary,
            tertiary = palette.tertiary,
            background = palette.background,
            surface = palette.surface,
            surfaceVariant = palette.surface,
        )
    } else {
        lightColorScheme(
            primary = palette.primary,
            secondary = palette.secondary,
            tertiary = palette.tertiary,
            background = palette.background,
            surface = palette.surface,
            onPrimary = Color.White,
        )
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
