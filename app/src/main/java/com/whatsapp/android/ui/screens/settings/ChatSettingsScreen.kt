package com.whatsapp.android.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whatsapp.android.ui.theme.*

@Composable
fun ChatSettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedTheme by remember { mutableStateOf("Dark (WhatsApp Default)") }
    var selectedWallpaper by remember { mutableStateOf("Classic WhatsApp Doodle") }
    var selectedFontSize by remember { mutableStateOf("Medium") }
    var enterIsSend by remember { mutableStateOf(true) }
    var mediaVisibility by remember { mutableStateOf(true) }

    var showThemeDialog by remember { mutableStateOf(false) }
    var showWallpaperDialog by remember { mutableStateOf(false) }
    var showFontDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WhatsAppDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
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
                text = "Chats & Customization",
                color = WhatsAppTextLight,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Display & Styling",
            color = WhatsAppLightGreen,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        // Theme Row
        ChatSettingRow(
            icon = Icons.Default.BrightnessMedium,
            title = "Theme",
            subtitle = selectedTheme,
            onClick = { showThemeDialog = true }
        )

        // Wallpaper Row
        ChatSettingRow(
            icon = Icons.Default.Wallpaper,
            title = "Wallpaper",
            subtitle = selectedWallpaper,
            onClick = { showWallpaperDialog = true }
        )

        // Font Size Row
        ChatSettingRow(
            icon = Icons.Default.FormatSize,
            title = "Font size",
            subtitle = selectedFontSize,
            onClick = { showFontDialog = true }
        )

        HorizontalDivider(color = WhatsAppDarkCard, modifier = Modifier.padding(vertical = 12.dp))

        Text(
            text = "Chat Settings",
            color = WhatsAppLightGreen,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        // Enter is Send switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Enter is send",
                    color = WhatsAppTextLight,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Enter key will send your message directly",
                    color = WhatsAppTextMuted,
                    fontSize = 12.sp
                )
            }
            Switch(
                checked = enterIsSend,
                onCheckedChange = { enterIsSend = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = WhatsAppLightGreen,
                    uncheckedTrackColor = WhatsAppDarkInput
                )
            )
        }

        // Media Visibility switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Media visibility",
                    color = WhatsAppTextLight,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Show newly downloaded media in your phone's gallery",
                    color = WhatsAppTextMuted,
                    fontSize = 12.sp
                )
            }
            Switch(
                checked = mediaVisibility,
                onCheckedChange = { mediaVisibility = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = WhatsAppLightGreen,
                    uncheckedTrackColor = WhatsAppDarkInput
                )
            )
        }

        HorizontalDivider(color = WhatsAppDarkCard, modifier = Modifier.padding(vertical = 12.dp))

        // Chat Backup
        ChatSettingRow(
            icon = Icons.Default.CloudUpload,
            title = "Chat backup",
            subtitle = "Backed up securely to Firebase Realtime Database",
            onClick = {
                Toast.makeText(context, "All chats are synced live with Firebase!", Toast.LENGTH_SHORT).show()
            }
        )

        // Clear All Chats
        ChatSettingRow(
            icon = Icons.Default.DeleteOutline,
            title = "Chat history",
            subtitle = "Clear chat history or export chat",
            onClick = {
                Toast.makeText(context, "Chat history management active", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Theme Selection Dialog
    if (showThemeDialog) {
        val themes = listOf("Dark (WhatsApp Default)", "Light", "AMOLED Deep Black", "WhatsApp Emerald Green")
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            containerColor = WhatsAppDarkCard,
            title = { Text("Choose Theme", color = WhatsAppTextLight, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    themes.forEach { t ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedTheme = t
                                    showThemeDialog = false
                                    Toast.makeText(context, "Theme set to $t", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedTheme == t,
                                onClick = {
                                    selectedTheme = t
                                    showThemeDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = WhatsAppLightGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = t, color = WhatsAppTextLight, fontSize = 15.sp)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Wallpaper Selection Dialog
    if (showWallpaperDialog) {
        val wallpapers = listOf(
            "Classic WhatsApp Doodle",
            "Midnight Charcoal (#0B141A)",
            "Deep Emerald (#075E54)",
            "Forest Pine (#128C7E)"
        )
        AlertDialog(
            onDismissRequest = { showWallpaperDialog = false },
            containerColor = WhatsAppDarkCard,
            title = { Text("Choose Chat Wallpaper", color = WhatsAppTextLight, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    wallpapers.forEach { wp ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedWallpaper = wp
                                    showWallpaperDialog = false
                                    Toast.makeText(context, "Wallpaper updated", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedWallpaper == wp,
                                onClick = {
                                    selectedWallpaper = wp
                                    showWallpaperDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = WhatsAppLightGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = wp, color = WhatsAppTextLight, fontSize = 15.sp)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Font Size Dialog
    if (showFontDialog) {
        val fontSizes = listOf("Small", "Medium", "Large")
        AlertDialog(
            onDismissRequest = { showFontDialog = false },
            containerColor = WhatsAppDarkCard,
            title = { Text("Font Size", color = WhatsAppTextLight, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    fontSizes.forEach { f ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedFontSize = f
                                    showFontDialog = false
                                    Toast.makeText(context, "Font size set to $f", Toast.LENGTH_SHORT).show()
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedFontSize == f,
                                onClick = {
                                    selectedFontSize = f
                                    showFontDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = WhatsAppLightGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = f, color = WhatsAppTextLight, fontSize = 15.sp)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
private fun ChatSettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
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
                fontSize = 12.sp
            )
        }
    }
}
