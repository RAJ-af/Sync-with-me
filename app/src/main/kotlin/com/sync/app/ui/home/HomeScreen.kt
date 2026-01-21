package com.sync.app.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sync.app.data.model.Room
import com.sync.app.data.repository.AuthRepository
import com.sync.app.data.repository.RoomRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val roomRepo: RoomRepository = RoomRepository(),
    private val authRepo: AuthRepository = AuthRepository()
) : ViewModel() {
    val rooms = roomRepo.observeRooms().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    var newRoomName by mutableStateOf("")

    fun createRoom(onCreated: (String) -> Unit) {
        val userId = authRepo.currentUser.value?.uid ?: return
        viewModelScope.launch {
            val roomId = roomRepo.createRoom(newRoomName, userId)
            onCreated(roomId)
        }
    }
}

@Composable
fun HomeScreen(viewModel: HomeViewModel, onJoinRoom: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row {
            TextField(
                value = viewModel.newRoomName,
                onValueChange = { viewModel.newRoomName = it },
                modifier = Modifier.weight(1f),
                label = { Text("New Room Name") }
            )
            Button(onClick = { viewModel.createRoom(onJoinRoom) }) {
                Text("Create")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn {
            items(viewModel.rooms.value) { room ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onJoinRoom(room.id) }
                ) {
                    Text(room.name, modifier = Modifier.padding(16.dp))
                }
            }
        }
    }
}
