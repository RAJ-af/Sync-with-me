package com.sync.app.ui.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

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
                // If login fails, try signing up (the "intuitive" approach)
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
            .background(Color(0xFF121212)) // Soft dark gray
    ) {
        GrainBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            FloatingTextSection()

            Spacer(modifier = Modifier.height(48.dp))

            AnimatedContent(
                targetState = viewModel.step,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                    }
                },
                label = "auth_step"
            ) { step ->
                if (step == 0) {
                    EmailInputSection(viewModel)
                } else {
                    PasswordInputSection(viewModel, onAuthSuccess)
                }
            }

            if (viewModel.error != null) {
                Text(
                    text = viewModel.error!!,
                    color = Color(0xFFFF5252),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (viewModel.step == 0) {
                GoogleButton()
            }
        }
    }
}

@Composable
fun GrainBackground() {
    Canvas(modifier = Modifier.fillMaxSize().alpha(0.05f)) {
        val random = Random(42)
        for (i in 0..1500) {
            drawCircle(
                color = Color.White,
                radius = 0.8.dp.toPx(),
                center = androidx.compose.ui.geometry.Offset(
                    random.nextFloat() * size.width,
                    random.nextFloat() * size.height
                )
            )
        }
    }
}

@Composable
fun FloatingTextSection() {
    val texts = listOf("Listen together", "Talk while the music plays", "Sync the vibe")
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        texts.forEachIndexed { index, text ->
            AnimatedFloatingText(text, index * 800)
        }
    }
}

@Composable
fun AnimatedFloatingText(text: String, delayMillis: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    val yOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, delayMillis = delayMillis, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "yOffset"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, delayMillis = delayMillis, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Text(
        text = text,
        color = Color.White,
        style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Light,
            letterSpacing = 1.2.sp,
            fontSize = 22.sp
        ),
        modifier = Modifier
            .graphicsLayer { translationY = yOffset }
            .alpha(alpha)
            .padding(vertical = 6.dp),
        textAlign = TextAlign.Center
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailInputSection(viewModel: AuthViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TextField(
            value = viewModel.email,
            onValueChange = { viewModel.email = it },
            placeholder = { Text("Enter your email", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF1E1E1E)),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { viewModel.onContinue() },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
        ) {
            Text("Continue →", fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordInputSection(viewModel: AuthViewModel, onAuthSuccess: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        TextField(
            value = viewModel.password,
            onValueChange = { viewModel.password = it },
            placeholder = { Text("Enter password", color = Color.Gray) },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF1E1E1E)),
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
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

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { viewModel.onLogin(onAuthSuccess) },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
            enabled = !viewModel.isLoading
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.Black,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Start Syncing", fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
            }
        }

        TextButton(onClick = { viewModel.step = 0 }, modifier = Modifier.padding(top = 8.dp)) {
            Text("Use a different email", color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
fun GoogleButton() {
    OutlinedButton(
        onClick = { /* Placeholder for Google Login */ },
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(50),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF333333)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Simple drawn Google 'G' icon placeholder
            Canvas(modifier = Modifier.size(20.dp)) {
                drawCircle(color = Color.White, radius = size.minDimension / 2, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()))
                // Drawing a simple 'G' shape
                drawArc(
                    color = Color.White,
                    startAngle = 45f,
                    sweepAngle = 270f,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text("Continue with Google", fontWeight = FontWeight.Normal, fontSize = 16.sp)
        }
    }
}
