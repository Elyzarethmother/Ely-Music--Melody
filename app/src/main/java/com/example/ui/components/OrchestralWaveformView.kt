package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.RoyalViolet
import kotlin.random.Random

@Composable
fun OrchestralWaveformView(
    amplitudes: FloatArray,
    progress: Float, // 0.0f to 1.0f
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onSeekFraction: ((Float) -> Unit)? = null
) {
    val barCount = 36
    val barHeightAnimators = remember {
        List(barCount) { Animatable(0.2f) }
    }

    LaunchedEffect(amplitudes, isPlaying) {
        for (i in 0 until barCount) {
            val ampIndex = (i * amplitudes.size / barCount).coerceIn(0, amplitudes.lastIndex)
            val rawAmp = if (amplitudes.isNotEmpty()) amplitudes[ampIndex] else 0.15f
            val targetHeight = if (isPlaying) {
                (rawAmp * 0.85f + Random.nextFloat() * 0.15f).coerceIn(0.12f, 1.0f)
            } else {
                0.22f
            }

            barHeightAnimators[i].animateTo(
                targetValue = targetHeight,
                animationSpec = tween(durationMillis = 80, easing = FastOutSlowInEasing)
            )
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                    onSeekFraction?.invoke(fraction)
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val totalSpacing = width / barCount
        val barWidth = totalSpacing * 0.65f
        val currentPlayX = width * progress

        // Draw center reference line
        drawLine(
            color = Color.White.copy(alpha = 0.08f),
            start = Offset(0f, height / 2),
            end = Offset(width, height / 2),
            strokeWidth = 1.dp.toPx()
        )

        for (i in 0 until barCount) {
            val barX = i * totalSpacing + (totalSpacing - barWidth) / 2
            val hFraction = barHeightAnimators[i].value
            val barHeight = (height * 0.88f) * hFraction
            val topY = (height - barHeight) / 2

            val isPassed = barX <= currentPlayX
            val barBrush = if (isPassed) {
                Brush.verticalGradient(
                    colors = listOf(
                        GoldPrimary,
                        Color(0xFFFFD54F),
                        RoyalViolet
                    )
                )
            } else {
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4C4465),
                        Color(0xFF2E2648)
                    )
                )
            }

            drawRoundRect(
                brush = barBrush,
                topLeft = Offset(barX, topY),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
            )
        }

        // Draw glowing playback playhead needle
        if (progress in 0f..1f) {
            drawCircle(
                color = GoldPrimary.copy(alpha = 0.35f),
                radius = 7.dp.toPx(),
                center = Offset(currentPlayX, height / 2)
            )
            drawLine(
                color = GoldPrimary,
                start = Offset(currentPlayX, 0f),
                end = Offset(currentPlayX, height),
                strokeWidth = 2.dp.toPx()
            )
            drawCircle(
                color = Color.White,
                radius = 3.5.dp.toPx(),
                center = Offset(currentPlayX, height / 2)
            )
        }
    }
}
