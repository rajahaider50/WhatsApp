package com.whatsapp.android.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material.icons.automirrored.filled.CallReceived
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
import coil.compose.AsyncImage
import com.whatsapp.android.data.models.CallSession
import com.whatsapp.android.data.models.CallStatus
import com.whatsapp.android.data.models.CallType
import com.whatsapp.android.data.models.User
import com.whatsapp.android.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CallsTab(
    currentUid: String,
    callHistory: List<CallSession>,
    onStartCall: (User, CallType) -> Unit,
    onNewCallClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WhatsAppDarkBg)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            // Create call link header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(WhatsAppLightGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Create call link",
                            color = WhatsAppTextLight,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Share a link for your WhatsApp call",
                            color = WhatsAppTextMuted,
                            fontSize = 13.sp
                        )
                    }
                }

                Text(
                    text = "Recent",
                    color = WhatsAppTextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 8.dp)
                )
            }

            if (callHistory.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = null,
                                tint = WhatsAppTextMuted,
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "To start calling contacts, tap the call button below",
                                color = WhatsAppTextMuted,
                                fontSize = 13.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(callHistory, key = { it.callId }) { session ->
                    val isOutgoing = session.callerUid == currentUid
                    val peerName = if (isOutgoing) session.receiverName else session.callerName
                    val peerAvatar = if (isOutgoing) session.receiverAvatar else session.callerAvatar
                    val peerUid = if (isOutgoing) session.receiverUid else session.callerUid
                    val isVideo = session.type == CallType.VIDEO.name
                    val isMissed = session.status == CallStatus.MISSED.name || session.status == CallStatus.REJECTED.name

                    val timeStr = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(session.timestamp))

                    val peerUser = User(
                        uid = peerUid,
                        username = "@user",
                        displayName = peerName,
                        avatarUrl = peerAvatar
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onStartCall(peerUser, if (isVideo) CallType.VIDEO else CallType.AUDIO)
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(WhatsAppDarkCard),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!peerAvatar.isNullOrEmpty()) {
                                AsyncImage(
                                    model = peerAvatar,
                                    contentDescription = peerName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(Icons.Default.Person, contentDescription = null, tint = WhatsAppTextMuted)
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = peerName,
                                color = if (isMissed) WhatsAppRed else WhatsAppTextLight,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                val callIcon = when {
                                    isMissed -> Icons.AutoMirrored.Filled.CallMissed
                                    isOutgoing -> Icons.AutoMirrored.Filled.CallMade
                                    else -> Icons.AutoMirrored.Filled.CallReceived
                                }
                                val iconColor = if (isMissed) WhatsAppRed else WhatsAppLightGreen

                                Icon(
                                    imageVector = callIcon,
                                    contentDescription = null,
                                    tint = iconColor,
                                    modifier = Modifier.size(15.dp)
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                Text(
                                    text = timeStr,
                                    color = WhatsAppTextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        IconButton(onClick = {
                            onStartCall(peerUser, if (isVideo) CallType.VIDEO else CallType.AUDIO)
                        }) {
                            Icon(
                                imageVector = if (isVideo) Icons.Default.Videocam else Icons.Default.Call,
                                contentDescription = "Call",
                                tint = WhatsAppLightGreen
                            )
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onNewCallClick,
            containerColor = WhatsAppLightGreen,
            contentColor = Color.Black,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp)
        ) {
            Icon(Icons.Default.AddCall, contentDescription = "New Call", modifier = Modifier.size(24.dp))
        }
    }
}
