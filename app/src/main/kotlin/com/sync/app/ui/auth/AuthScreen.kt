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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
import kotlin.random.Random

// Colors from the design prompt
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SmokyBlack)
    ) {
        AtmosphericBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            BreathingTextSection()

            Spacer(modifier = Modifier.height(60.dp))

            AnimatedContent(
                targetState = viewModel.step,
                transitionSpec = {
                    fadeIn(animationSpec = tween(800)) togetherWith fadeOut(animationSpec = tween(800))
                },
                label = "auth_step"
            ) { step ->
                if (step == 0) {
                    EmailDotInputSection(viewModel)
                } else {
                    PasswordInputSection(viewModel, onAuthSuccess)
                }
            }

            if (viewModel.error != null) {
                Text(
                    text = viewModel.error!!,
                    color = Color.Red.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 16.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (viewModel.step == 0) {
                GoogleButton()
            }
        }
    }
}

@Composable
fun AtmosphericBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "dust")
    val particles = remember { List(25) { DustParticle() } }

    Box(modifier = Modifier.fillMaxSize()) {
        // Grain Layer
        Canvas(modifier = Modifier.fillMaxSize().alpha(0.04f)) {
            val random = Random(1337)
            for (i in 0..2000) {
                drawCircle(
                    color = Color.White,
                    radius = 0.5.dp.toPx(),
                    center = Offset(random.nextFloat() * size.width, random.nextFloat() * size.height)
                )
            }
        }

        // Floating Dust
        particles.forEach { particle ->
            val xOffset by infiniteTransition.animateFloat(
                initialValue = particle.startX,
                targetValue = particle.endX,
                animationSpec = infiniteRepeatable(
                    animation = tween(particle.duration, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dustX"
            )
            val yOffset by infiniteTransition.animateFloat(
                initialValue = particle.startY,
                targetValue = particle.endY,
                animationSpec = infiniteRepeatable(
                    animation = tween(particle.duration, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dustY"
            )

            Canvas(modifier = Modifier.fillMaxSize().alpha(particle.alpha)) {
                drawCircle(
                    color = Color.White,
                    radius = particle.size.dp.toPx(),
                    center = Offset(size.width * xOffset, size.height * yOffset)
                )
            }
        }
    }
}

@Composable
fun BreathingTextSection() {
    val texts = listOf("Listen together", "Talk while the music plays", "Sync the vibe")
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        texts.forEachIndexed { index, text ->
            BreathingText(text, index * 1200)
        }
    }
}

@Composable
fun BreathingText(text: String, delayMillis: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, delayMillis = delayMillis, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, delayMillis = delayMillis, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Text(
        text = text,
        color = FloralWhite,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.ExtraLight,
            letterSpacing = 2.sp,
            fontSize = 18.sp
        ),
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .alpha(alpha)
            .padding(vertical = 4.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun EmailDotInputSection(viewModel: AuthViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            // Invisible text field to capture input
            BasicTextField(
                value = viewModel.email,
                onValueChange = { viewModel.email = it },
                modifier = Modifier.fillMaxWidth().height(40.dp),
                textStyle = TextStyle(color = Color.Transparent),
                cursorBrush = SolidColor(Color.Transparent),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            // Glowing Dots Representation
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val dotCount = 8
                repeat(dotCount) { index ->
                    GlowingDot(
                        isActive = index < viewModel.email.length || (index == 0 && viewModel.email.isEmpty()),
                        isBlinking = index == viewModel.email.length % dotCount
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        ContinueButton(onClick = { viewModel.onContinue() })
    }
}

@Composable
fun GlowingDot(isActive: Boolean, isBlinking: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "dot")
    val blinkAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blink"
    )

    val color = if (isActive) WarmOrange else SoftYellow.copy(alpha = 0.3f)
    val alpha = if (isBlinking) blinkAlpha else if (isActive) 0.8f else 0.2f

    Canvas(modifier = Modifier.size(8.dp)) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color, Color.Transparent),
                center = center,
                radius = size.width * 1.5f
            ),
            radius = size.width * 1.5f,
            alpha = alpha * 0.4f
        )
        drawCircle(
            color = color,
            radius = size.width / 2,
            alpha = alpha
        )
    }
}

@Composable
fun ContinueButton(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(28.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // Animated Orange Wave Glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            val waveWidth = size.width * 0.5f
            val startX = (size.width + waveWidth) * waveOffset - waveWidth
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.Transparent, WarmOrange.copy(alpha = 0.15f), Color.Transparent),
                    startX = startX,
                    endX = startX + waveWidth
                ),
                size = size
            )
        }

        Text(
            text = "Continue →",
            color = Color.White,
            fontWeight = FontWeight.Light,
            fontSize = 16.sp,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun GoogleButton() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .clickable { /* Placeholder */ },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Continue with Google",
            color = Color.White.copy(alpha = 0.6f),
            fontWeight = FontWeight.ExtraLight,
            fontSize = 14.sp
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
            placeholder = { Text("Enter password", color = Color.Gray.copy(alpha = 0.5f)) },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(SmokyBlack),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.White.copy(alpha = 0.2f),
                unfocusedIndicatorColor = Color.White.copy(alpha = 0.1f),
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

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .clickable(enabled = !viewModel.isLoading) { viewModel.onLogin(onAuthSuccess) },
            contentAlignment = Alignment.Center
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = SmokyBlack, strokeWidth = 2.dp)
            } else {
                Text("Start Syncing", color = SmokyBlack, fontWeight = FontWeight.Normal)
            }
        }

        TextButton(onClick = { viewModel.step = 0 }, modifier = Modifier.padding(top = 16.dp)) {
            Text("Back", color = Color.Gray, fontSize = 14.sp)
        }
    }
}

// Helper class for dust particles
private class DustParticle {
    val startX = Random.nextFloat()
    val startY = Random.nextFloat()
    val endX = Random.nextFloat()
    val endY = Random.nextFloat()
    val size = Random.nextFloat() * 1.5f + 0.5f
    val alpha = Random.nextFloat() * 0.15f + 0.05f
    val duration = Random.nextInt(15000, 30000)
}
