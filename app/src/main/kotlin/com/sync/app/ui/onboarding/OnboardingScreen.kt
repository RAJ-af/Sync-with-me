package com.sync.app.ui.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun OnboardingScreen(onSwipeUp: () -> Unit) {
    var offsetY by remember { mutableStateOf(0f) }
    var isTouching by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    val intensityMultiplier by animateFloatAsState(
        targetValue = if (isTouching) 2.5f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "intensityMultiplier"
    )

    val expandedScale by animateFloatAsState(
        targetValue = if (offsetY < -400f) 12f else 1f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "expandedScale"
    )

    val screenAlpha by animateFloatAsState(
        targetValue = if (offsetY < -400f) 0f else 1f,
        animationSpec = tween(800),
        label = "screenAlpha",
        finishedListener = { if (it == 0f) onSwipeUp() }
    )

    val liftOffset = (offsetY * 0.2f).dp

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050505))
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
        // Ambient Background Glow
        AmbientGlow(
            modifier = Modifier.fillMaxSize(),
            isTouching = isTouching
        )

        // Syncing Particles
        ParticleField(
            modifier = Modifier.fillMaxSize(),
            syncFactor = offsetY / -400f
        )

        // Multi-layered Organic Waveform
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = liftOffset)
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "breathing")
            val breathScale by infiniteTransition.animateFloat(
                initialValue = 0.95f,
                targetValue = 1.05f,
                animationSpec = infiniteRepeatable(
                    animation = tween(3000, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "breath"
            )

            // Background layers with "drift"
            WaveformAnimation(
                modifier = Modifier
                    .size(320.dp)
                    .alpha(0.2f)
                    .graphicsLayer {
                        val s = expandedScale * 1.2f * breathScale
                        scaleX = s
                        scaleY = s
                        rotationZ = 10f + (offsetY * 0.05f)
                    },
                intensity = intensityMultiplier,
                syncPhase = 0.8f
            )
            WaveformAnimation(
                modifier = Modifier
                    .size(310.dp)
                    .alpha(0.4f)
                    .graphicsLayer {
                        val s = expandedScale * 1.1f * breathScale
                        scaleX = s
                        scaleY = s
                        rotationZ = -5f - (offsetY * 0.03f)
                    },
                intensity = intensityMultiplier,
                syncPhase = 0.5f
            )
            // Primary layer
            WaveformAnimation(
                modifier = Modifier
                    .size(300.dp)
                    .graphicsLayer {
                        val s = expandedScale * breathScale
                        scaleX = s
                        scaleY = s
                    },
                intensity = intensityMultiplier,
                syncPhase = 0f
            )
        }

        // Bottom Subtle Hint (Discovered Interaction)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .offset { IntOffset(0, offsetY.roundToInt()) }
                .graphicsLayer {
                    alpha = (1f + offsetY / 400f).coerceIn(0.2f, 1f)
                }
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "hint")
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 0.7f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse"
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Subtle lift handle
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(2.dp)
                        .alpha(pulseAlpha)
                        .background(Color.White, RoundedCornerShape(1.dp))
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Extremely subtle mood line
                Box(
                    modifier = Modifier
                        .width(240.dp)
                        .height(1.dp)
                        .alpha(0.1f)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, Color.White, Color.Transparent)
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun AmbientGlow(modifier: Modifier = Modifier, isTouching: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val xOffset by infiniteTransition.animateFloat(
        initialValue = -0.2f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "x"
    )
    val yOffset by infiniteTransition.animateFloat(
        initialValue = -0.2f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(11000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "y"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (isTouching) 0.15f else 0.08f,
        label = "glowAlpha"
    )

    Canvas(modifier = modifier) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF39FF14).copy(alpha = glowAlpha), Color.Transparent),
                center = center.copy(
                    x = center.x * (1f + xOffset),
                    y = center.y * (1f + yOffset)
                ),
                radius = size.maxDimension / 2
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF00FFFF).copy(alpha = glowAlpha), Color.Transparent),
                center = center.copy(
                    x = center.x * (1f - xOffset),
                    y = center.y * (1f - yOffset)
                ),
                radius = size.maxDimension / 2
            )
        )
    }
}

@Composable
fun ParticleField(modifier: Modifier = Modifier, syncFactor: Float) {
    val particles = remember {
        List(40) {
            MutableParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                speed = 0.001f + Random.nextFloat() * 0.002f,
                angle = Random.nextFloat() * 2 * Math.PI.toFloat()
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier.alpha(0.4f)) {
        particles.forEach { p ->
            // Move particles
            p.x += Math.cos(p.angle.toDouble()).toFloat() * p.speed
            p.y += Math.sin(p.angle.toDouble()).toFloat() * p.speed

            // Wrap around
            if (p.x < 0) p.x = 1f
            if (p.x > 1) p.x = 0f
            if (p.y < 0) p.y = 1f
            if (p.y > 1) p.y = 0f

            // Pull towards center based on syncFactor
            val dx = 0.5f - p.x
            val dy = 0.5f - p.y
            p.x += dx * syncFactor * 0.05f
            p.y += dy * syncFactor * 0.05f

            drawCircle(
                color = if (p.id % 2 == 0) Color(0xFF39FF14) else Color(0xFF00FFFF),
                radius = 1.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(p.x * size.width, p.y * size.height),
                alpha = 0.3f + (1f - syncFactor) * 0.4f
            )
        }
    }
}

private class MutableParticle(
    val id: Int = Random.nextInt(),
    var x: Float,
    var y: Float,
    var speed: Float,
    var angle: Float
)

@Composable
fun WaveformAnimation(
    modifier: Modifier = Modifier,
    intensity: Float = 1f,
    syncPhase: Float = 0f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")

    val syncFactor by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "syncFactor"
    )

    val barsCount = 30
    val animValues = List(barsCount) { index ->
        val phaseOffset = index * 0.15f + syncPhase * (1f - syncFactor)
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (600 + (index * 25) % 600).toInt(),
                    easing = EaseInOutSine,
                    delayMillis = (phaseOffset * 150).toInt()
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar_$index"
        )
    }

    Canvas(modifier = modifier) {
        val barWidth = size.width / (barsCount * 1.6f)
        val space = barWidth * 0.6f

        inset(vertical = size.height * 0.25f) {
            for (i in 0 until barsCount) {
                val baseHeight = size.height * animValues[i].value
                val barHeight = (baseHeight * intensity).coerceAtMost(size.height)
                val x = i * (barWidth + space)
                val y = (size.height - barHeight) / 2

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF00FFFF).copy(alpha = 0.8f),
                            Color(0xFF39FF14).copy(alpha = 0.9f),
                            Color(0xFF00FFFF).copy(alpha = 0.8f)
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
