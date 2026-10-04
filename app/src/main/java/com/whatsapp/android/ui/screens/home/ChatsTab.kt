package com.whatsapp.android.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.whatsapp.android.data.models.ChatPreview
import com.whatsapp.android.data.models.MessageStatus
import com.whatsapp.android.data.models.User
import com.whatsapp.android.ui.components.ProfileQuickPopup
import com.whatsapp.android.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatsTab(
    chats: List<ChatPreview>,
    currentUid: String,
    onChatClick: (ChatPreview) -> Unit,
    onNewChatClick: () -> Unit,
    onAudioCallClick: (User) -> Unit,
    onVideoCallClick: (User) -> Unit,
    onInfoClick: (User) -> Unit,
    onPhotoClick: (String?) -> Unit
) {
    var quickPopupUser by remember { mutableStateOf<User?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredChats = if (searchQuery.isBlank()) {
        chats
    } else {
        chats.filter {
            it.peerDisplayName.contains(searchQuery, ignoreCase = true) ||
            it.peerUsername.contains(searchQuery, ignoreCase = true) ||
            it.lastMessage.contains(searchQuery, ignoreCase = true)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WhatsAppDarkBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search chats or messages...", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = WhatsAppTextMuted)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = WhatsAppTextMuted)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = WhatsAppDarkCard,
                        unfocusedContainerColor = WhatsAppDarkCard,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = WhatsAppTextLight,
                        unfocusedTextColor = WhatsAppTextLight
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                )
            }

            if (filteredChats.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = null,
                            tint = WhatsAppTextMuted,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isEmpty()) "No chats yet" else "No matching chats",
                            color = WhatsAppTextLight,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isEmpty()) "Tap the green button below to start chatting with anyone via @username" else "Try searching with a different keyword",
                            color = WhatsAppTextMuted,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredChats, key = { it.chatId }) { chat ->
                        ChatItemRow(
                            chat = chat,
                            isSentByMe = chat.lastMessageSenderId == currentUid,
                            onRowClick = { onChatClick(chat) },
                            onAvatarClick = {
                                quickPopupUser = User(
                                    uid = chat.peerUid,
                                    username = chat.peerUsername,
                                    displayName = chat.peerDisplayName,
                                    avatarUrl = chat.peerAvatarUrl,
                                    isOnline = chat.isOnline
                                )
                            }
                        )
                    }
                }
            }
        }

        // WhatsApp Floating Action Button for New Chat
        FloatingActionButton(
            onClick = onNewChatClick,
            containerColor = WhatsAppLightGreen,
            contentColor = Color.Black,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Chat,
                contentDescription = "New Chat",
                modifier = Modifier.size(24.dp)
            )
        }

        // Quick Profile Popup Modal
        quickPopupUser?.let { user ->
            ProfileQuickPopup(
                user = user,
                onDismiss = { quickPopupUser = null },
                onPhotoClick = { url ->
                    quickPopupUser = null
                    onPhotoClick(url)
                },
                onMessageClick = {
                    val matchingChat = chats.find { it.peerUid == user.uid }
                    if (matchingChat != null) {
                        onChatClick(matchingChat)
                    } else {
                        onChatClick(
                            ChatPreview(
                                chatId = if (currentUid < user.uid) "${currentUid}_${user.uid}" else "${user.uid}_${currentUid}",
                                peerUid = user.uid,
                                peerUsername = user.username,
                                peerDisplayName = user.displayName,
                                peerAvatarUrl = user.avatarUrl
                            )
                        )
                    }
                },
                onAudioCallClick = { onAudioCallClick(user) },
                onVideoCallClick = { onVideoCallClick(user) },
                onInfoClick = { onInfoClick(user) }
            )
        }
    }
}

@Composable
private fun ChatItemRow(
    chat: ChatPreview,
    isSentByMe: Boolean,
    onRowClick: () -> Unit,
    onAvatarClick: () -> Unit
) {
    val timeFormatted = remember(chat.lastTimestamp) {
        if (chat.lastTimestamp == 0L) ""
        else {
            val date = Date(chat.lastTimestamp)
            val now = System.currentTimeMillis()
            val diff = now - chat.lastTimestamp
            if (diff < 24 * 60 * 60 * 1000) {
                SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
            } else {
                SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(date)
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRowClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular Avatar with click handler for Quick Popup
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(WhatsAppDarkCard)
                .clickable { onAvatarClick() },
            contentAlignment = Alignment.Center
        ) {
            if (!chat.peerAvatarUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = chat.peerAvatarUrl,
                    contentDescription = chat.peerDisplayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = WhatsAppTextMuted,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Online indicator dot
            if (chat.isOnline) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(WhatsAppLightGreen)
                        .align(Alignment.BottomEnd)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Center: Name & Last Message with Status Ticks
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.peerDisplayName.ifBlank { chat.peerUsername },
                    color = WhatsAppTextLight,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Text(
                    text = timeFormatted,
                    color = if (chat.unreadCount > 0) WhatsAppLightGreen else WhatsAppTextMuted,
                    fontSize = 12.sp,
                    fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Official Message Status Ticks (Single grey / Double grey / Double blue)
                if (isSentByMe) {
                    when (chat.lastMessageStatus) {
                        MessageStatus.SENT.name -> {
                            // Single grey tick
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Sent",
                                tint = WhatsAppGreyTick,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        MessageStatus.DELIVERED.name -> {
                            // Double grey tick
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Delivered",
                                tint = WhatsAppGreyTick,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        MessageStatus.SEEN.name -> {
                            // Double blue tick
                            Icon(
                                imageVector = Icons.Default.DoneAll,
                                contentDescription = "Read",
                                tint = WhatsAppBlueTick,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = chat.lastMessage.ifBlank { chat.peerUsername },
                    color = WhatsAppTextMuted,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Unread Count Badge
                if (chat.unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .clip(CircleShape)
                            .background(WhatsAppLightGreen)
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${chat.unreadCount}",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
