package com.whatsapp.android.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.whatsapp.android.data.models.CallType
import com.whatsapp.android.data.models.User
import com.whatsapp.android.data.repository.RealtimeRepository
import com.whatsapp.android.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun UserProfileScreen(
    user: User,
    currentUid: String,
    realtimeRepo: RealtimeRepository,
    onBack: () -> Unit,
    onMessageClick: () -> Unit,
    onStartCall: (User, CallType) -> Unit,
    onPhotoClick: (String?) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isBlocked by remember { mutableStateOf(false) }

    LaunchedEffect(user.uid) {
        realtimeRepo.observeBlockedUsers(currentUid).collect { blockedList ->
            isBlocked = blockedList.contains(user.uid)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WhatsAppDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Top Back Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = WhatsAppTextLight
                )
            }
        }

        // Profile Photo and Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(WhatsAppDarkCard)
                    .clickable { onPhotoClick(user.avatarUrl) },
                contentAlignment = Alignment.Center
            ) {
                if (!user.avatarUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = user.avatarUrl,
                        contentDescription = user.displayName,
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

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = user.displayName.ifBlank { user.username },
                color = WhatsAppTextLight,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = user.getCleanUsername(),
                color = WhatsAppLightGreen,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3 Action Buttons (Message, Audio Call, Video Call)
            Row(
                modifier = Modifier.fillMaxWidth(0.85f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileActionButton(
                    icon = Icons.Default.ChatBubble,
                    label = "Message",
                    onClick = onMessageClick
                )

                ProfileActionButton(
                    icon = Icons.Default.Call,
                    label = "Audio",
                    onClick = { onStartCall(user, CallType.AUDIO) }
                )

                ProfileActionButton(
                    icon = Icons.Default.Videocam,
                    label = "Video",
                    onClick = { onStartCall(user, CallType.VIDEO) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // About / Bio Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WhatsAppDarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "About",
                    color = WhatsAppLightGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = user.bio.ifBlank { "Hey there! I am using WhatsApp." },
                    color = WhatsAppTextLight,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Encryption Badge
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WhatsAppDarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = WhatsAppLightGreen,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Encryption",
                        color = WhatsAppTextLight,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Messages and calls are end-to-end encrypted. No one outside of this chat can read or listen to them.",
                        color = WhatsAppTextMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Block / Unblock User Row
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WhatsAppDarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        coroutineScope.launch {
                            realtimeRepo.setBlockedStatus(currentUid, user.uid, !isBlocked)
                        }
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = null,
                    tint = if (isBlocked) WhatsAppLightGreen else WhatsAppRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = if (isBlocked) "Unblock ${user.displayName}" else "Block ${user.displayName}",
                    color = if (isBlocked) WhatsAppLightGreen else WhatsAppRed,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun ProfileActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(WhatsAppDarkCard),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = WhatsAppLightGreen,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = WhatsAppLightGreen,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
