package com.sync.app.ui.onboarding

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun OnboardingScreen(onSwipeUp: () -> Unit) {
    var offsetY by remember { mutableStateOf(0f) }

    val screenAlpha by animateFloatAsState(
        targetValue = if (offsetY < -400f) 0f else 1f,
        animationSpec = tween(1200),
        label = "screenAlpha",
        finishedListener = { if (it == 0f) onSwipeUp() }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF020202))
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        offsetY = (offsetY + dragAmount).coerceAtMost(0f)
                    },
                    onDragEnd = {
                        if (offsetY >= -400f) {
                            offsetY = 0f
                        }
                    }
                )
            }
            .alpha(screenAlpha)
            .graphicsLayer {
                translationY = offsetY * 1.2f // Overscroll feeling
            }
    ) {
        // Drifting, asymmetric light leak simulation
        LightLeak(Color(0xFF39FF14).copy(alpha = 0.08f), duration = 20000, seed = 1)
        LightLeak(Color(0xFF00FFFF).copy(alpha = 0.05f), duration = 25000, seed = 2)

        // Imperfect, human-like activity traces
        TraceLines()

        // Subtle drifting "shadows"
        ShadowDrift()
    }
}

@Composable
fun LightLeak(color: Color, duration: Int, seed: Int) {
    val transition = rememberInfiniteTransition(label = "leak")
    val random = remember { Random(seed) }

    val xPos by transition.animateFloat(
        initialValue = random.nextFloat(),
        targetValue = random.nextFloat(),
        animationSpec = infiniteRepeatable(tween(duration, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "x"
    )
    val yPos by transition.animateFloat(
        initialValue = random.nextFloat(),
        targetValue = random.nextFloat(),
        animationSpec = infiniteRepeatable(tween((duration * 1.2).toInt(), easing = EaseInOutSine), RepeatMode.Reverse),
        label = "y"
    )

    Canvas(modifier = Modifier.fillMaxSize().blur(100.dp)) {
        drawCircle(
            color = color,
            radius = size.minDimension * 0.8f,
            center = Offset(size.width * xPos, size.height * yPos)
        )
    }
}

@Composable
fun TraceLines() {
    val transition = rememberInfiniteTransition(label = "traces")

    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(15000, easing = LinearEasing), RepeatMode.Restart),
        label = "progress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val stroke = 0.5.dp.toPx()

        // Asymmetric, unplanned line fragments
        drawPath(
            path = Path().apply {
                moveTo(size.width * 0.1f, size.height * 0.8f)
                quadraticTo(
                    size.width * 0.2f, size.height * (0.8f - 0.1f * progress),
                    size.width * 0.3f, size.height * 0.85f
                )
            },
            color = Color.White.copy(alpha = 0.1f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = StrokeCap.Round)
        )

        drawPath(
            path = Path().apply {
                moveTo(size.width * 0.9f, size.height * 0.2f)
                lineTo(size.width * 0.85f, size.height * (0.3f + 0.05f * progress))
            },
            color = Color(0xFF39FF14).copy(alpha = 0.15f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun ShadowDrift() {
    val transition = rememberInfiniteTransition(label = "shadow")
    val driftY by transition.animateFloat(
        initialValue = -100f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(tween(18000, easing = EaseInOutQuad), RepeatMode.Reverse),
        label = "driftY"
    )

    Canvas(modifier = Modifier.fillMaxSize().alpha(0.3f).blur(40.dp)) {
        // Asymmetric "shadow" block
        drawRect(
            color = Color.Black,
            topLeft = Offset(size.width * 0.6f, size.height * 0.4f + driftY.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(size.width * 0.5f, size.height * 0.3f)
        )
    }
}
