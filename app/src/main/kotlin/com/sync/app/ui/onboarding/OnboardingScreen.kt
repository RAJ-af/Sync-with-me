package com.sync.app.ui.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.random.Random

/**
 * An "Entry Moment" for the Sync app.
 * Motion-first, discovery-based, and ambient.
 * Feels alive and connected to what's happening inside.
 */
@Composable
fun OnboardingScreen(onEnter: () -> Unit) {
    var discoveryProgress by remember { mutableFloatStateOf(0f) }
    var touchOffset by remember { mutableStateOf(Offset.Zero) }
    var isTouching by remember { mutableStateOf(false) }

    // Smoothly animate the discovery progress for visual feedback
    val animatedProgress by animateFloatAsState(
        targetValue = discoveryProgress,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessVeryLow),
        label = "discovery"
    )

    // Trigger entry when threshold is reached
    LaunchedEffect(discoveryProgress) {
        if (discoveryProgress >= 0.99f) {
            onEnter()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        touchOffset = it
                        isTouching = true
                    },
                    onDragEnd = {
                        isTouching = false
                        if (discoveryProgress < 0.99f) {
                            discoveryProgress = 0f
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        touchOffset = change.position
                        // Dragging upwards increases discovery; no labels or buttons needed
                        val delta = -dragAmount.y / size.height.toFloat()
                        discoveryProgress = (discoveryProgress + delta * 1.5f).coerceIn(0f, 1f)
                    }
                )
            }
            .graphicsLayer {
                // Subtle scale and alpha transition as we "push through" the veil
                val scale = 1f + (animatedProgress * 0.1f)
                scaleX = scale
                scaleY = scale
                alpha = 1f - (animatedProgress * 0.5f)
            }
    ) {
        // Ambient Activity - pulsing echoes that suggest a live community inside
        PresenceEchoes()

        // Living Background - drifting ambient light leaks in Cyan and Neon Green
        AmbientLivingBackground(animatedProgress)

        // Reactive Light - follows the user's touch, creating an instinctive bond
        if (isTouching || discoveryProgress > 0f) {
            ReactiveLight(touchOffset, isTouching, animatedProgress)
        }

        // The "Veil" - abstract particles that represent the barrier to the app
        InteractionVeil(animatedProgress)
    }
}

@Composable
fun PresenceEchoes() {
    val infiniteTransition = rememberInfiniteTransition(label = "echoes")

    // Staggered echoes creating a rhythmic, sonar-like effect
    val echoDelays = listOf(0, 1000, 2000)

    echoDelays.forEach { delay ->
        val progress by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, delayMillis = delay, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "echoProgress$delay"
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension * 0.2f + (size.minDimension * 0.8f * progress)
            val alpha = (0.12f * (1f - progress)).coerceAtLeast(0f)

            drawCircle(
                color = Color(0xFFB4FFB4).copy(alpha = alpha), // Neon Green
                radius = radius,
                center = center,
                style = Stroke(width = 0.5.dp.toPx())
            )
        }
    }
}

@Composable
fun AmbientLivingBackground(progress: Float) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient")

    val driftX by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(25000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "driftX"
    )
    val driftY by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(22000, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "driftY"
    )

    Canvas(modifier = Modifier.fillMaxSize().blur(120.dp)) {
        // Cyan ambient glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF00FFFF).copy(alpha = 0.08f + progress * 0.1f), Color.Transparent),
                center = Offset(size.width * driftX, size.height * driftY),
                radius = size.minDimension * 0.8f
            ),
            radius = size.minDimension * 0.8f,
            center = Offset(size.width * driftX, size.height * driftY)
        )

        // Neon Green ambient glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF39FF14).copy(alpha = 0.06f + progress * 0.1f), Color.Transparent),
                center = Offset(size.width * (1f - driftX), size.height * (1f - driftY)),
                radius = size.minDimension * 0.7f
            ),
            radius = size.minDimension * 0.7f,
            center = Offset(size.width * (1f - driftX), size.height * (1f - driftY))
        )
    }
}

@Composable
fun ReactiveLight(offset: Offset, isTouching: Boolean, progress: Float) {
    val alpha by animateFloatAsState(
        targetValue = if (isTouching) 0.4f else if (progress > 0f) 0.2f else 0f,
        animationSpec = tween(600),
        label = "reactiveAlpha"
    )

    Canvas(modifier = Modifier.fillMaxSize().blur(60.dp)) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White.copy(alpha = alpha), Color.Transparent),
                center = offset,
                radius = 200.dp.toPx() * (1f + progress)
            ),
            radius = 200.dp.toPx() * (1f + progress),
            center = offset
        )
    }
}

@Composable
fun InteractionVeil(progress: Float) {
    val particles = remember { List(40) { ParticleData() } }
    val infiniteTransition = rememberInfiniteTransition(label = "veil")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Restart),
        label = "time"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEach { p ->
            val x = (p.x + (time * p.vx) + 1f) % 1f
            val y = (p.y + (time * p.vy) + 1f) % 1f
            val alpha = (p.baseAlpha * (1f - progress)).coerceAtLeast(0f)

            if (alpha > 0.01f) {
                drawCircle(
                    color = p.color.copy(alpha = alpha),
                    radius = p.radius.dp.toPx(),
                    center = Offset(x * size.width, y * size.height)
                )
            }
        }

        // Final transition mask
        if (progress > 0.9f) {
            drawRect(
                color = Color.Black.copy(alpha = (progress - 0.9f) * 10f),
                size = size
            )
        }
    }
}

private class ParticleData {
    val x = Random.nextFloat()
    val y = Random.nextFloat()
    val vx = (Random.nextFloat() - 0.5f) * 0.03f
    val vy = (Random.nextFloat() - 0.5f) * 0.03f
    val radius = Random.nextFloat() * 1.2f + 0.3f
    val baseAlpha = Random.nextFloat() * 0.15f + 0.05f
    val color = if (Random.nextBoolean()) Color(0xFF00FFFF) else Color(0xFFB4FFB4)
}
