package com.sync.app.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sync.app.data.repository.AuthRepository
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

// High-fidelity color palette
val SmokyBlack = Color(0xFF11120D)
val FloralWhite = Color(0xFFFFFBF4)
val WarmOrange = Color(0xFFFFA500)
val SoftYellow = Color(0xFFFFD700)

class AuthViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var step by mutableIntStateOf(0) // 0: Email, 1: Password

    fun onContinue() {
        if (email.isNotBlank() && email.contains("@")) {
            step = 1
            error = null
        } else {
            error = "Please enter a valid email"
        }
    }

    fun onLogin(onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            repository.signIn(email, password).onSuccess {
                onSuccess()
            }.onFailure {
                onSignUp(onSuccess)
            }
            isLoading = false
        }
    }

    private fun onSignUp(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.signUp(email, password).onSuccess {
                onSuccess()
            }.onFailure {
                error = it.message
            }
        }
    }
}

@Composable
fun AuthScreen(viewModel: AuthViewModel, onAuthSuccess: () -> Unit) {
    // Shared beat for rhythmic synchronization
    val infiniteTransition = rememberInfiniteTransition(label = "beat")
    val beatProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beatProgress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SmokyBlack)
    ) {
        AtmosphericBackground(beatProgress)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            MusicReactiveTextSection(beatProgress)

            Spacer(modifier = Modifier.height(80.dp))

            AnimatedContent(
                targetState = viewModel.step,
                transitionSpec = {
                    fadeIn(animationSpec = tween(1200)) togetherWith fadeOut(animationSpec = tween(1200))
                },
                label = "auth_step"
            ) { step ->
                if (step == 0) {
                    MotionInputSection(viewModel, beatProgress)
                } else {
                    PasswordInputSection(viewModel, onAuthSuccess)
                }
            }

            if (viewModel.error != null) {
                Text(
                    text = viewModel.error!!,
                    color = Color.Red.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 24.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            if (viewModel.step == 0) {
                GoogleButton()
            }
        }

        // Cinematic vignette
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        0.0f to Color.Transparent,
                        1.0f to SmokyBlack.copy(alpha = 0.6f),
                        center = Offset.Unspecified
                    )
                )
        )
    }
}

@Composable
fun AtmosphericBackground(beat: Float) {
    val particles = remember { List(30) { RhythmicParticle() } }

    Box(modifier = Modifier.fillMaxSize()) {
        // Rhythmic Floating Dust
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { p ->
                // Motion is rhythmic, responding to the global beat
                val x = (p.baseX + sin(beat * p.speedX) * 0.05f)
                val y = (p.baseY + sin(beat * p.speedY) * 0.05f)
                val alpha = (p.alpha * (0.8f + 0.2f * beat))

                drawCircle(
                    color = Color.White,
                    radius = p.size.dp.toPx(),
                    center = Offset(size.width * x, size.height * y),
                    alpha = alpha
                )
            }
        }

        // Grain Overlay
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.03f)) {
            val random = Random(42)
            for (i in 0..1500) {
                drawCircle(
                    color = Color.White,
                    radius = 0.5.dp.toPx(),
                    center = Offset(random.nextFloat() * size.width, random.nextFloat() * size.height)
                )
            }
        }
    }
}

