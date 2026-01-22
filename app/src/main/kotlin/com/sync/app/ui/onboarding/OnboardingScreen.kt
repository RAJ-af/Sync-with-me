package com.sync.app.ui.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun OnboardingScreen(onSwipeUp: () -> Unit) {
    var offsetY by remember { mutableStateOf(0f) }
    var isTouching by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val intensityMultiplier by animateFloatAsState(
        targetValue = if (isTouching) 2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "intensityMultiplier"
    )

    val expandedScale by animateFloatAsState(
        targetValue = if (offsetY < -400f) 10f else 1f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "expandedScale"
    )

    val screenAlpha by animateFloatAsState(
        targetValue = if (offsetY < -400f) 0f else 1f,
        animationSpec = tween(600),
        label = "screenAlpha",
        finishedListener = { if (it == 0f) onSwipeUp() }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = {
                        isTouching = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        offsetY = (offsetY + dragAmount).coerceAtMost(0f)
                    },
                    onDragEnd = {
                        isTouching = false
                        if (offsetY >= -400f) {
                            offsetY = 0f
                        }
                    },
                    onDragCancel = {
                        isTouching = false
                        offsetY = 0f
                    }
                )
            }
            .alpha(screenAlpha)
    ) {
        // Multi-layered Waveform Animation
        Box(modifier = Modifier.align(Alignment.Center)) {
            // Background layers
            WaveformAnimation(
                modifier = Modifier
                    .size(320.dp)
                    .alpha(0.3f)
                    .graphicsLayer {
                        scaleX = expandedScale * 1.1f
                        scaleY = expandedScale * 1.1f
                        rotationZ = 5f
                    },
                intensity = intensityMultiplier,
                syncPhase = 0.7f
            )
            WaveformAnimation(
                modifier = Modifier
                    .size(310.dp)
                    .alpha(0.5f)
                    .graphicsLayer {
                        scaleX = expandedScale * 1.05f
                        scaleY = expandedScale * 1.05f
                        rotationZ = -3f
                    },
                intensity = intensityMultiplier,
                syncPhase = 0.4f
            )
            // Primary layer
            WaveformAnimation(
                modifier = Modifier
                    .size(300.dp)
                    .graphicsLayer {
                        scaleX = expandedScale
                        scaleY = expandedScale
                    },
                intensity = intensityMultiplier,
                syncPhase = 0f
            )
        }

        // Bottom Swipe Area
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
                .offset { IntOffset(0, offsetY.roundToInt()) },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ambient Mood Line
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, Color.White.copy(alpha = 0.05f), Color.Transparent)
                        )
                    )
            )

            Spacer(modifier = Modifier.height(32.dp))

            SwipeUpBar()
        }
    }
}

@Composable
fun WaveformAnimation(
    modifier: Modifier = Modifier,
    intensity: Float = 1f,
    syncPhase: Float = 0f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")

    // Convergence factor: slowly goes from 0 (out of sync) to 1 (synced)
    val syncFactor by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "syncFactor"
    )

    val barsCount = 30
    val animValues = List(barsCount) { index ->
        val phaseOffset = index * 0.1f + syncPhase * (1f - syncFactor)
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (500 + (index * 20) % 500).toInt(),
                    easing = FastOutSlowInEasing,
                    delayMillis = (phaseOffset * 100).toInt()
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$index"
        )
    }

    Canvas(modifier = modifier) {
        val barWidth = size.width / (barsCount * 1.5f)
        val space = barWidth * 0.5f

        inset(vertical = size.height * 0.2f) {
            for (i in 0 until barsCount) {
                val baseHeight = size.height * animValues[i].value
                val barHeight = (baseHeight * intensity).coerceAtMost(size.height)
                val x = i * (barWidth + space)
                val y = (size.height - barHeight) / 2

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF00FFFF),
                            Color(0xFF39FF14),
                            Color(0xFF00FFFF)
                        )
                    ),
                    topLeft = androidx.compose.ui.geometry.Offset(x, y),
                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2)
                )
            }
        }
    }
}

@Composable
fun SwipeUpBar(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "swipeHint")
    val hintOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hintOffset"
    )

    Column(
        modifier = modifier.offset(y = hintOffset.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(4.dp)
                .background(Color.White.copy(alpha = 0.6f), RoundedCornerShape(2.dp))
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Swipe up to join the vibe",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 2.sp
        )
    }
}
