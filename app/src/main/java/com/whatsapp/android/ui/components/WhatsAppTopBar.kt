package com.whatsapp.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whatsapp.android.ui.theme.WhatsAppDarkSurface
import com.whatsapp.android.ui.theme.WhatsAppTextLight
import com.whatsapp.android.ui.theme.WhatsAppTextMuted

@Composable
fun WhatsAppTopBar(
    title: String = "WhatsApp",
    onCameraClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onStarredClick: () -> Unit = {},
    onRecoveryClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WhatsAppDarkSurface)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold,
                color = WhatsAppTextLight,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onCameraClick) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Camera",
                    tint = WhatsAppTextMuted
                )
            }

            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = WhatsAppTextMuted
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = WhatsAppTextMuted
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(WhatsAppDarkSurface)
                ) {
                    DropdownMenuItem(
                        text = { Text("Profile & Avatar", color = WhatsAppTextLight) },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = WhatsAppTextMuted) },
                        onClick = {
                            showMenu = false
                            onProfileClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Account Security & Recovery", color = WhatsAppTextLight) },
                        leadingIcon = { Icon(Icons.Default.Security, null, tint = WhatsAppTextMuted) },
                        onClick = {
                            showMenu = false
                            onRecoveryClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Starred Messages", color = WhatsAppTextLight) },
                        leadingIcon = { Icon(Icons.Default.Star, null, tint = WhatsAppTextMuted) },
                        onClick = {
                            showMenu = false
                            onStarredClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Settings", color = WhatsAppTextLight) },
                        leadingIcon = { Icon(Icons.Default.Settings, null, tint = WhatsAppTextMuted) },
                        onClick = {
                            showMenu = false
                            onSettingsClick()
                        }
                    )
                    HorizontalDivider(color = Color(0xFF222D34))
                    DropdownMenuItem(
                        text = { Text("Sign Out", color = Color(0xFFF15C5C)) },
                        leadingIcon = { Icon(Icons.Default.ExitToApp, null, tint = Color(0xFFF15C5C)) },
                        onClick = {
                            showMenu = false
                            onSignOutClick()
                        }
                    )
                }
            }
        }
    }
}
