package com.whatsapp.android.ui.screens.auth

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whatsapp.android.ui.theme.*

@Composable
fun LoginScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onLoginClick: (email: String, pass: String) -> Unit,
    onForgetPasswordClick: () -> Unit,
    onGoogleSignInClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WhatsAppDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // Back Button
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

        // Header
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
                    contentDescription = "WhatsApp",
                    tint = WhatsAppLightGreen,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Log In to WhatsApp",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = WhatsAppTextLight
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Enter your registered email and password",
                fontSize = 13.sp,
                color = WhatsAppTextMuted
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Enter Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
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

        Spacer(modifier = Modifier.height(16.dp))

        // Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null, tint = WhatsAppTextMuted)
            },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = WhatsAppTextMuted
                    )
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
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

        // Forget Password Option
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Forget Password?",
                color = WhatsAppLightGreen,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable { onForgetPasswordClick() }
                    .padding(vertical = 4.dp)
            )
        }

        if (!errorMessage.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = errorMessage,
                color = WhatsAppRed,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Login Button
        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    onLoginClick(email, password)
                }
            },
            enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppLightGreen),
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.Black,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = "Log In",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Divider
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

        // Sign Up with Google Button
        OutlinedButton(
            onClick = onGoogleSignInClick,
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
                text = "Sign in with Google",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = WhatsAppTextLight
            )
        }
    }
}
