package com.whatsapp.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.whatsapp.android.ui.theme.*

@Composable
fun SetPasswordReminderDialog(
    onDismiss: () -> Unit,
    onSavePassword: (String) -> Unit
) {
    var isInputMode by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = WhatsAppDarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = WhatsAppLightGreen,
                    modifier = Modifier.size(44.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isInputMode) "Create Account Password" else "Set Up Password",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = WhatsAppTextLight
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (!isInputMode) {
                    Text(
                        text = "You signed in with Google. It is highly recommended to set a password so you can also log in directly with your email and enable the 3-option account recovery feature.",
                        fontSize = 14.sp,
                        color = WhatsAppTextMuted,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Skip For Now", color = WhatsAppTextMuted)
                        }

                        Button(
                            onClick = { isInputMode = true },
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppLightGreen)
                        ) {
                            Text("Set Password", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Text(
                        text = "Enter a secure password for your WhatsApp account:",
                        fontSize = 13.sp,
                        color = WhatsAppTextMuted
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        label = { Text("New Password") },
                        singleLine = true,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = WhatsAppTextMuted
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            errorMessage = null
                        },
                        label = { Text("Confirm Password") },
                        singleLine = true,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = WhatsAppRed,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { isInputMode = false }) {
                            Text("Back", color = WhatsAppTextMuted)
                        }

                        Button(
                            onClick = {
                                if (password.length < 6) {
                                    errorMessage = "Password must be at least 6 characters"
                                } else if (password != confirmPassword) {
                                    errorMessage = "Passwords do not match"
                                } else {
                                    onSavePassword(password)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppLightGreen)
                        ) {
                            Text("Save Password", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
