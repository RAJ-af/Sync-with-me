package com.sync.app.music

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.sync.app.data.model.MusicState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MusicPlayerManager(context: Context) {

    private val exoPlayer = ExoPlayer.Builder(context).build()

    private val _currentMusicState = MutableStateFlow(MusicState())
    val currentMusicState = _currentMusicState.asStateFlow()

    private var isInternalUpdate = false

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (!isInternalUpdate) {
                    _currentMusicState.value = _currentMusicState.value.copy(
                        isPlaying = isPlaying,
                        currentTimestamp = exoPlayer.currentPosition,
                        lastUpdated = System.currentTimeMillis()
                    )
                }
            }
        })
    }

    fun syncWithState(newState: MusicState) {
        if (newState == _currentMusicState.value) return

        isInternalUpdate = true

        // Update URL if changed
        if (newState.currentTrackUrl != _currentMusicState.value.currentTrackUrl) {
            exoPlayer.setMediaItem(MediaItem.fromUri(newState.currentTrackUrl))
            exoPlayer.prepare()
        }

        // Sync playback state
        if (newState.isPlaying) {
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }

        // Sync timestamp if drift is large (> 2 seconds)
        val drift = Math.abs(exoPlayer.currentPosition - newState.currentTimestamp)
        if (drift > 2000) {
            exoPlayer.seekTo(newState.currentTimestamp)
        }

        _currentMusicState.value = newState
        isInternalUpdate = false
    }

    fun playTrack(url: String) {
        isInternalUpdate = true
        exoPlayer.setMediaItem(MediaItem.fromUri(url))
        exoPlayer.prepare()
        exoPlayer.play()
        _currentMusicState.value = MusicState(
            currentTrackUrl = url,
            isPlaying = true,
            currentTimestamp = 0,
            lastUpdated = System.currentTimeMillis()
        )
        isInternalUpdate = false
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun release() {
        exoPlayer.release()
    }

    fun getCurrentPosition(): Long = exoPlayer.currentPosition
}
