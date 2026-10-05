package com.whatsapp.android.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.whatsapp.android.data.models.StatusPrivacy
import com.whatsapp.android.data.models.StatusStory
import com.whatsapp.android.data.models.User
import com.whatsapp.android.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StatusTab(
    currentUser: User?,
    statuses: List<StatusStory>,
    onAddStatusClick: () -> Unit,
    onStatusClick: (StatusStory) -> Unit,
    onPrivacyClick: () -> Unit
) {
    var showPrivacyModal by remember { mutableStateOf(false) }
    var privacyMode by remember { mutableStateOf("MY_CONTACTS") }
    var excludedCount by remember { mutableIntStateOf(0) }
    var selectedCount by remember { mutableIntStateOf(0) }

    val myStatuses = statuses.filter { it.uid == currentUser?.uid }
    val otherStatuses = statuses.filter { it.uid != currentUser?.uid }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(WhatsAppDarkBg)
            .padding(vertical = 8.dp)
    ) {
        // Status Privacy Bar with dynamic counter display
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showPrivacyModal = true }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(WhatsAppDarkCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = WhatsAppLightGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Status Privacy",
                            color = WhatsAppTextLight,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        val privacySubtitle = when (privacyMode) {
                            "CONTACTS_EXCEPT" -> "My contacts except ($excludedCount excluded)"
                            "ONLY_SHARE_WITH" -> "Only share with ($selectedCount selected)"
                            else -> "My contacts (All contacts)"
                        }
                        Text(
                            text = privacySubtitle,
                            color = WhatsAppLightGreen,
                            fontSize = 12.sp
                        )
                    }
                }

                IconButton(onClick = { showPrivacyModal = true }) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Configure Privacy",
                        tint = WhatsAppLightGreen
                    )
                }
            }

            HorizontalDivider(color = WhatsAppDarkCard, modifier = Modifier.padding(vertical = 4.dp))
        }

        // My Status Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (myStatuses.isNotEmpty()) onStatusClick(myStatuses.first()) else onAddStatusClick()
                    }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val avatarBorder = if (myStatuses.isNotEmpty()) {
                        Modifier.border(2.5.dp, WhatsAppLightGreen, CircleShape)
                    } else Modifier

                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(WhatsAppDarkCard)
                            .then(avatarBorder),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!currentUser?.avatarUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = currentUser?.avatarUrl,
                                contentDescription = "My Status",
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
                    }

                    // Green "+" badge
                    if (myStatuses.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(WhatsAppLightGreen)
                                .align(Alignment.BottomEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add status",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "My Status",
                        color = WhatsAppTextLight,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (myStatuses.isNotEmpty()) "${myStatuses.size} active update(s)" else "Tap to add status update",
                        color = WhatsAppTextMuted,
                        fontSize = 13.sp
                    )
                }

                IconButton(onClick = onAddStatusClick) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera",
                        tint = WhatsAppLightGreen
                    )
                }
            }
        }

        // Recent Updates Section Header
        if (otherStatuses.isNotEmpty()) {
            item {
                Text(
                    text = "Recent updates",
                    color = WhatsAppTextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                )
            }

            items(otherStatuses, key = { it.id }) { story ->
                val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(story.timestamp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onStatusClick(story) }
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .border(2.5.dp, WhatsAppLightGreen, CircleShape)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(WhatsAppDarkCard),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!story.avatarUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = story.avatarUrl,
                                contentDescription = story.displayName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = WhatsAppTextMuted,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = story.displayName.ifBlank { story.username },
                            color = WhatsAppTextLight,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = timeStr,
                            color = WhatsAppTextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }

    // Status Privacy Settings Dialog with interactive live counter
    if (showPrivacyModal) {
        AlertDialog(
            onDismissRequest = { showPrivacyModal = false },
            containerColor = WhatsAppDarkCard,
            title = {
                Text(
                    text = "Status Privacy",
                    color = WhatsAppTextLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Who can see my status updates:",
                        color = WhatsAppTextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // 1. My Contacts
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { privacyMode = "MY_CONTACTS" }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = privacyMode == "MY_CONTACTS",
                            onClick = { privacyMode = "MY_CONTACTS" },
                            colors = RadioButtonDefaults.colors(selectedColor = WhatsAppLightGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("My contacts", color = WhatsAppTextLight, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("Share with all contacts", color = WhatsAppTextMuted, fontSize = 12.sp)
                        }
                    }

                    // 2. My Contacts Except...
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { privacyMode = "CONTACTS_EXCEPT" }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = privacyMode == "CONTACTS_EXCEPT",
                            onClick = { privacyMode = "CONTACTS_EXCEPT" },
                            colors = RadioButtonDefaults.colors(selectedColor = WhatsAppLightGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("My contacts except...", color = WhatsAppTextLight, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("$excludedCount contacts excluded", color = if (privacyMode == "CONTACTS_EXCEPT") WhatsAppLightGreen else WhatsAppTextMuted, fontSize = 12.sp)
                        }

                        if (privacyMode == "CONTACTS_EXCEPT") {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { if (excludedCount > 0) excludedCount-- },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = WhatsAppLightGreen)
                                }
                                Text("$excludedCount", color = WhatsAppLightGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                                IconButton(
                                    onClick = { excludedCount++ },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase", tint = WhatsAppLightGreen)
                                }
                            }
                        }
                    }

                    // 3. Only Share With...
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { privacyMode = "ONLY_SHARE_WITH" }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = privacyMode == "ONLY_SHARE_WITH",
                            onClick = { privacyMode = "ONLY_SHARE_WITH" },
                            colors = RadioButtonDefaults.colors(selectedColor = WhatsAppLightGreen)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Only share with...", color = WhatsAppTextLight, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            Text("$selectedCount contacts selected", color = if (privacyMode == "ONLY_SHARE_WITH") WhatsAppLightGreen else WhatsAppTextMuted, fontSize = 12.sp)
                        }

                        if (privacyMode == "ONLY_SHARE_WITH") {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { if (selectedCount > 0) selectedCount-- },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = WhatsAppLightGreen)
                                }
                                Text("$selectedCount", color = WhatsAppLightGreen, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                                IconButton(
                                    onClick = { selectedCount++ },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase", tint = WhatsAppLightGreen)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyModal = false },
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppLightGreen)
                ) {
                    Text("Save Privacy", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPrivacyModal = false }) {
                    Text("Cancel", color = WhatsAppTextMuted)
                }
            }
        )
    }
}
