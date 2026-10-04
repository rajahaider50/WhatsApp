package com.whatsapp.android.ui.screens.permissions

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
fun PermissionsScreen(
    onPermissionsComplete: () -> Unit
) {
    var micGranted by remember { mutableStateOf(false) }
    var cameraGranted by remember { mutableStateOf(false) }
    var notificationGranted by remember { mutableStateOf(false) }
    var storageGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        micGranted = results[Manifest.permission.RECORD_AUDIO] == true
        cameraGranted = results[Manifest.permission.CAMERA] == true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationGranted = results[Manifest.permission.POST_NOTIFICATIONS] == true
        } else {
            notificationGranted = true
        }
        storageGranted = true
        onPermissionsComplete()
    }

    fun requestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WhatsAppDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(WhatsAppDarkCard),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = WhatsAppLightGreen,
                    modifier = Modifier.size(42.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Permissions Required",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = WhatsAppTextLight
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "To send voice notes, make audio/video calls, and share photos with end-to-end security, please grant the following permissions.",
                fontSize = 14.sp,
                color = WhatsAppTextMuted,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Step 1: Microphone
            PermissionStepItem(
                icon = Icons.Default.Mic,
                title = "Microphone Access",
                subtitle = "For recording HD voice notes and clear audio calls"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Step 2: Camera & Photos
            PermissionStepItem(
                icon = Icons.Default.CameraAlt,
                title = "Camera & Media Storage",
                subtitle = "To capture status updates, photos, videos, and video calls"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Step 3: Notifications
            PermissionStepItem(
                icon = Icons.Default.Notifications,
                title = "Notifications & Alerts",
                subtitle = "To receive messages and incoming call popups instantly"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Step 4: Background Service
            PermissionStepItem(
                icon = Icons.Default.Sync,
                title = "Background Sync",
                subtitle = "Ensures calls and messages reach you even when phone is locked"
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
        ) {
            Button(
                onClick = { requestPermissions() },
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppLightGreen),
                shape = RoundedCornerShape(26.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "Continue & Allow All",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = onPermissionsComplete,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Not Now / Skip", color = WhatsAppTextMuted, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun PermissionStepItem(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(WhatsAppDarkSurface)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(WhatsAppTeal.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = WhatsAppLightGreen,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = WhatsAppTextLight,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = WhatsAppTextMuted,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }

        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = WhatsAppTeal,
            modifier = Modifier.size(20.dp)
        )
    }
}
