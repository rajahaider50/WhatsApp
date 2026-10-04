package com.whatsapp.android.ui.screens.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.whatsapp.android.config.CloudinaryConfig
import com.whatsapp.android.data.models.CallType
import com.whatsapp.android.data.models.ChatMessage
import com.whatsapp.android.data.models.MessageType
import com.whatsapp.android.data.models.User
import com.whatsapp.android.data.repository.RealtimeRepository
import com.whatsapp.android.data.service.AudioRecordingManager
import com.whatsapp.android.ui.components.AudioTrimmerSheet
import com.whatsapp.android.ui.components.VoiceRecordingStudioModal
import com.whatsapp.android.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun ChatScreen(
    chatId: String,
    currentUser: User,
    peerUser: User,
    realtimeRepo: RealtimeRepository,
    onBack: () -> Unit,
    onPeerProfileClick: () -> Unit,
    onStartCall: (User, CallType) -> Unit,
    onPhotoClick: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var isViewOnceNext by remember { mutableStateOf(false) }

    // Audio recording & trimming manager
    val audioManager = remember { AudioRecordingManager(context) }
    val isRecording by audioManager.isRecording.collectAsState()
    val recordingDuration by audioManager.recordingDurationSeconds.collectAsState()
    val amplitudes by audioManager.amplitudes.collectAsState()
    val isPlayingPreview by audioManager.isPlayingPreview.collectAsState()

    var showRecordingModal by remember { mutableStateOf(false) }
    var showTrimmerSheet by remember { mutableStateOf(false) }
    var rawRecordedFile by remember { mutableStateOf<File?>(null) }
    var trimmedDurationSeconds by remember { mutableIntStateOf(0) }

    // Media attachment launcher
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val stream = context.contentResolver.openInputStream(uri)
                    val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
                    val out = FileOutputStream(file)
                    stream?.copyTo(out)
                    stream?.close()
                    out.close()

                    val uploadResult = CloudinaryConfig.uploadFile(file)
                    uploadResult.onSuccess { url ->
                        realtimeRepo.sendMessage(
                            chatId = chatId,
                            sender = currentUser,
                            receiver = peerUser,
                            text = "",
                            type = MessageType.IMAGE,
                            mediaUrl = url,
                            isViewOnce = isViewOnceNext
                        )
                        isViewOnceNext = false
                    }
                } catch (ignored: Exception) {}
            }
        }
    }

    // Observe messages
    LaunchedEffect(chatId) {
        realtimeRepo.observeMessages(chatId, currentUser.uid).collect { msgList ->
            messages = msgList
            if (msgList.isNotEmpty()) {
                listState.animateScrollToItem(msgList.size - 1)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WhatsAppDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Chat Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(WhatsAppDarkSurface)
                .padding(horizontal = 6.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = WhatsAppTextLight
                )
            }

            // Peer Avatar (Clickable to visit profile)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(WhatsAppDarkCard)
                    .clickable { onPeerProfileClick() },
                contentAlignment = Alignment.Center
            ) {
                if (!peerUser.avatarUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = peerUser.avatarUrl,
                        contentDescription = peerUser.displayName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = WhatsAppTextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Peer Name & Status
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onPeerProfileClick() }
            ) {
                Text(
                    text = peerUser.displayName.ifBlank { peerUser.username },
                    color = WhatsAppTextLight,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Text(
                    text = if (peerUser.isOnline) "online" else peerUser.getCleanUsername(),
                    color = if (peerUser.isOnline) WhatsAppLightGreen else WhatsAppTextMuted,
                    fontSize = 12.sp
                )
            }

            // Video Call Button
            IconButton(onClick = { onStartCall(peerUser, CallType.VIDEO) }) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = "Video Call",
                    tint = WhatsAppLightGreen
                )
            }

            // Audio Call Button
            IconButton(onClick = { onStartCall(peerUser, CallType.AUDIO) }) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Audio Call",
                    tint = WhatsAppLightGreen
                )
            }
        }

        // Messages LazyColumn
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    MessageBubble(
                        message = msg,
                        isSentByMe = msg.senderId == currentUser.uid,
                        onPhotoClick = onPhotoClick,
                        onPlayAudio = { audioUrl ->
                            // Play audio note
                        },
                        onViewOnceClick = { viewedMsg ->
                            if (!viewedMsg.mediaUrl.isNullOrEmpty()) {
                                onPhotoClick(viewedMsg.mediaUrl)
                                coroutineScope.launch {
                                    realtimeRepo.markViewOnceOpened(chatId, viewedMsg.id)
                                }
                            }
                        },
                        onToggleStar = { starMsg ->
                            coroutineScope.launch {
                                realtimeRepo.toggleStar(chatId, starMsg.id, starMsg.isStarred)
                            }
                        },
                        onDeleteForMe = { delMsg ->
                            coroutineScope.launch {
                                realtimeRepo.deleteForMe(chatId, delMsg.id, currentUser.uid)
                            }
                        },
                        onDeleteForEveryone = { delMsg ->
                            coroutineScope.launch {
                                realtimeRepo.deleteForEveryone(chatId, delMsg.id)
                            }
                        }
                    )
                }
            }
        }

        // Bottom Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Text Input Pill
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(26.dp))
                    .background(WhatsAppDarkSurface)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Attach Media (Cloudinary)
                IconButton(
                    onClick = { mediaPickerLauncher.launch("image/*") },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Attach",
                        tint = WhatsAppTextMuted
                    )
                }

                // View Once Toggle ("1" badge)
                IconButton(
                    onClick = { isViewOnceNext = !isViewOnceNext },
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isViewOnceNext) WhatsAppLightGreen else WhatsAppDarkCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "1",
                            color = if (isViewOnceNext) Color.Black else WhatsAppTextMuted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Message", color = WhatsAppTextMuted, fontSize = 15.sp) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = WhatsAppTextLight,
                        unfocusedTextColor = WhatsAppTextLight
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Quick Camera Icon
                IconButton(
                    onClick = { mediaPickerLauncher.launch("image/*") },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera",
                        tint = WhatsAppTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Voice Mic Button or Send Button
            if (inputText.isBlank()) {
                // Voice Recording Button
                IconButton(
                    onClick = {
                        val res = audioManager.startRecording()
                        if (res.isSuccess) {
                            showRecordingModal = true
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(WhatsAppLightGreen)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Record Voice Note",
                        tint = Color.Black
                    )
                }
            } else {
                // Send Text Message
                IconButton(
                    onClick = {
                        val textToSend = inputText.trim()
                        inputText = ""
                        coroutineScope.launch {
                            realtimeRepo.sendMessage(
                                chatId = chatId,
                                sender = currentUser,
                                receiver = peerUser,
                                text = textToSend,
                                type = MessageType.TEXT
                            )
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(WhatsAppLightGreen)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.Black
                    )
                }
            }
        }
    }

    // Voice Recording Studio Popup Modal
    if (showRecordingModal) {
        VoiceRecordingStudioModal(
            durationSeconds = recordingDuration,
            amplitudes = amplitudes,
            onCancel = {
                audioManager.cancelRecording()
                showRecordingModal = false
            },
            onStopAndEdit = {
                trimmedDurationSeconds = recordingDuration
                rawRecordedFile = audioManager.stopRecording()
                showRecordingModal = false
                showTrimmerSheet = true
            },
            onDirectSend = {
                trimmedDurationSeconds = recordingDuration
                val recordedFile = audioManager.stopRecording()
                showRecordingModal = false
                if (recordedFile != null) {
                    coroutineScope.launch {
                        val uploadRes = CloudinaryConfig.uploadFile(recordedFile, folder = "voice_notes")
                        uploadRes.onSuccess { url ->
                            realtimeRepo.sendMessage(
                                chatId = chatId,
                                sender = currentUser,
                                receiver = peerUser,
                                text = "",
                                type = MessageType.AUDIO,
                                mediaUrl = url,
                                audioDurationSeconds = trimmedDurationSeconds
                            )
                        }
                    }
                }
            }
        )
    }

    // Audio Trimming & Cropping Studio Sheet
    if (showTrimmerSheet && rawRecordedFile != null) {
        AudioTrimmerSheet(
            totalDurationSeconds = trimmedDurationSeconds,
            isPlaying = isPlayingPreview,
            onTogglePlay = { startMs, endMs ->
                if (isPlayingPreview) {
                    audioManager.stopPreview()
                } else {
                    rawRecordedFile?.let { file ->
                        audioManager.startPreview(file)
                    }
                }
            },
            onDiscard = {
                audioManager.stopPreview()
                rawRecordedFile?.delete()
                rawRecordedFile = null
                showTrimmerSheet = false
            },
            onSendTrimmed = { startMs, endMs ->
                audioManager.stopPreview()
                val originalFile = rawRecordedFile
                showTrimmerSheet = false
                if (originalFile != null) {
                    coroutineScope.launch {
                        val croppedResult = audioManager.cropAudio(originalFile, startMs, endMs)
                        val finalFile = croppedResult.getOrDefault(originalFile)
                        val uploadRes = CloudinaryConfig.uploadFile(finalFile, folder = "voice_notes")
                        uploadRes.onSuccess { url ->
                            val finalDuration = ((endMs - startMs) / 1000).toInt().coerceAtLeast(1)
                            realtimeRepo.sendMessage(
                                chatId = chatId,
                                sender = currentUser,
                                receiver = peerUser,
                                text = "",
                                type = MessageType.AUDIO,
                                mediaUrl = url,
                                audioDurationSeconds = finalDuration
                            )
                        }
                    }
                }
            }
        )
    }
}
