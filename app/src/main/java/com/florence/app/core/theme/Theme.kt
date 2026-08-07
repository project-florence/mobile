package com.florence.app.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.florence.app.R

/**
 * Web arayüzündeki (project-florence/web) temalarla birebir eşleşen paletler.
 * Renkler src/config/themes.ts'ten alınmıştır.
 */
@Immutable
data class FlorencePalette(
    val key: String,
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
        key = "florence", name = "Florence", isDark = true,
        primary = Color(0xFF2563EB), secondary = Color(0xFFF59E0B),
        tertiary = Color(0xFF22C55E), background = Color(0xFF0A0A0F), surface = Color(0xFF14141C),
    )
    val Ocean = FlorencePalette(
        key = "ocean", name = "Okyanus", isDark = true,
        primary = Color(0xFF06B6D4), secondary = Color(0xFF2DD4BF),
        tertiary = Color(0xFF22C55E), background = Color(0xFF0A0F1A), surface = Color(0xFF12202E),
    )
    val Emerald = FlorencePalette(
        key = "emerald", name = "Zümrüt", isDark = true,
        primary = Color(0xFF10B981), secondary = Color(0xFF34D399),
        tertiary = Color(0xFF22C55E), background = Color(0xFF0A0F0F), surface = Color(0xFF12201C),
    )
    val Midnight = FlorencePalette(
        key = "midnight", name = "Geceyarısı", isDark = true,
        primary = Color(0xFF8B5CF6), secondary = Color(0xFFA78BFA),
        tertiary = Color(0xFF22C55E), background = Color(0xFF0A0A0F), surface = Color(0xFF171226),
    )
    val Sunset = FlorencePalette(
        key = "sunset", name = "Günbatımı", isDark = true,
        primary = Color(0xFFF59E0B), secondary = Color(0xFFFB923C),
        tertiary = Color(0xFF22C55E), background = Color(0xFF0F0F0A), surface = Color(0xFF1C170F),
    )
    val Sepia = FlorencePalette(
        key = "sepia", name = "Sepya", isDark = false,
        primary = Color(0xFF8F5A1E), secondary = Color(0xFF4F7A5A),
        tertiary = Color(0xFF4A8A5C), background = Color(0xFFECE1CC), surface = Color(0xFFF4ECDC),
    )

    val all: List<FlorencePalette> = listOf(Florence, Ocean, Emerald, Midnight, Sunset, Sepia)
}

/** Borsa renkleri: yükseliş / düşüş (web'deki themes.ts up/downColor ile aynı). */
val UpColor = Color(0xFF22C55E)
val DownColor = Color(0xFFEF4444)

/** Web'deki onSurfaceVariant karşılığı (ikincil metin). */
val TextSecondary = Color(0xFF94A3B8)

/** Marka gradyanı: web'in primary(#2563EB) → cyan geçişi. */
val BrandGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF2563EB), Color(0xFF3B82F6), Color(0xFF06B6D4)),
)

val FlorenceFontFamily = FontFamily(
    Font(R.font.inter, FontWeight.Light),
    Font(R.font.inter, FontWeight.Normal),
    Font(R.font.inter, FontWeight.Medium),
    Font(R.font.inter, FontWeight.SemiBold),
    Font(R.font.inter, FontWeight.Bold),
    Font(R.font.inter, FontWeight.ExtraBold),
)

private val FlorenceTypography = Typography(
    displaySmall = TextStyle(fontFamily = FlorenceFontFamily, fontWeight = FontWeight.ExtraBold, fontSize = 36.sp, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontFamily = FlorenceFontFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp, letterSpacing = (-0.3).sp),
    headlineSmall = TextStyle(fontFamily = FlorenceFontFamily, fontWeight = FontWeight.Bold, fontSize = 24.sp),
    titleLarge = TextStyle(fontFamily = FlorenceFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    titleMedium = TextStyle(fontFamily = FlorenceFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    titleSmall = TextStyle(fontFamily = FlorenceFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    bodyLarge = TextStyle(fontFamily = FlorenceFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = FlorenceFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = FlorenceFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = FlorenceFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = FlorenceFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = FlorenceFontFamily, fontWeight = FontWeight.Medium, fontSize = 10.sp, letterSpacing = 0.4.sp),
)

@Composable
fun FlorenceTheme(
    palette: FlorencePalette = FlorencePalettes.Florence,
    content: @Composable () -> Unit,
) {
    val surface = palette.surface
    val scheme = if (palette.isDark) {
        darkColorScheme(
            primary = palette.primary,
            onPrimary = Color.White,
            primaryContainer = palette.primary.copy(alpha = 0.35f),
            onPrimaryContainer = Color(0xFFDBEAFE),
            secondary = palette.secondary,
            onSecondary = Color(0xFF1A1206),
            tertiary = palette.tertiary,
            background = palette.background,
            onBackground = Color(0xFFE8EAF0),
            surface = surface,
            onSurface = Color(0xFFE8EAF0),
            surfaceVariant = lerp(surface, Color.White, 0.05f),
            onSurfaceVariant = TextSecondary,
            surfaceContainerLowest = lerp(surface, Color.Black, 0.10f),
            surfaceContainerLow = lerp(surface, Color.Black, 0.04f),
            surfaceContainer = surface,
            surfaceContainerHigh = lerp(surface, Color.White, 0.06f),
            surfaceContainerHighest = lerp(surface, Color.White, 0.12f),
            outline = lerp(palette.primary, Color.White, 0.62f),
            outlineVariant = lerp(palette.primary, Color.Black, 0.75f),
            error = DownColor,
            onError = Color.White,
            errorContainer = Color(0xFF3B0D0D),
            onErrorContainer = Color(0xFFFECACA),
        )
    } else {
        lightColorScheme(
            primary = palette.primary,
            onPrimary = Color.White,
            primaryContainer = palette.primary.copy(alpha = 0.20f),
            onPrimaryContainer = Color(0xFF1E1B16),
            secondary = palette.secondary,
            onSecondary = Color.White,
            tertiary = palette.tertiary,
            background = palette.background,
            onBackground = Color(0xFF1F1B16),
            surface = surface,
            onSurface = Color(0xFF1F1B16),
            surfaceVariant = lerp(surface, Color.White, 0.30f),
            onSurfaceVariant = Color(0xFF6B6256),
            surfaceContainerLowest = lerp(surface, Color.White, 0.35f),
            surfaceContainerLow = lerp(surface, Color.White, 0.20f),
            surfaceContainer = surface,
            surfaceContainerHigh = lerp(surface, Color.White, 0.12f),
            surfaceContainerHighest = lerp(surface, Color.White, 0.05f),
            outline = lerp(palette.primary, Color.Black, 0.55f),
            outlineVariant = lerp(palette.primary, Color.White, 0.55f),
            error = DownColor,
            onError = Color.White,
            errorContainer = Color(0xFFFECACA),
            onErrorContainer = Color(0xFF3B0D0D),
        )
    }
    MaterialTheme(colorScheme = scheme, typography = FlorenceTypography, content = content)
}
