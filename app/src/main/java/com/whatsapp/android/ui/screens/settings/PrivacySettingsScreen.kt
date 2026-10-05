package com.whatsapp.android.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whatsapp.android.data.repository.RealtimeRepository
import com.whatsapp.android.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun PrivacySettingsScreen(
    currentUid: String,
    realtimeRepo: RealtimeRepository,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var readReceiptsEnabled by remember { mutableStateOf(true) }
    var blockedUsers by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(currentUid) {
        realtimeRepo.observeBlockedUsers(currentUid).collect {
            blockedUsers = it
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WhatsAppDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
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
                text = "Privacy",
                color = WhatsAppTextLight,
                fontSize = 19.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Text(
                    text = "Who can see my personal info",
                    color = WhatsAppTextMuted,
                    fontSize = 13.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )

                PrivacyInfoRow(title = "Last seen and online", subtitle = "Everyone")
                PrivacyInfoRow(title = "Profile photo", subtitle = "Everyone")
                PrivacyInfoRow(title = "About", subtitle = "Everyone")
                PrivacyInfoRow(title = "Status", subtitle = "My contacts")

                HorizontalDivider(color = WhatsAppDarkCard, modifier = Modifier.padding(vertical = 12.dp))

                // Read Receipts Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Read receipts",
                            color = WhatsAppTextLight,
                            fontSize = 16.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                        )
                        Text(
                            text = "If turned off, you won't send or receive read receipts (double blue ticks).",
                            color = WhatsAppTextMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Switch(
                        checked = readReceiptsEnabled,
                        onCheckedChange = { readReceiptsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = WhatsAppLightGreen,
                            uncheckedTrackColor = WhatsAppDarkInput
                        )
                    )
                }

                HorizontalDivider(color = WhatsAppDarkCard, modifier = Modifier.padding(vertical = 12.dp))

                Text(
                    text = "Disappearing messages & Security",
                    color = WhatsAppTextMuted,
                    fontSize = 13.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                )

                PrivacyInfoRow(title = "Default message timer", subtitle = "Off")

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Blocked Contacts (${blockedUsers.size})",
                    color = WhatsAppTextLight,
                    fontSize = 16.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
                )
            }

            if (blockedUsers.isEmpty()) {
                item {
                    Text(
                        text = "None. Contacts you block will not be able to call you or send you messages.",
                        color = WhatsAppTextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            } else {
                items(blockedUsers) { blockedUid ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Blocked User: $blockedUid",
                            color = WhatsAppTextLight,
                            fontSize = 14.sp
                        )

                        TextButton(
                            onClick = {
                                coroutineScope.launch {
                                    realtimeRepo.setBlockedStatus(currentUid, blockedUid, false)
                                }
                            }
                        ) {
                            Text("Unblock", color = WhatsAppLightGreen)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrivacyInfoRow(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(text = title, color = WhatsAppTextLight, fontSize = 15.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
        Text(text = subtitle, color = WhatsAppTextMuted, fontSize = 13.sp)
    }
}
