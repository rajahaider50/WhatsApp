package com.whatsapp.android.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whatsapp.android.data.models.RecoveryMethod
import com.whatsapp.android.data.repository.AuthRepository
import com.whatsapp.android.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ForgetPasswordRecoveryScreen(
    authRepo: AuthRepository,
    onBack: () -> Unit,
    onPasswordResetSuccess: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    var accountQuery by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf<RecoveryMethod?>(null) }
    var secretInput by remember { mutableStateOf("") }

    // After verification state
    var isVerified by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    var confirmNewPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

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
                    imageVector = Icons.Default.LockReset,
                    contentDescription = null,
                    tint = WhatsAppLightGreen,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Account Recovery",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = WhatsAppTextLight
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (!isVerified) "Verify your configured recovery method" else "Create a new password",
                fontSize = 13.sp,
                color = WhatsAppTextMuted
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        if (!isVerified) {
            // Step 1: Email or Username
            OutlinedTextField(
                value = accountQuery,
                onValueChange = {
                    accountQuery = it
                    errorMessage = null
                },
                label = { Text("Email or Username (@xxxxxx)") },
                placeholder = { Text("e.g. name@example.com or @haider") },
                leadingIcon = {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = WhatsAppTextMuted)
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

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Select the recovery method you set up in Settings:",
                color = WhatsAppTextLight,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3 Recovery Options
            RecoveryOptionCard(
                method = RecoveryMethod.BACKUP_CODE,
                icon = Icons.Default.Pin,
                isSelected = selectedMethod == RecoveryMethod.BACKUP_CODE,
                onClick = {
                    selectedMethod = RecoveryMethod.BACKUP_CODE
                    errorMessage = null
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            RecoveryOptionCard(
                method = RecoveryMethod.CNIC,
                icon = Icons.Default.Badge,
                isSelected = selectedMethod == RecoveryMethod.CNIC,
                onClick = {
                    selectedMethod = RecoveryMethod.CNIC
                    errorMessage = null
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            RecoveryOptionCard(
                method = RecoveryMethod.PHONE,
                icon = Icons.Default.Phone,
                isSelected = selectedMethod == RecoveryMethod.PHONE,
                onClick = {
                    selectedMethod = RecoveryMethod.PHONE
                    errorMessage = null
                }
            )

            // Secret Value Input based on selected method
            if (selectedMethod != null) {
                Spacer(modifier = Modifier.height(20.dp))

                val placeholder = when (selectedMethod!!) {
                    RecoveryMethod.BACKUP_CODE -> "Enter 8-digit secret code (e.g. 12345678)"
                    RecoveryMethod.CNIC -> "Enter 13-digit CNIC (e.g. 4210112345671)"
                    RecoveryMethod.PHONE -> "Enter restore phone number (e.g. 03001234567)"
                }

                OutlinedTextField(
                    value = secretInput,
                    onValueChange = {
                        secretInput = it
                        errorMessage = null
                    },
                    label = { Text(selectedMethod!!.title) },
                    placeholder = { Text(placeholder) },
                    leadingIcon = {
                        Icon(Icons.Default.Key, contentDescription = null, tint = WhatsAppTextMuted)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage ?: "",
                    color = WhatsAppRed,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Verify Button
            Button(
                onClick = {
                    if (accountQuery.isBlank()) {
                        errorMessage = "Please enter your email or username"
                        return@Button
                    }
                    if (selectedMethod == null) {
                        errorMessage = "Please choose the recovery method you set up"
                        return@Button
                    }
                    if (secretInput.isBlank()) {
                        errorMessage = "Please enter your recovery code/number"
                        return@Button
                    }

                    isProcessing = true
                    errorMessage = null

                    coroutineScope.launch {
                        try {
                            val cleanQuery = accountQuery.trim()
                            val cfgRes = authRepo.getRecoveryConfig(cleanQuery)
                            val cfg = cfgRes.getOrNull()

                            if (cfg == null || !cfg.isConfigured) {
                                errorMessage = "No recovery setup found for account '$cleanQuery'. Please ensure you entered the exact username or email you registered with."
                            } else if (cfg.method != selectedMethod!!.name) {
                                val methodTitle = when (cfg.method) {
                                    RecoveryMethod.BACKUP_CODE.name -> "8-Digit Forget Code"
                                    RecoveryMethod.CNIC.name -> "13-Digit CNIC Code"
                                    RecoveryMethod.PHONE.name -> "Restore Phone Number"
                                    else -> cfg.method
                                }
                                errorMessage = "Incorrect recovery method selected! This account configured: $methodTitle."
                            } else {
                                val savedDigits = cfg.secretValue.filter { it.isDigit() }
                                val enteredDigits = secretInput.filter { it.isDigit() }
                                val isMatch = if (savedDigits.isNotEmpty() && enteredDigits.isNotEmpty()) {
                                    savedDigits == enteredDigits
                                } else {
                                    cfg.secretValue.trim().equals(secretInput.trim(), ignoreCase = true)
                                }

                                if (!isMatch) {
                                    errorMessage = "The entered recovery code/number does not match."
                                } else {
                                    // Match verified!
                                    isVerified = true
                                }
                            }
                        } catch (e: Exception) {
                            errorMessage = e.message ?: "Verification failed"
                        } finally {
                            isProcessing = false
                        }
                    }
                },
                enabled = !isProcessing && accountQuery.isNotBlank() && selectedMethod != null && secretInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppLightGreen),
                shape = RoundedCornerShape(26.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.Black, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                } else {
                    Text("Verify & Continue", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        } else {
            // STEP 2: Password Reset UI
            Text(
                text = "Recovery Verified! Set New Password",
                color = WhatsAppLightGreen,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = {
                    newPassword = it
                    errorMessage = null
                },
                label = { Text("New Password") },
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

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmNewPassword,
                onValueChange = {
                    confirmNewPassword = it
                    errorMessage = null
                },
                label = { Text("Confirm New Password") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = WhatsAppTextMuted)
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

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage ?: "",
                    color = WhatsAppRed,
                    fontSize = 13.sp
                )
            }

            if (successMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = successMessage ?: "",
                    color = WhatsAppLightGreen,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    if (newPassword.length < 6) {
                        errorMessage = "Password must be at least 6 characters"
                        return@Button
                    }
                    if (newPassword != confirmNewPassword) {
                        errorMessage = "Passwords do not match"
                        return@Button
                    }

                    isProcessing = true
                    errorMessage = null

                    coroutineScope.launch {
                        val res = authRepo.resetPasswordWithRecovery(
                            emailOrUsername = accountQuery,
                            selectedMethod = selectedMethod!!.name,
                            enteredSecret = secretInput,
                            newPassword = newPassword
                        )
                        isProcessing = false
                        res.onSuccess {
                            successMessage = "Password successfully updated on Firebase! Please log in."
                            kotlinx.coroutines.delay(1200)
                            onPasswordResetSuccess()
                        }.onFailure { e ->
                            errorMessage = e.message ?: "Failed to reset password"
                        }
                    }
                },
                enabled = !isProcessing && newPassword.isNotBlank() && confirmNewPassword.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppLightGreen),
                shape = RoundedCornerShape(26.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(color = Color.Black, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save & Update Password", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun RecoveryOptionCard(
    method: RecoveryMethod,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) WhatsAppLightGreen else WhatsAppDarkInput
    val bgColor = if (isSelected) WhatsAppTeal.copy(alpha = 0.15f) else WhatsAppDarkSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isSelected) WhatsAppLightGreen else WhatsAppDarkCard),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) Color.Black else WhatsAppTextMuted,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = method.title,
                color = WhatsAppTextLight,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = method.description,
                color = WhatsAppTextMuted,
                fontSize = 12.sp
            )
        }

        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = WhatsAppLightGreen,
                unselectedColor = WhatsAppTextMuted
            )
        )
    }
}
