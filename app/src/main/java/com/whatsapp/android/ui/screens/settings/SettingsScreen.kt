package com.whatsapp.android.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.whatsapp.android.data.models.User
import com.whatsapp.android.ui.theme.*

@Composable
fun SettingsScreen(
    currentUser: User?,
    onProfileClick: () -> Unit,
    onAccountRecoveryClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onStarredClick: () -> Unit,
    onChatsClick: () -> Unit = {},
    onStorageClick: () -> Unit = {},
    onAppInfoClick: () -> Unit = {},
    onDeveloperProfileClick: () -> Unit = {},
    onSignOutClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(WhatsAppDarkBg)
    ) {
        // User Profile Header Card
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onProfileClick() }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(WhatsAppDarkCard),
                    contentAlignment = Alignment.Center
                ) {
                    if (!currentUser?.avatarUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = currentUser?.avatarUrl,
                            contentDescription = currentUser?.displayName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = WhatsAppTextMuted,
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentUser?.displayName?.ifBlank { "User" } ?: "User",
                        color = WhatsAppTextLight,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentUser?.getCleanUsername() ?: "@user",
                        color = WhatsAppLightGreen,
                        fontSize = 13.sp
                    )
                    Text(
                        text = currentUser?.bio ?: "Hey there! I am using WhatsApp.",
                        color = WhatsAppTextMuted,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = WhatsAppTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            HorizontalDivider(color = WhatsAppDarkCard, modifier = Modifier.padding(horizontal = 16.dp))
        }

        // Developer Profile Featured Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(WhatsAppDarkCard)
                    .clickable { onDeveloperProfileClick() }
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(WhatsAppLightGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Developer",
                            tint = WhatsAppLightGreen,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Raja Haider Ali",
                                color = WhatsAppTextLight,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = WhatsAppLightGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "03495031007 · @haideredits463",
                            color = WhatsAppLightGreen,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Rawalpindi Islamabad · Tap for contact & details",
                            color = WhatsAppTextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = WhatsAppLightGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            HorizontalDivider(color = WhatsAppDarkCard, modifier = Modifier.padding(horizontal = 16.dp))
        }

        // Settings Category Items
        item {
            SettingsItemRow(
                icon = Icons.Default.Security,
                title = "Account Security & Recovery",
                subtitle = "3-Option Forget Code, CNIC, Phone recovery & password",
                onClick = onAccountRecoveryClick
            )

            SettingsItemRow(
                icon = Icons.Default.Lock,
                title = "Privacy",
                subtitle = "Block contacts, read receipts, profile photo",
                onClick = onPrivacyClick
            )

            SettingsItemRow(
                icon = Icons.Default.Star,
                title = "Starred Messages",
                subtitle = "Pinned and favorite messages across chats",
                onClick = onStarredClick
            )

            SettingsItemRow(
                icon = Icons.Default.Chat,
                title = "Chats",
                subtitle = "Theme, wallpapers, chat history",
                onClick = onChatsClick
            )

            SettingsItemRow(
                icon = Icons.Default.Storage,
                title = "Storage and data",
                subtitle = "Cloudinary media storage and network usage",
                onClick = onStorageClick
            )

            SettingsItemRow(
                icon = Icons.Default.Info,
                title = "App Info",
                subtitle = "WhatsApp Native Android v1.0.0",
                onClick = onAppInfoClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sign Out Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSignOutClick() }
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(WhatsAppRed.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = null,
                        tint = WhatsAppRed,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Sign Out",
                    color = WhatsAppRed,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = WhatsAppTextMuted,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(20.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = WhatsAppTextLight,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = WhatsAppTextMuted,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}
