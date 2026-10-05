package com.whatsapp.android.ui.screens.call

import android.content.Context
import android.media.AudioManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

    val isSelfCall = callSession.callerUid == callSession.receiverUid
    val isVideo = callSession.type == CallType.VIDEO.name

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WhatsAppDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // VIDEO CALL: Dual Camera / Mirror View
        if (isVideo && isVideoEnabled) {
            // 1. Main Background Viewport (Opponent view - for self call, shows You!)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                val avatar = callSession.receiverAvatar ?: callSession.callerAvatar
                if (!avatar.isNullOrEmpty()) {
                    AsyncImage(
                        model = avatar,
                        contentDescription = "Main View",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(WhatsAppDarkCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = WhatsAppTextMuted,
                            modifier = Modifier.size(120.dp)
                        )
                    }
                }

                // Overlay gradient for readability
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.35f))
                )

                // Top Banner with Caller Info
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 20.dp, top = 20.dp)
                ) {
                    Text(
                        text = if (isSelfCall) "You (Self Video Call)" else callSession.receiverName.ifBlank { callSession.callerName },
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (currentStatus == CallStatus.CONNECTED.name) durationFormatted else "Connecting...",
                        color = WhatsAppLightGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (isSelfCall) {
                        Text(
                            text = "Dual Mirror / Camera Preview Active",
                            color = WhatsAppLightGreen.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }
                }

                // 2. Picture-in-Picture Secondary Window (Self View)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 16.dp, top = 20.dp)
                        .size(width = 110.dp, height = 160.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(WhatsAppDarkCard)
                        .border(2.dp, WhatsAppLightGreen, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!callSession.callerAvatar.isNullOrEmpty()) {
                        AsyncImage(
                            model = callSession.callerAvatar,
                            contentDescription = "Self Pip",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    // Badge on PiP
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isFrontCamera) "Front Cam" else "Back Cam",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        } else {
            // AUDIO CALL: Central Animated Waveform & Caller Details
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pulsating Circle around Avatar
                Box(contentAlignment = Alignment.Center) {
                    if (currentStatus == CallStatus.CONNECTED.name) {
                        Box(
                            modifier = Modifier
                                .size(170.dp * pulseScale)
                                .clip(CircleShape)
                                .background(WhatsAppLightGreen.copy(alpha = 0.15f))
                        )
                        Box(
                            modifier = Modifier
                                .size(150.dp * pulseScale)
                                .clip(CircleShape)
                                .background(WhatsAppLightGreen.copy(alpha = 0.25f))
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .background(WhatsAppDarkCard)
                            .border(3.dp, WhatsAppLightGreen, CircleShape),
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
                                modifier = Modifier.size(70.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = if (isSelfCall) "You (Self Audio Call)" else callSession.receiverName.ifBlank { callSession.callerName },
                    color = WhatsAppTextLight,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (currentStatus == CallStatus.CONNECTED.name) durationFormatted else "Connecting...",
                    color = if (currentStatus == CallStatus.CONNECTED.name) WhatsAppLightGreen else WhatsAppTextMuted,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (isSelfCall) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(WhatsAppDarkCard)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hearing,
                            contentDescription = null,
                            tint = WhatsAppLightGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Dual Voice Audio Active",
                            color = WhatsAppLightGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Text(
                        text = "WhatsApp End-to-End Encrypted",
                        color = WhatsAppTextMuted,
                        fontSize = 12.sp
                    )
                }
            }
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
