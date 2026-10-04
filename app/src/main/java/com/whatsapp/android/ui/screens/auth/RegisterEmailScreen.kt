package com.whatsapp.android.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whatsapp.android.ui.theme.*

@Composable
fun RegisterEmailScreen(
    onBack: () -> Unit,
    onContinueEmail: (email: String) -> Unit,
    onSignUpWithGoogle: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WhatsAppDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = WhatsAppTextLight
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(WhatsAppDarkCard),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubble,
                    contentDescription = null,
                    tint = WhatsAppLightGreen,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Enter Your Email",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = WhatsAppTextLight
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "We will link your account and messages to this email",
                fontSize = 13.sp,
                color = WhatsAppTextMuted
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Enter Email Input
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = null
            },
            label = { Text("Email Address") },
            placeholder = { Text("name@example.com") },
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = null, tint = WhatsAppTextMuted)
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

        if (emailError != null) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = emailError ?: "",
                color = WhatsAppRed,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Continue Button
        Button(
            onClick = {
                if (!email.contains("@") || !email.contains(".")) {
                    emailError = "Please enter a valid email address"
                } else {
                    onContinueEmail(email.trim())
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppLightGreen),
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Continue",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = WhatsAppDarkInput)
            Text(
                text = "OR",
                color = WhatsAppTextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = WhatsAppDarkInput)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sign Up with Google
        OutlinedButton(
            onClick = onSignUpWithGoogle,
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = WhatsAppDarkSurface),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(WhatsAppDarkInput)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Google",
                tint = WhatsAppLightGreen,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Sign up with Google",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = WhatsAppTextLight
            )
        }
    }
}
