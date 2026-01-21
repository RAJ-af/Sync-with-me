package com.sync.app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sync.app.data.repository.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    fun onLogin(onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            repository.signIn(email, password).onSuccess {
                onSuccess()
            }.onFailure {
                error = it.message
            }
            isLoading = false
        }
    }

    fun onSignUp(onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            repository.signUp(email, password).onSuccess {
                onSuccess()
            }.onFailure {
                error = it.message
            }
            isLoading = false
        }
    }
}

@Composable
fun AuthScreen(viewModel: AuthViewModel, onAuthSuccess: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Sync", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))
        TextField(value = viewModel.email, onValueChange = { viewModel.email = it }, label = { Text("Email") })
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = viewModel.password,
            onValueChange = { viewModel.password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )
        if (viewModel.error != null) {
            Text(viewModel.error!!, color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { viewModel.onLogin(onAuthSuccess) }, enabled = !viewModel.isLoading) {
            Text("Login")
        }
        TextButton(onClick = { viewModel.onSignUp(onAuthSuccess) }, enabled = !viewModel.isLoading) {
            Text("Sign Up")
        }
    }
}
