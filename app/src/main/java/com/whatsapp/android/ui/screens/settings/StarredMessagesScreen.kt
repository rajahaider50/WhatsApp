package com.whatsapp.android.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whatsapp.android.data.models.ChatMessage
import com.whatsapp.android.data.repository.RealtimeRepository
import com.whatsapp.android.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StarredMessagesScreen(
    currentUid: String,
    realtimeRepo: RealtimeRepository,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var starredList by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(currentUid) {
        realtimeRepo.observeStarredMessages(currentUid).collect {
            starredList = it
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WhatsAppDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = WhatsAppTextLight
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Starred Messages",
                color = WhatsAppTextLight,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = WhatsAppLightGreen)
            }
        } else if (starredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(WhatsAppDarkCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = WhatsAppTextMuted,
                            modifier = Modifier.size(44.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No Starred Messages",
                        color = WhatsAppTextLight,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Tap and hold any message in a chat and tap the Star icon to easily find it here later.",
                        color = WhatsAppTextMuted,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(starredList, key = { it.id }) { msg ->
                    StarredMessageItemCard(
                        msg = msg,
                        isSentByMe = msg.senderId == currentUid,
                        onUnstar = {
                            coroutineScope.launch {
                                realtimeRepo.toggleStar(msg.chatId, msg.id, currentStarred = true)
                            }
                        },
                        onCopy = {
                            clipboardManager.setText(AnnotatedString(msg.text))
                            Toast.makeText(context, "Message copied", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StarredMessageItemCard(
    msg: ChatMessage,
    isSentByMe: Boolean,
    onUnstar: () -> Unit,
    onCopy: () -> Unit
) {
    val timeFormatted = remember(msg.timestamp) {
        SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()).format(Date(msg.timestamp))
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSentByMe) WhatsAppOutgoingBubble else WhatsAppDarkCard),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isSentByMe) "You" else msg.senderName,
                    color = WhatsAppLightGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Starred",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onUnstar() }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = WhatsAppTextMuted,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onCopy() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = msg.text,
                color = WhatsAppTextLight,
                fontSize = 15.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = timeFormatted,
                color = WhatsAppTextMuted,
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
