package com.whatsapp.android.ui.screens.call

import android.content.Context
import android.media.AudioManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.whatsapp.android.data.models.CallSession
import com.whatsapp.android.data.models.CallStatus
import com.whatsapp.android.data.models.CallType
import com.whatsapp.android.data.repository.CallRepository
import com.whatsapp.android.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ActiveCallScreen(
    callSession: CallSession,
    callRepo: CallRepository,
    onCallEnded: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    var isMuted by remember { mutableStateOf(false) }
    var isSpeakerOn by remember { mutableStateOf(callSession.type == CallType.VIDEO.name) }
    var isVideoEnabled by remember { mutableStateOf(callSession.type == CallType.VIDEO.name) }
    var isFrontCamera by remember { mutableStateOf(true) }
    var durationSeconds by remember { mutableIntStateOf(0) }
    var currentStatus by remember { mutableStateOf(callSession.status) }

    // Listen to call status from Firebase
    LaunchedEffect(callSession.callId) {
        callRepo.observeCall(callSession.callId).collect { session ->
            if (session != null) {
                currentStatus = session.status
                if (session.status == CallStatus.ENDED.name || session.status == CallStatus.REJECTED.name) {
                    onCallEnded()
                }
            }
        }
    }

    // Call duration timer
    LaunchedEffect(currentStatus) {
        if (currentStatus == CallStatus.CONNECTED.name) {
            while (true) {
                delay(1000)
                durationSeconds++
            }
        }
    }

    // Speaker control
    LaunchedEffect(isSpeakerOn) {
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isSpeakerphoneOn = isSpeakerOn
    }

    // Microphone mute control
    LaunchedEffect(isMuted) {
        audioManager.isMicrophoneMute = isMuted
    }

    val minutes = durationSeconds / 60
    val seconds = durationSeconds % 60
    val durationFormatted = String.format("%02d:%02d", minutes, seconds)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WhatsAppDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        // Center: Peer Avatar and Details
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(WhatsAppDarkCard),
                contentAlignment = Alignment.Center
            ) {
                val avatar = callSession.receiverAvatar ?: callSession.callerAvatar
                if (!avatar.isNullOrEmpty()) {
                    AsyncImage(
                        model = avatar,
                        contentDescription = "Caller",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = WhatsAppTextMuted,
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = callSession.receiverName.ifBlank { callSession.callerName },
                color = WhatsAppTextLight,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (currentStatus == CallStatus.CONNECTED.name) durationFormatted else "Ringing...",
                color = if (currentStatus == CallStatus.CONNECTED.name) WhatsAppLightGreen else WhatsAppTextMuted,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "WhatsApp ${if (callSession.type == CallType.VIDEO.name) "Video" else "Audio"} Call (End-to-End Encrypted)",
                color = WhatsAppTextMuted,
                fontSize = 12.sp
            )
        }

        // Bottom Controls Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Mute Toggle
                IconButton(
                    onClick = { isMuted = !isMuted },
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(if (isMuted) Color.White else WhatsAppDarkCard)
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mute",
                        tint = if (isMuted) Color.Black else Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // 2. Speaker Toggle (Earpiece vs Loudspeaker)
                IconButton(
                    onClick = { isSpeakerOn = !isSpeakerOn },
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(if (isSpeakerOn) WhatsAppLightGreen else WhatsAppDarkCard)
                ) {
                    Icon(
                        imageVector = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                        contentDescription = "Speaker",
                        tint = if (isSpeakerOn) Color.Black else Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                // 3. Video Toggle
                if (callSession.type == CallType.VIDEO.name) {
                    IconButton(
                        onClick = { isVideoEnabled = !isVideoEnabled },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(if (!isVideoEnabled) Color.White else WhatsAppDarkCard)
                    ) {
                        Icon(
                            imageVector = if (isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                            contentDescription = "Video",
                            tint = if (!isVideoEnabled) Color.Black else Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // 4. Flip Camera
                    IconButton(
                        onClick = { isFrontCamera = !isFrontCamera },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(WhatsAppDarkCard)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Switch Camera",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // 5. End Call Button
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            callRepo.endCall(callSession.callId, durationSeconds)
                            onCallEnded()
                        }
                    },
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(WhatsAppRed)
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
