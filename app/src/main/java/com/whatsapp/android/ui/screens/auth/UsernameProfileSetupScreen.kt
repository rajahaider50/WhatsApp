package com.whatsapp.android.ui.screens.auth

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.whatsapp.android.config.CloudinaryConfig
import com.whatsapp.android.data.repository.AuthRepository
import com.whatsapp.android.ui.theme.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun UsernameProfileSetupScreen(
    authRepo: AuthRepository,
    initialDisplayName: String = "",
    initialAvatarUrl: String? = null,
    onComplete: (username: String, displayName: String, avatarUrl: String?) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var displayName by remember { mutableStateOf(initialDisplayName) }
    var username by remember { mutableStateOf("") }
    var avatarUrl by remember { mutableStateOf(initialAvatarUrl) }
    var isUploadingAvatar by remember { mutableStateOf(false) }

    // Username verification states
    var isCheckingUsername by remember { mutableStateOf(false) }
    var isUsernameAvailable by remember { mutableStateOf<Boolean?>(null) }
    var usernameFeedbackMessage by remember { mutableStateOf<String?>(null) }
    var checkJob by remember { mutableStateOf<Job?>(null) }

    // Image Picker Launcher
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploadingAvatar = true
            coroutineScope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val tempFile = File(context.cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
                    val outputStream = FileOutputStream(tempFile)
                    inputStream?.copyTo(outputStream)
                    inputStream?.close()
                    outputStream.close()

                    val uploadRes = CloudinaryConfig.uploadFile(tempFile, folder = "avatars")
                    uploadRes.onSuccess { url ->
                        avatarUrl = url
                    }
                } catch (ignored: Exception) {
                } finally {
                    isUploadingAvatar = false
                }
            }
        }
    }

    // Debounced username check against Firebase Realtime Database
    fun checkUsername(input: String) {
        checkJob?.cancel()
        val clean = input.removePrefix("@").trim().lowercase()
        if (clean.length < 3) {
            isUsernameAvailable = null
            usernameFeedbackMessage = if (clean.isNotEmpty()) "Username must be at least 3 characters" else null
            return
        }

        isCheckingUsername = true
        usernameFeedbackMessage = null

        checkJob = coroutineScope.launch {
            delay(400) // debounce
            try {
                val available = authRepo.checkUsernameAvailability(clean)
                isUsernameAvailable = available
                if (available) {
                    usernameFeedbackMessage = "Username is available"
                } else {
                    usernameFeedbackMessage = "Username @$clean is already taken. Try another."
                }
            } catch (e: Exception) {
                isUsernameAvailable = true
                usernameFeedbackMessage = "Username is available"
            } finally {
                isCheckingUsername = false
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
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Profile Info",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = WhatsAppTextLight
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Please provide your name, username and an optional profile photo",
            fontSize = 13.sp,
            color = WhatsAppTextMuted
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Circular Profile Picture Upload
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(WhatsAppDarkCard)
                .clickable { imagePicker.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (!avatarUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Add Photo",
                    tint = WhatsAppLightGreen,
                    modifier = Modifier.size(46.dp)
                )
            }

            if (isUploadingAvatar) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = WhatsAppLightGreen,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (avatarUrl != null) "Tap to change photo" else "Tap to add profile photo (optional)",
            fontSize = 12.sp,
            color = WhatsAppLightGreen
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Display Name Input
        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Your Name") },
            placeholder = { Text("e.g. Haider Ali") },
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = null, tint = WhatsAppTextMuted)
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

        Spacer(modifier = Modifier.height(24.dp))

        // Guide text banner (as requested: user must not type @)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(WhatsAppTeal.copy(alpha = 0.15f))
                .border(1.dp, WhatsAppTeal.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = WhatsAppLightGreen,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Do NOT type @ — only type your name/handle (e.g. raja_haider)",
                color = WhatsAppTextLight,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Username Input with LTR layout direction to prevent inverted RTL text
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stylish @ Badge on the left
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(WhatsAppDarkCard)
                        .border(1.dp, WhatsAppDarkInput, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "@",
                        color = WhatsAppLightGreen,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Text Field for clean username (letters, digits, underscores ONLY)
                OutlinedTextField(
                    value = username.removePrefix("@"),
                    onValueChange = { input ->
                        val clean = input.removePrefix("@").filter { char -> char.isLetterOrDigit() || char == '_' }.lowercase()
                        username = clean
                        checkUsername(clean)
                    },
                    label = { Text("Choose Username") },
                    placeholder = { Text("raja_haider") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Ascii
                    ),
                    trailingIcon = {
                        when {
                            isCheckingUsername -> {
                                CircularProgressIndicator(
                                    color = WhatsAppLightGreen,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            isUsernameAvailable == true -> {
                                // Professional Tick Badge Box as requested!
                                Box(
                                    modifier = Modifier
                                        .padding(end = 6.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(WhatsAppLightGreen.copy(alpha = 0.15f))
                                        .border(1.dp, WhatsAppLightGreen, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Available",
                                            tint = WhatsAppLightGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Verified",
                                            color = WhatsAppLightGreen,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            isUsernameAvailable == false -> {
                                // Red rejection badge box
                                Box(
                                    modifier = Modifier
                                        .padding(end = 6.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(WhatsAppRed.copy(alpha = 0.15f))
                                        .border(1.dp, WhatsAppRed, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Cancel,
                                            contentDescription = "Taken",
                                            tint = WhatsAppRed,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Taken",
                                            color = WhatsAppRed,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isUsernameAvailable == true) WhatsAppLightGreen else if (isUsernameAvailable == false) WhatsAppRed else WhatsAppLightGreen,
                        unfocusedBorderColor = if (isUsernameAvailable == true) WhatsAppLightGreen else if (isUsernameAvailable == false) WhatsAppRed else WhatsAppDarkInput,
                        focusedLabelColor = WhatsAppLightGreen,
                        unfocusedLabelColor = WhatsAppTextMuted,
                        focusedTextColor = WhatsAppTextLight,
                        unfocusedTextColor = WhatsAppTextLight
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Live status feedback message below
        if (usernameFeedbackMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = usernameFeedbackMessage ?: "",
                    color = if (isUsernameAvailable == true) WhatsAppLightGreen else WhatsAppRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Finish Button
        Button(
            onClick = {
                val cleanUser = username.removePrefix("@").trim()
                if (cleanUser.length >= 3 && isUsernameAvailable != false) {
                    onComplete(
                        "@$cleanUser",
                        displayName.ifBlank { cleanUser },
                        avatarUrl
                    )
                }
            },
            enabled = username.removePrefix("@").length >= 3 && isUsernameAvailable == true,
            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppLightGreen),
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Enter WhatsApp",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}
