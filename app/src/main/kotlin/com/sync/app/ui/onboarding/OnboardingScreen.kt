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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun OnboardingScreen(onSwipeUp: () -> Unit) {
    var offsetY by remember { mutableStateOf(0f) }

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
    ) {
        // Animated Center Waveform
        WaveformAnimation(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.Center)
                .graphicsLayer {
                    scaleX = expandedScale
                    scaleY = expandedScale
                }
        )

        // Bottom Swipe Bar
        SwipeUpBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
                .offset { IntOffset(0, offsetY.roundToInt()) }
        )
    }
}

@Composable
fun WaveformAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val barsCount = 30
    val animValues = List(barsCount) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 600 + (index * 40) % 600,
                    easing = FastOutSlowInEasing
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
                val barHeight = size.height * animValues[i].value
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
