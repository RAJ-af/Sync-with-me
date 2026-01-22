package com.sync.app.voice

import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

class SignalingClient(
    private val serverUrl: String,
    private val listener: Listener
) {
    private var socket: Socket? = null

    fun connect() {
        try {
            val opts = IO.Options()
            socket = IO.socket(serverUrl, opts)

            socket?.on(Socket.EVENT_CONNECT, object : Emitter.Listener {
                override fun call(vararg args: Any?) {
                    listener.onConnected()
                }
            })

            socket?.on("user-joined", object : Emitter.Listener {
                override fun call(vararg args: Any?) {
                    val userId = args[0] as String
                    listener.onUserJoined(userId)
                }
            })

            socket?.on("offer", object : Emitter.Listener {
                override fun call(vararg args: Any?) {
                    val data = args[0] as JSONObject
                    val from = data.getString("from")
                    val sdp = data.getString("offer")
                    listener.onOfferReceived(from, SessionDescription(SessionDescription.Type.OFFER, sdp))
                }
            })

            socket?.on("answer", object : Emitter.Listener {
                override fun call(vararg args: Any?) {
                    val data = args[0] as JSONObject
                    val from = data.getString("from")
                    val sdp = data.getString("answer")
                    listener.onAnswerReceived(from, SessionDescription(SessionDescription.Type.ANSWER, sdp))
                }
            })

            socket?.on("ice-candidate", object : Emitter.Listener {
                override fun call(vararg args: Any?) {
                    val data = args[0] as JSONObject
                    val from = data.getString("from")
                    val candidateObj = data.getJSONObject("candidate")
                    val candidate = IceCandidate(
                        candidateObj.getString("sdpMid"),
                        candidateObj.getInt("sdpMLineIndex"),
                        candidateObj.getString("sdp")
                    )
                    listener.onIceCandidateReceived(from, candidate)
                }
            })

            socket?.connect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun joinRoom(roomId: String) {
        socket?.emit("join-room", roomId)
    }

    fun sendOffer(to: String, sdp: SessionDescription) {
        val data = JSONObject()
        data.put("to", to)
        data.put("offer", sdp.description)
        socket?.emit("offer", data)
    }

    fun sendAnswer(to: String, sdp: SessionDescription) {
        val data = JSONObject()
        data.put("to", to)
        data.put("answer", sdp.description)
        socket?.emit("answer", data)
    }

    fun sendIceCandidate(to: String, candidate: IceCandidate) {
        val data = JSONObject()
        data.put("to", to)
        val candidateObj = JSONObject()
        candidateObj.put("sdpMid", candidate.sdpMid)
        candidateObj.put("sdpMLineIndex", candidate.sdpMLineIndex)
        candidateObj.put("sdp", candidate.sdp)
        data.put("candidate", candidateObj)
        socket?.emit("ice-candidate", data)
    }

    fun disconnect() {
        socket?.disconnect()
    }

    interface Listener {
        fun onConnected()
        fun onUserJoined(userId: String)
        fun onOfferReceived(from: String, sdp: SessionDescription)
        fun onAnswerReceived(from: String, sdp: SessionDescription)
        fun onIceCandidateReceived(from: String, candidate: IceCandidate)
    }
}
