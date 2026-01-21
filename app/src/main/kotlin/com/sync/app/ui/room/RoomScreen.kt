package com.sync.app.ui.room

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RoomScreen(viewModel: RoomViewModel) {
    val room = viewModel.room ?: return

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(room.name, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.isOwner) {
            TextField(
                value = viewModel.trackUrlInput,
                onValueChange = { viewModel.trackUrlInput = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Music URL") }
            )
            Row {
                Button(onClick = { viewModel.onPlayTrack() }) { Text("Play") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { viewModel.togglePlayPause() }) {
                    Text(if (room.musicState.isPlaying) "Pause" else "Resume")
                }
            }
        } else {
            Text("Currently Playing: ${room.musicState.currentTrackUrl}")
            Text(if (room.musicState.isPlaying) "Status: Playing" else "Status: Paused")
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text("Participants:", style = MaterialTheme.typography.titleMedium)
        room.participants.keys.forEach { userId ->
            Text("- $userId")
        }
    }
}
