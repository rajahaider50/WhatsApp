package com.whatsapp.android.ui.screens.status

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
import com.whatsapp.android.data.models.StatusPrivacy
import com.whatsapp.android.data.models.User
import com.whatsapp.android.data.repository.StatusRepository
import com.whatsapp.android.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun AddStatusScreen(
    currentUser: User,
    statusRepo: StatusRepository,
    onBack: () -> Unit,
    onStatusPosted: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var captionText by remember { mutableStateOf("") }
    var selectedPrivacy by remember { mutableStateOf(StatusPrivacy.ALL) }
    var isUploading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
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
                text = "Add Status Update",
                color = WhatsAppTextLight,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Media Selector Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(WhatsAppDarkCard)
                .clickable { imagePicker.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Selected Status",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        tint = WhatsAppLightGreen,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Tap to choose photo for status",
                        color = WhatsAppTextLight,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Caption Input
        OutlinedTextField(
            value = captionText,
            onValueChange = { captionText = it },
            label = { Text("Add a caption...") },
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

        Text(
            text = "Status Privacy - Who can see this status:",
            color = WhatsAppTextLight,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Privacy Options
        PrivacyOptionRow(
            title = "My Contacts",
            subtitle = "Visible to all contacts",
            isSelected = selectedPrivacy == StatusPrivacy.ALL,
            onClick = { selectedPrivacy = StatusPrivacy.ALL }
        )

        Spacer(modifier = Modifier.height(8.dp))

        PrivacyOptionRow(
            title = "My Contacts Except...",
            subtitle = "Exclude selected contacts",
            isSelected = selectedPrivacy == StatusPrivacy.CONTACTS,
            onClick = { selectedPrivacy = StatusPrivacy.CONTACTS }
        )

        Spacer(modifier = Modifier.height(8.dp))

        PrivacyOptionRow(
            title = "Only Share With / Private",
            subtitle = "Private status for specific people only",
            isSelected = selectedPrivacy == StatusPrivacy.PRIVATE,
            onClick = { selectedPrivacy = StatusPrivacy.PRIVATE }
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = errorMessage ?: "",
                color = WhatsAppRed,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Share Button
        Button(
            onClick = {
                if (selectedImageUri == null) {
                    errorMessage = "Please select a photo first"
                    return@Button
                }

                isUploading = true
                errorMessage = null

                coroutineScope.launch {
                    try {
                        val stream = context.contentResolver.openInputStream(selectedImageUri!!)
                        val tempFile = File(context.cacheDir, "status_${System.currentTimeMillis()}.jpg")
                        val out = FileOutputStream(tempFile)
                        stream?.copyTo(out)
                        stream?.close()
                        out.close()

                        val uploadRes = CloudinaryConfig.uploadFile(tempFile, folder = "statuses")
                        uploadRes.onSuccess { mediaUrl ->
                            statusRepo.postStatus(
                                user = currentUser,
                                mediaUrl = mediaUrl,
                                caption = captionText.trim(),
                                privacy = selectedPrivacy.name
                            )
                            isUploading = false
                            onStatusPosted()
                        }.onFailure { e ->
                            isUploading = false
                            errorMessage = e.message ?: "Failed to upload to Cloudinary"
                        }
                    } catch (e: Exception) {
                        isUploading = false
                        errorMessage = e.message ?: "Error processing media"
                    }
                }
            },
            enabled = !isUploading && selectedImageUri != null,
            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppLightGreen),
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (isUploading) {
                CircularProgressIndicator(color = Color.Black, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = "Share Status",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
private fun PrivacyOptionRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(WhatsAppDarkSurface)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = WhatsAppLightGreen,
                unselectedColor = WhatsAppTextMuted
            )
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = WhatsAppTextLight, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = WhatsAppTextMuted, fontSize = 12.sp)
        }
    }
}
