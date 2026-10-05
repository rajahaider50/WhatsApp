package com.whatsapp.android.ui.screens.settings

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whatsapp.android.ui.theme.*

@Composable
fun StorageDataScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var autoDownloadWifi by remember { mutableStateOf(true) }
    var autoDownloadCellular by remember { mutableStateOf(false) }

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
                text = "Storage and Data",
                color = WhatsAppTextLight,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Storage Usage Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(WhatsAppDarkCard)
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(WhatsAppTeal),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Cloudinary Media Cloud",
                            color = WhatsAppTextLight,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Cloud: dtpuqzq0e (Unsigned Preset Active)",
                            color = WhatsAppLightGreen,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = WhatsAppDarkInput)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Network usage", color = WhatsAppTextMuted, fontSize = 13.sp)
                    Text(text = "14.2 MB sent · 28.5 MB received", color = WhatsAppTextLight, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Media Storage (Local Cache)", color = WhatsAppTextMuted, fontSize = 13.sp)
                    Text(text = "4.8 MB", color = WhatsAppLightGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Media Auto-Download",
            color = WhatsAppLightGreen,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "When using mobile data",
                    color = WhatsAppTextLight,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (autoDownloadCellular) "Photos, Voice Notes" else "No media (saves mobile data)",
                    color = WhatsAppTextMuted,
                    fontSize = 12.sp
                )
            }
            Switch(
                checked = autoDownloadCellular,
                onCheckedChange = { autoDownloadCellular = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = WhatsAppLightGreen,
                    uncheckedTrackColor = WhatsAppDarkInput
                )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "When connected on Wi-Fi",
                    color = WhatsAppTextLight,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (autoDownloadWifi) "All media (Photos, Audio, Videos)" else "Manual download",
                    color = WhatsAppTextMuted,
                    fontSize = 12.sp
                )
            }
            Switch(
                checked = autoDownloadWifi,
                onCheckedChange = { autoDownloadWifi = it },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = WhatsAppLightGreen,
                    uncheckedTrackColor = WhatsAppDarkInput
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Clear Cache Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            OutlinedButton(
                onClick = {
                    Toast.makeText(context, "Local media cache cleared!", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = WhatsAppLightGreen),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(WhatsAppLightGreen)
                ),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Clear Local Media Cache", fontWeight = FontWeight.Bold)
            }
        }
    }
}
