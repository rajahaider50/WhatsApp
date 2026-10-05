package com.whatsapp.android.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whatsapp.android.ui.theme.*

@Composable
fun AppInfoScreen(
    onBack: () -> Unit,
    onOpenDeveloperProfile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WhatsAppDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
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
                text = "App Info",
                color = WhatsAppTextLight,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(WhatsAppLightGreen),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = "WhatsApp",
                    tint = Color.Black,
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "WhatsApp Native Android",
                color = WhatsAppTextLight,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Version 1.0.0 (Release Build 2026)",
                color = WhatsAppTextMuted,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Developer Banner Card
            Card(
                onClick = onOpenDeveloperProfile,
                colors = CardDefaults.cardColors(containerColor = WhatsAppDarkCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, WhatsAppLightGreen.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(WhatsAppLightGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = WhatsAppLightGreen,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Lead Developer",
                            color = WhatsAppTextMuted,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Raja Haider Ali",
                            color = WhatsAppTextLight,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Rawalpindi Islamabad · 03495031007",
                            color = WhatsAppLightGreen,
                            fontSize = 12.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "View Profile",
                        tint = WhatsAppTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Features Checklist Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(WhatsAppDarkCard)
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "Implemented Technologies & Features",
                        color = WhatsAppTextLight,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FeatureCheckRow("Jetpack Compose with Material 3 Dark Theme")
                    FeatureCheckRow("Firebase Realtime Database & Firebase Auth")
                    FeatureCheckRow("3-Option Account Recovery (8-Digit, 13-Digit CNIC, Phone)")
                    FeatureCheckRow("Self-Chat (You - Message yourself) & Self-Call")
                    FeatureCheckRow("Cloudinary Unsigned Media Storage Integration")
                    FeatureCheckRow("Voice Recording Studio with Cropping & Preview")
                    FeatureCheckRow("Double Blue Ticks (Read Receipts)")
                    FeatureCheckRow("One-Time Media & Starred Messages")
                }
            }
        }
    }
}

@Composable
private fun FeatureCheckRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = WhatsAppLightGreen,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            color = WhatsAppTextMuted,
            fontSize = 12.sp
        )
    }
}
