package com.whatsapp.android.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.whatsapp.android.config.CloudinaryConfig
import com.whatsapp.android.data.models.User
import com.whatsapp.android.data.repository.AuthRepository
import com.whatsapp.android.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun EditMyProfileScreen(
    currentUser: User,
    authRepo: AuthRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var displayName by remember { mutableStateOf(currentUser.displayName) }
    var bio by remember { mutableStateOf(currentUser.bio) }
    var avatarUrl by remember { mutableStateOf(currentUser.avatarUrl) }
    var isUploading by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploading = true
            coroutineScope.launch {
                try {
                    val stream = context.contentResolver.openInputStream(uri)
                    val file = File(context.cacheDir, "my_avatar_${System.currentTimeMillis()}.jpg")
                    val out = FileOutputStream(file)
                    stream?.copyTo(out)
                    stream?.close()
                    out.close()

                    val res = CloudinaryConfig.uploadFile(file, folder = "avatars")
                    res.onSuccess { url ->
                        avatarUrl = url
                    }
                } catch (ignored: Exception) {}
                finally {
                    isUploading = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WhatsAppDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                text = "Edit Profile",
                color = WhatsAppTextLight,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Avatar Picker
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(130.dp)
                .clip(CircleShape)
                .background(WhatsAppDarkCard)
                .clickable { imagePicker.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (!avatarUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "My Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = WhatsAppTextMuted,
                    modifier = Modifier.size(64.dp)
                )
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(WhatsAppLightGreen)
                    .align(Alignment.BottomEnd),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Edit photo",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (isUploading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = WhatsAppLightGreen, modifier = Modifier.size(32.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Username (Immutable / unique)
        OutlinedTextField(
            value = currentUser.getCleanUsername(),
            onValueChange = {},
            readOnly = true,
            label = { Text("Username") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = WhatsAppDarkInput,
                unfocusedBorderColor = WhatsAppDarkInput,
                focusedTextColor = WhatsAppLightGreen,
                unfocusedTextColor = WhatsAppLightGreen
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display Name
        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Display Name") },
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

        // Bio / About
        OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text("About") },
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

        if (message != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message ?: "",
                color = WhatsAppLightGreen,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                isSaving = true
                coroutineScope.launch {
                    val res = authRepo.updateUsernameAndAvatar(
                        uid = currentUser.uid,
                        rawUsername = currentUser.username,
                        avatarUrl = avatarUrl,
                        displayName = displayName
                    )
                    isSaving = false
                    res.onSuccess {
                        message = "Profile successfully updated!"
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppLightGreen),
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (isSaving) {
                CircularProgressIndicator(color = Color.Black, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
            } else {
                Text("Save Profile", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
