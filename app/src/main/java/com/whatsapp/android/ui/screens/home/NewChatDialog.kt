package com.whatsapp.android.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.whatsapp.android.data.models.User
import com.whatsapp.android.data.repository.RealtimeRepository
import com.whatsapp.android.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun NewChatDialog(
    realtimeRepo: RealtimeRepository,
    onDismiss: () -> Unit,
    onUserSelected: (User) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var usernameQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var searchResult by remember { mutableStateOf<User?>(null) }
    var searchError by remember { mutableStateOf<String?>(null) }

    fun doSearch() {
        val clean = usernameQuery.removePrefix("@").trim()
        if (clean.isEmpty()) return

        isSearching = true
        searchError = null
        searchResult = null

        coroutineScope.launch {
            val user = realtimeRepo.findUserByUsername(clean)
            isSearching = false
            if (user != null) {
                searchResult = user
            } else {
                searchError = "No user found with username @$clean"
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = WhatsAppDarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "New Chat",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = WhatsAppTextLight
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = WhatsAppTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Enter the user's @username to start a conversation:",
                    fontSize = 13.sp,
                    color = WhatsAppTextMuted
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = if (usernameQuery.startsWith("@")) usernameQuery else if (usernameQuery.isNotEmpty()) "@$usernameQuery" else "",
                    onValueChange = {
                        usernameQuery = it
                        searchError = null
                    },
                    label = { Text("Username (@xxxxxx)") },
                    placeholder = { Text("@username") },
                    leadingIcon = {
                        Icon(Icons.Default.AlternateEmail, contentDescription = null, tint = WhatsAppTextMuted)
                    },
                    trailingIcon = {
                        IconButton(onClick = { doSearch() }) {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = WhatsAppLightGreen)
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WhatsAppLightGreen,
                        unfocusedBorderColor = WhatsAppDarkInput,
                        focusedLabelColor = WhatsAppLightGreen,
                        unfocusedLabelColor = WhatsAppTextMuted,
                        focusedTextColor = WhatsAppTextLight,
                        unfocusedTextColor = WhatsAppTextLight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isSearching) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = WhatsAppLightGreen, modifier = Modifier.size(28.dp))
                    }
                }

                if (searchError != null) {
                    Text(
                        text = searchError ?: "",
                        color = WhatsAppRed,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                searchResult?.let { foundUser ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(WhatsAppDarkCard)
                            .clickable {
                                onDismiss()
                                onUserSelected(foundUser)
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF263238)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!foundUser.avatarUrl.isNullOrEmpty()) {
                                AsyncImage(
                                    model = foundUser.avatarUrl,
                                    contentDescription = foundUser.displayName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(Icons.Default.Person, contentDescription = null, tint = WhatsAppTextMuted)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = foundUser.displayName,
                                color = WhatsAppTextLight,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = foundUser.username,
                                color = WhatsAppLightGreen,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = {
                                onDismiss()
                                onUserSelected(foundUser)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppLightGreen),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text("Chat", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
