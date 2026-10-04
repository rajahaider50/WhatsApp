package com.whatsapp.android.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whatsapp.android.ui.theme.*

@Composable
fun WelcomeAuthScreen(
    onCreateAccountClick: () -> Unit,
    onLoginAccountClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WhatsAppDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Branding
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(WhatsAppDarkCard),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubble,
                    contentDescription = "WhatsApp Logo",
                    tint = WhatsAppLightGreen,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Welcome to WhatsApp",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = WhatsAppTextLight
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Simple, secure, and reliable messaging and calling with HD voice notes and 3-factor recovery protection.",
                fontSize = 14.sp,
                color = WhatsAppTextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        }

        // Two Action Buttons ONLY as requested
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Create Account Button
            Button(
                onClick = onCreateAccountClick,
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppLightGreen),
                shape = RoundedCornerShape(26.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = "Create Account",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Login Account Button
            OutlinedButton(
                onClick = onLoginAccountClick,
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = WhatsAppLightGreen
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(WhatsAppLightGreen)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = "Login Account",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = WhatsAppLightGreen
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "End-to-End Encrypted Cloudinary & Firebase Platform",
                fontSize = 11.sp,
                color = WhatsAppTextMuted
            )
        }
    }
}
