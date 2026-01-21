package com.sync.app.ui.room

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sync.app.data.model.MusicState
import com.sync.app.data.model.Room
import com.sync.app.data.repository.AuthRepository
import com.sync.app.data.repository.RoomRepository
import com.sync.app.music.MusicPlayerManager
import com.sync.app.voice.SignalingClient
import com.sync.app.voice.WebRtcClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.webrtc.IceCandidate
import org.webrtc.MediaStream
import org.webrtc.SessionDescription

class RoomViewModel(application: Application, private val roomId: String) : AndroidViewModel(application), WebRtcClient.Observer, SignalingClient.Listener {

    private val authRepo = AuthRepository()
    private val roomRepo = RoomRepository()
    private val musicManager = MusicPlayerManager(application)
    private val webRtcClient = WebRtcClient(application, this)
    private val signalingClient = SignalingClient(com.sync.app.Config.SIGNALING_SERVER_URL, this)

    var room by mutableStateOf<Room?>(null)
    var isOwner by mutableStateOf(false)
    var trackUrlInput by mutableStateOf("")

    init {
        viewModelScope.launch {
            val userId = authRepo.currentUser.value?.uid
            if (userId != null) {
                roomRepo.joinRoom(roomId, userId)
            }
            roomRepo.observeRoom(roomId).collect { updatedRoom ->
                room = updatedRoom
                isOwner = updatedRoom?.ownerId == authRepo.currentUser.value?.uid
                if (!isOwner && updatedRoom != null) {
                    musicManager.syncWithState(updatedRoom.musicState)
                }
            }
        }
        signalingClient.connect()
    }

    fun onPlayTrack() {
        if (isOwner) {
            musicManager.playTrack(trackUrlInput)
            updateRoomMusicState()
        }
    }

    fun togglePlayPause() {
        if (isOwner) {
            musicManager.togglePlayPause()
            updateRoomMusicState()
        }
    }

    private fun updateRoomMusicState() {
        viewModelScope.launch {
            val state = musicManager.currentMusicState.value
            roomRepo.updateMusicState(roomId, state)
        }
    }

    // Signaling Listener
    override fun onConnected() {
        signalingClient.joinRoom(roomId)
    }

    override fun onUserJoined(userId: String) {
        if (isOwner) {
            webRtcClient.startCall(userId)
        }
    }

    override fun onOfferReceived(from: String, sdp: SessionDescription) {
        webRtcClient.handleOffer(from, sdp)
    }

    override fun onAnswerReceived(from: String, sdp: SessionDescription) {
        webRtcClient.handleAnswer(from, sdp)
    }

    override fun onIceCandidateReceived(from: String, candidate: IceCandidate) {
        webRtcClient.handleIceCandidate(from, candidate)
    }

    // WebRTC Observer
    override fun onIceCandidate(candidate: IceCandidate, toUser: String) {
        signalingClient.sendIceCandidate(toUser, candidate)
    }

    override fun onSendOffer(sdp: SessionDescription, toUser: String) {
        signalingClient.sendOffer(toUser, sdp)
    }

    override fun onSendAnswer(sdp: SessionDescription, toUser: String) {
        signalingClient.sendAnswer(toUser, sdp)
    }

    override fun onAddStream(stream: MediaStream, fromUser: String) {
        // In a real app, you'd attach this stream to an AudioTrack/View
        // For voice only, it should play automatically if using JavaAudioDeviceModule
    }

    override fun onCleared() {
        super.onCleared()
        val userId = authRepo.currentUser.value?.uid
        if (userId != null) {
            viewModelScope.launch {
                roomRepo.leaveRoom(roomId, userId)
            }
        }
        musicManager.release()
        signalingClient.disconnect()
    }
}
