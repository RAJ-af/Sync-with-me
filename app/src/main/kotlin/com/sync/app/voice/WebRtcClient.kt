package com.sync.app.voice

import android.content.Context
import org.webrtc.*
import java.util.*

class WebRtcClient(
    private val context: Context,
    private val observer: Observer
) {
    private val rootEglBase: EglBase = EglBase.create()
    private val peerConnectionFactory: PeerConnectionFactory

    init {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)

        peerConnectionFactory = PeerConnectionFactory.builder()
            .createPeerConnectionFactory()
    }

    private val iceServers = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
    )

    private val peerConnections = mutableMapOf<String, PeerConnection>()
    private val localStream: MediaStream by lazy { createLocalStream() }

    fun createPeerConnection(userId: String): PeerConnection? {
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        val pc = peerConnectionFactory.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate) {
                observer.onIceCandidate(candidate, userId)
            }
            override fun onAddStream(stream: MediaStream) {
                observer.onAddStream(stream, userId)
            }
            // Other callbacks...
            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {}
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
            override fun onRemoveStream(p0: MediaStream?) {}
            override fun onDataChannel(p0: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
        })
        if (pc != null) {
            pc.addStream(localStream)
            peerConnections[userId] = pc
        }
        return pc
    }

    fun startCall(userId: String) {
        val pc = peerConnections[userId] ?: createPeerConnection(userId)
        val constraints = MediaConstraints()
        pc?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription) {
                pc.setLocalDescription(this, sdp)
                observer.onSendOffer(sdp, userId)
            }
            override fun onSetSuccess() {}
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(p0: String?) {}
        }, constraints)
    }

    fun handleOffer(userId: String, sdp: SessionDescription) {
        val pc = peerConnections[userId] ?: createPeerConnection(userId)
        pc?.setRemoteDescription(object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onSetSuccess() {
                val constraints = MediaConstraints()
                pc.createAnswer(object : SdpObserver {
                    override fun onCreateSuccess(answerSdp: SessionDescription) {
                        pc.setLocalDescription(this, answerSdp)
                        observer.onSendAnswer(answerSdp, userId)
                    }
                    override fun onSetSuccess() {}
                    override fun onCreateFailure(p0: String?) {}
                    override fun onSetFailure(p0: String?) {}
                }, constraints)
            }
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(p0: String?) {}
        }, sdp)
    }

    fun handleAnswer(userId: String, sdp: SessionDescription) {
        peerConnections[userId]?.setRemoteDescription(SimpleSdpObserver(), sdp)
    }

    fun handleIceCandidate(userId: String, candidate: IceCandidate) {
        peerConnections[userId]?.addIceCandidate(candidate)
    }

    fun createLocalStream(): MediaStream {
        val audioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
        val audioTrack = peerConnectionFactory.createAudioTrack("ARDAMSa0", audioSource)
        val stream = peerConnectionFactory.createLocalMediaStream("ARDAMS")
        stream.addTrack(audioTrack)
        return stream
    }

    interface Observer {
        fun onIceCandidate(candidate: IceCandidate, toUser: String)
        fun onSendOffer(sdp: SessionDescription, toUser: String)
        fun onSendAnswer(sdp: SessionDescription, toUser: String)
        fun onAddStream(stream: MediaStream, fromUser: String)
    }

    private class SimpleSdpObserver : SdpObserver {
        override fun onCreateSuccess(p0: SessionDescription?) {}
        override fun onSetSuccess() {}
        override fun onCreateFailure(p0: String?) {}
        override fun onSetFailure(p0: String?) {}
    }
}