@Composable
fun MusicReactiveTextSection(beat: Float) {
    val texts = listOf("Listen together", "Talk while the music plays", "Sync the vibe")
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        texts.forEachIndexed { index, text ->
            val stagger = index * 0.2f
            val breathingBeat = (beat + stagger) % 1f

            Text(
                text = text,
                color = FloralWhite,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.ExtraLight,
                    letterSpacing = 2.5.sp,
                    fontSize = 19.sp
                ),
                modifier = Modifier
                    .graphicsLayer {
                        val scale = 1f + sin(breathingBeat * Math.PI.toFloat()) * 0.02f
                        scaleX = scale
                        scaleY = scale
                    }
                    .alpha(0.4f + sin(breathingBeat * Math.PI.toFloat()) * 0.4f)
                    .padding(vertical = 6.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MotionInputSection(viewModel: AuthViewModel, beat: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.height(60.dp)) {
            // Secret input field
            BasicTextField(
                value = viewModel.email,
                onValueChange = { viewModel.email = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(color = Color.Transparent),
                cursorBrush = SolidColor(Color.Transparent),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            // Dynamic Dot Input (Moves, Connects, Merges)
            DynamicDotInteraction(viewModel.email, beat)
        }

        Spacer(modifier = Modifier.height(56.dp))

        ActionPill(onClick = { viewModel.onContinue() })
    }
}

@Composable
fun DynamicDotInteraction(input: String, beat: Float) {
    Canvas(modifier = Modifier.fillMaxWidth().height(40.dp)) {
        val dotRadius = 4.dp.toPx()
        val spacing = 30.dp.toPx()
        val maxDots = 10
        val centerPoint = Offset(size.width / 2, size.height / 2)

        // Typing intensity affects the "vibration" of the dots
        val typingActivity = (input.length.toFloat() / 20f).coerceIn(0f, 1f)

        for (i in 0 until maxDots) {
            val isActive = i < input.length
            val isCursor = i == input.length

            // Base position
            val startX = (size.width - (maxDots - 1) * spacing) / 2
            val baseX = startX + i * spacing

            // Motion logic: move and connect based on typing and beat
            val offsetY = sin(beat * Math.PI.toFloat() * 2 + i) * 5f
            val offsetX = if (isActive) sin(beat * 5f + i) * typingActivity * 10f else 0f

            val dotPos = Offset(baseX + offsetX, centerPoint.y + offsetY)

            val color = if (isActive) WarmOrange else if (isCursor) SoftYellow else Color.White.copy(alpha = 0.15f)
            val alpha = if (isCursor) 0.5f + 0.5f * sin(beat * Math.PI.toFloat() * 2) else if (isActive) 1f else 0.2f

            // Draw connection line to previous dot if active (Merging effect)
            if (i > 0 && i <= input.length) {
                val prevX = startX + (i - 1) * spacing
                val prevPos = Offset(prevX, centerPoint.y + sin(beat * Math.PI.toFloat() * 2 + (i - 1)) * 5f)
                drawLine(
                    color = WarmOrange.copy(alpha = 0.3f * alpha),
                    start = prevPos,
                    end = dotPos,
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Outer glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = 0.2f * alpha), Color.Transparent),
                    center = dotPos,
                    radius = dotRadius * 4
                ),
                radius = dotRadius * 4,
                center = dotPos
            )

            // Core dot
            drawCircle(
                color = color,
                radius = if (isActive) dotRadius * 1.2f else dotRadius,
                center = dotPos,
                alpha = alpha
            )
        }
    }
}

@Composable
fun ActionPill(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "energy")
    val energyFlow by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flow"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(29.dp))
            .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(29.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Sound Energy Flow (Orange Wave)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val waveWidth = size.width * 0.4f
            val startX = (size.width + waveWidth) * energyFlow - waveWidth
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, WarmOrange.copy(alpha = 0.12f), Color.Transparent),
                    startX = startX,
                    endX = startX + waveWidth
                ),
                size = size
            )
        }

        Text(
            text = "Continue →",
            color = Color.White,
            fontWeight = FontWeight.ExtraLight,
            fontSize = 17.sp,
            letterSpacing = 1.5.sp
        )
    }
}

@Composable
fun GoogleButton() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable { /* No action */ },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Continue with Google",
            color = Color.White.copy(alpha = 0.4f),
            fontWeight = FontWeight.ExtraLight,
            fontSize = 14.sp,
            letterSpacing = 1.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordInputSection(viewModel: AuthViewModel, onAuthSuccess: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TextField(
            value = viewModel.password,
            onValueChange = { viewModel.password = it },
            placeholder = { Text("Enter password", color = Color.Gray.copy(alpha = 0.3f)) },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(29.dp))
                .background(SmokyBlack),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.White.copy(alpha = 0.1f),
                unfocusedIndicatorColor = Color.White.copy(alpha = 0.05f),
                cursorColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            )
        )

        Spacer(modifier = Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .clip(RoundedCornerShape(29.dp))
                .background(Color.White)
                .clickable(enabled = !viewModel.isLoading) { viewModel.onLogin(onAuthSuccess) },
            contentAlignment = Alignment.Center
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = SmokyBlack, strokeWidth = 1.5.dp)
            } else {
                Text("Start Syncing", color = SmokyBlack, fontWeight = FontWeight.Light, letterSpacing = 1.sp)
            }
        }

        TextButton(onClick = { viewModel.step = 0 }, modifier = Modifier.padding(top = 20.dp)) {
            Text("Back", color = Color.Gray, fontSize = 13.sp)
        }
    }
}

private class RhythmicParticle {
    val baseX = Random.nextFloat()
    val baseY = Random.nextFloat()
    val speedX = Random.nextFloat() * 2f + 1f
    val speedY = Random.nextFloat() * 2f + 1f
    val size = Random.nextFloat() * 1.2f + 0.4f
    val alpha = Random.nextFloat() * 0.1f + 0.05f
}
