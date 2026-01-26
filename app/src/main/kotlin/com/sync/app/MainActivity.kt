/*
 * Copyright (c) 2024 Sync App
 */
package com.sync.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import com.sync.app.ui.theme.ComposeEmptyActivityTheme
import com.sync.app.ui.onboarding.OnboardingScreen
import com.sync.app.ui.auth.AuthScreen
import com.sync.app.ui.auth.AuthViewModel
import com.sync.app.ui.home.HomeScreen
import com.sync.app.ui.home.HomeViewModel
import com.sync.app.ui.room.RoomScreen
import com.sync.app.ui.room.RoomViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.viewmodel.compose.viewModel

sealed class Screen {
    object Onboarding : Screen()
    object Auth : Screen()
    object Home : Screen()
    data class Room(val roomId: String) : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeEmptyActivityTheme {
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    // Handle permission result if needed
                }

                LaunchedEffect(Unit) {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }

                var currentScreen by remember {
                    mutableStateOf<Screen>(
                        if (FirebaseAuth.getInstance().currentUser == null) Screen.Onboarding else Screen.Home
                    )
                }

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    when (val screen = currentScreen) {
                        is Screen.Onboarding -> OnboardingScreen {
                            currentScreen = Screen.Auth
                        }
                        is Screen.Auth -> AuthScreen(viewModel()) {
                            currentScreen = Screen.Home
                        }
                        is Screen.Home -> HomeScreen(viewModel()) { roomId ->
                            currentScreen = Screen.Room(roomId)
                        }
                        is Screen.Room -> {
                            val roomViewModel: RoomViewModel = viewModel(factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                                    return RoomViewModel(application, screen.roomId) as T
                                }
                            })
                            RoomScreen(roomViewModel)
                        }
                    }
                }
            }
        }
    }
}
