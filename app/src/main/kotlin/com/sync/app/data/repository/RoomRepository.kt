package com.sync.app.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sync.app.data.model.MusicState
import com.sync.app.data.model.Room
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RoomRepository(private val db: FirebaseDatabase = FirebaseDatabase.getInstance()) {

    private val roomsRef = db.getReference("rooms")

    suspend fun createRoom(name: String, ownerId: String): String {
        val roomId = roomsRef.push().key ?: throw Exception("Could not generate room ID")
        val room = Room(id = roomId, name = name, ownerId = ownerId)
        roomsRef.child(roomId).setValue(room).await()
        return roomId
    }

    fun observeRoom(roomId: String): Flow<Room?> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(Room::class.java))
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        roomsRef.child(roomId).addValueEventListener(listener)
        awaitClose { roomsRef.child(roomId).removeEventListener(listener) }
    }

    fun observeRooms(): Flow<List<Room>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val rooms = snapshot.children.mapNotNull { it.getValue(Room::class.java) }
                trySend(rooms)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        roomsRef.addValueEventListener(listener)
        awaitClose { roomsRef.removeEventListener(listener) }
    }

    suspend fun updateMusicState(roomId: String, musicState: MusicState) {
        roomsRef.child(roomId).child("musicState").setValue(musicState).await()
    }

    suspend fun joinRoom(roomId: String, userId: String) {
        roomsRef.child(roomId).child("participants").child(userId).setValue(true).await()
    }

    suspend fun leaveRoom(roomId: String, userId: String) {
        roomsRef.child(roomId).child("participants").child(userId).removeValue().await()
    }
}
