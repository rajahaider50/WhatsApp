package com.whatsapp.android.ui.screens.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.whatsapp.android.data.models.ChatMessage
import com.whatsapp.android.data.models.MessageStatus
import com.whatsapp.android.data.models.MessageType
import com.whatsapp.android.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: ChatMessage,
    isSentByMe: Boolean,
    onPhotoClick: (String) -> Unit,
    onPlayAudio: (String) -> Unit,
    onViewOnceClick: (ChatMessage) -> Unit,
    onToggleStar: (ChatMessage) -> Unit,
    onDeleteForMe: (ChatMessage) -> Unit,
    onDeleteForEveryone: (ChatMessage) -> Unit
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    val timeFormatted = remember(message.timestamp) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp))
    }

    val bubbleShape = if (isSentByMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    } else {
        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
    }

    val bubbleColor = if (isSentByMe) WhatsAppOutgoingBubble else WhatsAppIncomingBubble

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp),
        contentAlignment = if (isSentByMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(bubbleShape)
                .background(bubbleColor)
                .combinedClickable(
                    onClick = {},
                    onLongClick = { showMenu = true }
                )
                .padding(8.dp)
        ) {
            // View-Once Media Message
            if (message.isViewOnce) {
                if (!message.isViewed) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(WhatsAppDarkCard)
                            .clickable { onViewOnceClick(message) }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(WhatsAppTeal),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("1", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Photo (View Once)",
                            color = WhatsAppTextLight,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = WhatsAppTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Opened",
                            color = WhatsAppTextMuted,
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            } else if (message.isDeletedForEveryone) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        tint = WhatsAppTextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "This message was deleted",
                        color = WhatsAppTextMuted,
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic
                    )
                }
            } else {
                // Media Preview (Image)
                if (message.type == MessageType.IMAGE.name && !message.mediaUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = message.mediaUrl,
                        contentDescription = "Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onPhotoClick(message.mediaUrl) }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Voice Note / Audio Message
                if (message.type == MessageType.AUDIO.name && !message.mediaUrl.isNullOrEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(WhatsAppDarkCard.copy(alpha = 0.6f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onPlayAudio(message.mediaUrl) },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(WhatsAppLightGreen)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.Black)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "Voice Note (${message.audioDurationSeconds}s)",
                                color = WhatsAppTextLight,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "HD Audio",
                                color = WhatsAppLightGreen,
                                fontSize = 11.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Document / PDF Message
                if (message.type == MessageType.DOCUMENT.name) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(WhatsAppDarkCard.copy(alpha = 0.6f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = WhatsAppLightGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = message.fileName ?: "Document.pdf",
                            color = WhatsAppTextLight,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Text Content
                if (message.text.isNotEmpty()) {
                    Text(
                        text = message.text,
                        color = WhatsAppTextLight,
                        fontSize = 15.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            // Bottom row: Star, Timestamp & Delivery Ticks
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (message.isStarred) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Starred",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = timeFormatted,
                    color = WhatsAppTextMuted,
                    fontSize = 11.sp
                )

                if (isSentByMe && !message.isDeletedForEveryone) {
                    Spacer(modifier = Modifier.width(4.dp))
                    when (message.status) {
                        MessageStatus.SENT.name -> {
                            // Single grey tick (Receiver network off/offline)
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Sent",
                                tint = WhatsAppGreyTick,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        MessageStatus.DELIVERED.name -> {
                            // Double grey tick (Delivered)
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Delivered",
                                tint = WhatsAppGreyTick,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        MessageStatus.SEEN.name -> {
                            // Double blue tick (Seen/Read)
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Seen",
                                tint = WhatsAppBlueTick,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }
        }

        // Dropdown Menu for Message Actions (Copy, Star, Delete)
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier.background(WhatsAppDarkSurface)
        ) {
            // Copy Message Text
            if (message.text.isNotEmpty()) {
                DropdownMenuItem(
                    text = { Text("Copy Text", color = WhatsAppTextLight) },
                    leadingIcon = { Icon(Icons.Default.ContentCopy, null, tint = WhatsAppTextMuted) },
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("WhatsApp Message", message.text))
                        showMenu = false
                    }
                )
            }

            // Star / Unstar
            DropdownMenuItem(
                text = { Text(if (message.isStarred) "Unstar" else "Star", color = WhatsAppTextLight) },
                leadingIcon = { Icon(Icons.Default.Star, null, tint = WhatsAppTextMuted) },
                onClick = {
                    onToggleStar(message)
                    showMenu = false
                }
            )

            // Delete For Me
            DropdownMenuItem(
                text = { Text("Delete for me", color = WhatsAppTextLight) },
                leadingIcon = { Icon(Icons.Default.DeleteOutline, null, tint = WhatsAppTextMuted) },
                onClick = {
                    onDeleteForMe(message)
                    showMenu = false
                }
            )

            // Delete For Everyone (if sent by me)
            if (isSentByMe && !message.isDeletedForEveryone) {
                DropdownMenuItem(
                    text = { Text("Delete for everyone", color = WhatsAppRed) },
                    leadingIcon = { Icon(Icons.Default.DeleteForever, null, tint = WhatsAppRed) },
                    onClick = {
                        onDeleteForEveryone(message)
                        showMenu = false
                    }
                )
            }
        }
    }
}
