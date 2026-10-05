package com.whatsapp.android.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whatsapp.android.ui.theme.*

@Composable
fun DeveloperProfileScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val devName = "Raja Haider Ali"
    val devPhone = "03495031007"
    val devWhatsApp = "03495031007"
    val devTikTok = "@haideredits463"
    val devLocation = "Rawalpindi Islamabad"

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
                text = "Developer Profile",
                color = WhatsAppTextLight,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Developer Hero Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(WhatsAppDarkCard)
                .border(1.dp, WhatsAppDarkInput, RoundedCornerShape(20.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Large Avatar with Verified Checkmark Badge
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(WhatsAppTeal)
                            .border(3.dp, WhatsAppLightGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(52.dp)
                        )
                    }

                    // Verified Badge
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(WhatsAppLightGreen)
                            .border(2.dp, WhatsAppDarkCard, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Verified Developer",
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = devName,
                    color = WhatsAppTextLight,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = WhatsAppLightGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Lead Android & Full-Stack Developer",
                        color = WhatsAppLightGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = WhatsAppRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = devLocation,
                        color = WhatsAppTextMuted,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Official Contact & Social Details",
            color = WhatsAppTextMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        // Contact detail cards
        DeveloperDetailRow(
            icon = Icons.Default.Person,
            label = "Developer Name",
            value = devName,
            actionLabel = "Copy",
            onAction = {
                clipboardManager.setText(AnnotatedString(devName))
                Toast.makeText(context, "Copied name to clipboard", Toast.LENGTH_SHORT).show()
            }
        )

        DeveloperDetailRow(
            icon = Icons.Default.Phone,
            label = "Phone Number",
            value = devPhone,
            actionLabel = "Call",
            onAction = {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$devPhone"))
                context.startActivity(intent)
            }
        )

        DeveloperDetailRow(
            icon = Icons.Default.Chat,
            label = "WhatsApp Number",
            value = devWhatsApp,
            actionLabel = "Chat",
            onAction = {
                val cleanNumber = devWhatsApp.removePrefix("0")
                val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/92$cleanNumber"))
                try {
                    context.startActivity(waIntent)
                } catch (e: Exception) {
                    clipboardManager.setText(AnnotatedString(devWhatsApp))
                    Toast.makeText(context, "Copied $devWhatsApp", Toast.LENGTH_SHORT).show()
                }
            }
        )

        DeveloperDetailRow(
            icon = Icons.Default.MusicNote,
            label = "TikTok ID",
            value = devTikTok,
            actionLabel = "Open",
            onAction = {
                val cleanHandle = devTikTok.removePrefix("@")
                val tiktokIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tiktok.com/@$cleanHandle"))
                try {
                    context.startActivity(tiktokIntent)
                } catch (e: Exception) {
                    clipboardManager.setText(AnnotatedString(devTikTok))
                    Toast.makeText(context, "Copied $devTikTok", Toast.LENGTH_SHORT).show()
                }
            }
        )

        DeveloperDetailRow(
            icon = Icons.Default.Place,
            label = "Location",
            value = devLocation,
            actionLabel = "Copy",
            onAction = {
                clipboardManager.setText(AnnotatedString(devLocation))
                Toast.makeText(context, "Copied location to clipboard", Toast.LENGTH_SHORT).show()
            }
        )

        Spacer(modifier = Modifier.height(30.dp))

        // WhatsApp App Version Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(WhatsAppDarkCard)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "WhatsApp Native Android Clone",
                    color = WhatsAppTextLight,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Engineered with Jetpack Compose, Kotlin, Firebase & Cloudinary",
                    color = WhatsAppTextMuted,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun DeveloperDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAction() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(WhatsAppDarkCard),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = WhatsAppLightGreen,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = WhatsAppTextMuted,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = WhatsAppTextLight,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Button(
            onClick = onAction,
            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppLightGreen.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(16.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            modifier = Modifier.height(34.dp)
        ) {
            Text(
                text = actionLabel,
                color = WhatsAppLightGreen,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
