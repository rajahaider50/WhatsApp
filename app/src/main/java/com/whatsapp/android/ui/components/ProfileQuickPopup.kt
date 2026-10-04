package com.whatsapp.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.whatsapp.android.data.models.User
import com.whatsapp.android.ui.theme.WhatsAppDarkCard
import com.whatsapp.android.ui.theme.WhatsAppDarkSurface
import com.whatsapp.android.ui.theme.WhatsAppLightGreen
import com.whatsapp.android.ui.theme.WhatsAppTextLight
import com.whatsapp.android.ui.theme.WhatsAppTextMuted

@Composable
fun ProfileQuickPopup(
    user: User,
    onDismiss: () -> Unit,
    onPhotoClick: (String?) -> Unit,
    onMessageClick: () -> Unit,
    onAudioCallClick: () -> Unit,
    onVideoCallClick: () -> Unit,
    onInfoClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WhatsAppDarkSurface),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Photo header with User Name
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(WhatsAppDarkCard)
                        .clickable { onPhotoClick(user.avatarUrl) }
                ) {
                    if (!user.avatarUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = user.avatarUrl,
                            contentDescription = user.displayName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF263238)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = WhatsAppTextMuted,
                                modifier = Modifier.size(100.dp)
                            )
                        }
                    }

                    // Display Name overlay at top
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = user.displayName.ifBlank { user.username },
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // 4 WhatsApp Action Buttons (Message, Audio Call, Video Call, Info)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WhatsAppDarkSurface)
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Message
                    IconButton(onClick = {
                        onDismiss()
                        onMessageClick()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = "Message",
                            tint = WhatsAppLightGreen,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // 2. Audio Call
                    IconButton(onClick = {
                        onDismiss()
                        onAudioCallClick()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Audio Call",
                            tint = WhatsAppLightGreen,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // 3. Video Call
                    IconButton(onClick = {
                        onDismiss()
                        onVideoCallClick()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Video Call",
                            tint = WhatsAppLightGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // 4. Info (View Full Profile)
                    IconButton(onClick = {
                        onDismiss()
                        onInfoClick()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = WhatsAppLightGreen,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}
