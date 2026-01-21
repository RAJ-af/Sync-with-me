package com.sync.app.data.model

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = ""
)

data class Room(
    val id: String = "",
    val name: String = "",
    val ownerId: String = "",
    val hostId: String = "", // Can be different from owner if needed
    val musicState: MusicState = MusicState(),
    val participants: Map<String, Boolean> = emptyMap()
)

data class MusicState(
    val currentTrackUrl: String = "",
    val isPlaying: Boolean = false,
    val currentTimestamp: Long = 0,
    val lastUpdated: Long = 0
)
