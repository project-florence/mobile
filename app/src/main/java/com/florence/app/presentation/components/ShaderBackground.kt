package com.florence.app.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.florence.app.core.theme.FlorencePalettes
import kotlin.math.cos
import kotlin.math.sin

/**
 * Giriş ekranı için "canlı shader" arka planı: sürekli akışkan degrade +
 * dönen ışık hüzmeleri (web'deki FluidBackground/Hyperspace hissi).
 * Renkler yavaşça nefes alır — pil dostu, zarif.
 */
@Composable
fun ShaderBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val transition = rememberInfiniteTransition(label = "shader")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 18_000
                0f at 0 using LinearEasing
                120f at 4500 using LinearEasing
                240f at 9000 using LinearEasing
                360f at 13500 using LinearEasing
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "phase",
    )
    val hueA by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 11_000
                0f at 0 using LinearEasing
                1f at 5500 using LinearEasing
                0f at 11000 using LinearEasing
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "hueA",
    )
    val hueB by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 14_000
                1f at 0 using LinearEasing
                0f at 7000 using LinearEasing
                1f at 14000 using LinearEasing
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "hueB",
    )

    val c1 = lerpColor(FlorencePalettes.Florence.primary, Color(0xFF7C3AED), hueA)
    val c2 = lerpColor(Color(0xFF0EA5E9), Color(0xFF2563EB), hueB)
    val c3 = lerpColor(FlorencePalettes.Florence.background, Color(0xFF0B1020), hueB)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(c1, c2, c3),
                    start = Offset(
                        x = (cos(Math.toRadians(phase.toDouble())) * 0.5f + 0.5f).toFloat(),
                        y = (sin(Math.toRadians(phase.toDouble())) * 0.5f + 0.5f).toFloat(),
                    ),
                    end = Offset(
                        x = (cos(Math.toRadians(phase.toDouble() + 180.0)) * 0.5f + 0.5f).toFloat(),
                        y = (sin(Math.toRadians(phase.toDouble() + 180.0)) * 0.5f + 0.5f).toFloat(),
                    ),
                )
            ),
    ) {
        // Dönen ışık hüzmeleri (nefes alan glow)
        val glow by transition.animateFloat(
            initialValue = 0.12f,
            targetValue = 0.30f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 6_000
                    0.12f at 0 using LinearEasing
                    0.30f at 3000 using LinearEasing
                    0.12f at 6000 using LinearEasing
                },
                repeatMode = RepeatMode.Restart,
            ),
            label = "glow",
        )
        val angle = Math.toRadians(phase.toDouble())
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(FlorencePalettes.Florence.primary.copy(alpha = glow), Color.Transparent),
                    center = Offset(size.width * 0.22f, size.height * 0.15f),
                    radius = size.width * 0.75f,
                ),
                radius = size.width * 0.75f,
                center = Offset(size.width * 0.22f, size.height * 0.15f),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(FlorencePalettes.Florence.secondary.copy(alpha = glow * 0.8f), Color.Transparent),
                    center = Offset(
                        size.width * (0.75f + cos(angle).toFloat() * 0.12f),
                        size.height * (0.85f + sin(angle).toFloat() * 0.12f),
                    ),
                    radius = size.width * 0.65f,
                ),
                radius = size.width * 0.65f,
                center = Offset(
                    size.width * (0.75f + cos(angle).toFloat() * 0.12f),
                    size.height * (0.85f + sin(angle).toFloat() * 0.12f),
                ),
            )
        }
        content()
    }
}

private fun lerpColor(a: Color, b: Color, t: Float): Color = Color(
    red = a.red + (b.red - a.red) * t,
    green = a.green + (b.green - a.green) * t,
    blue = a.blue + (b.blue - a.blue) * t,
    alpha = 1f,
)
