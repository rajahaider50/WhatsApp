package com.whatsapp.android.ui.screens.settings

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.whatsapp.android.data.models.RecoveryConfig
import com.whatsapp.android.data.models.RecoveryMethod
import com.whatsapp.android.data.repository.AuthRepository
import com.whatsapp.android.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun RecoverySetupScreen(
    authRepo: AuthRepository,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedMethod by remember { mutableStateOf(RecoveryMethod.BACKUP_CODE) }
    var secretValue by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var existingConfig by remember { mutableStateOf<RecoveryConfig?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val uid = authRepo.getCurrentUid()
        if (uid != null) {
            val cfgRes = authRepo.getRecoveryConfig(uid)
            val cfg = cfgRes.getOrNull()
            if (cfg != null && cfg.isConfigured) {
                existingConfig = cfg
                when (cfg.method) {
                    RecoveryMethod.BACKUP_CODE.name -> selectedMethod = RecoveryMethod.BACKUP_CODE
                    RecoveryMethod.CNIC.name -> selectedMethod = RecoveryMethod.CNIC
                    RecoveryMethod.PHONE.name -> selectedMethod = RecoveryMethod.PHONE
                }
                secretValue = cfg.secretValue
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
                text = "Password Recovery Setup",
                color = WhatsAppTextLight,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Info Banner
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = WhatsAppDarkSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = WhatsAppLightGreen,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "3-Option Recovery Protection",
                        color = WhatsAppTextLight,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Choose ONE method to secure your account. When resetting password, providing this verified information will grant instant access.",
                        color = WhatsAppTextMuted,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        if (existingConfig != null && existingConfig!!.isConfigured) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(WhatsAppTeal.copy(alpha = 0.2f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = WhatsAppLightGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Active Recovery: ${existingConfig!!.method} is connected and verified.",
                    color = WhatsAppLightGreen,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Select your preferred recovery method:",
            color = WhatsAppTextLight,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Option 1: 8-digit Forget Code
        MethodCard(
            method = RecoveryMethod.BACKUP_CODE,
            icon = Icons.Default.Pin,
            isSelected = selectedMethod == RecoveryMethod.BACKUP_CODE,
            onClick = {
                selectedMethod = RecoveryMethod.BACKUP_CODE
                message = null
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Option 2: 13-digit CNIC Code
        MethodCard(
            method = RecoveryMethod.CNIC,
            icon = Icons.Default.Badge,
            isSelected = selectedMethod == RecoveryMethod.CNIC,
            onClick = {
                selectedMethod = RecoveryMethod.CNIC
                message = null
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Option 3: Restore Phone Number
        MethodCard(
            method = RecoveryMethod.PHONE,
            icon = Icons.Default.Phone,
            isSelected = selectedMethod == RecoveryMethod.PHONE,
            onClick = {
                selectedMethod = RecoveryMethod.PHONE
                message = null
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        val labelText = when (selectedMethod) {
            RecoveryMethod.BACKUP_CODE -> "Enter 8-Digit Secret Code"
            RecoveryMethod.CNIC -> "Enter 13-Digit CNIC Number"
            RecoveryMethod.PHONE -> "Enter Restore Phone Number"
        }

        val placeholderText = when (selectedMethod) {
            RecoveryMethod.BACKUP_CODE -> "e.g. 84920158"
            RecoveryMethod.CNIC -> "e.g. 4210112345671"
            RecoveryMethod.PHONE -> "e.g. 03001234567"
        }

        OutlinedTextField(
            value = secretValue,
            onValueChange = {
                secretValue = it
                message = null
            },
            label = { Text(labelText) },
            placeholder = { Text(placeholderText) },
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

        if (message != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message ?: "",
                color = if (isSuccess) WhatsAppLightGreen else WhatsAppRed,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = {
                val digitsOnly = secretValue.filter { it.isDigit() }
                when (selectedMethod) {
                    RecoveryMethod.BACKUP_CODE -> {
                        if (digitsOnly.length != 8) {
                            message = "Code must be exactly 8 numeric digits"
                            isSuccess = false
                            return@Button
                        }
                    }
                    RecoveryMethod.CNIC -> {
                        if (digitsOnly.length != 13) {
                            message = "CNIC must be exactly 13 numeric digits (found ${digitsOnly.length})"
                            isSuccess = false
                            return@Button
                        }
                    }
                    RecoveryMethod.PHONE -> {
                        if (digitsOnly.length < 10) {
                            message = "Please enter a valid phone number (at least 10 digits)"
                            isSuccess = false
                            return@Button
                        }
                    }
                }

                isSaving = true
                message = null

                coroutineScope.launch {
                    val res = authRepo.saveRecoveryOption(selectedMethod.name, digitsOnly)
                    isSaving = false
                    res.onSuccess {
                        isSuccess = true
                        message = "Recovery method verified and successfully connected to Firebase!"
                        existingConfig = RecoveryConfig(
                            method = selectedMethod.name,
                            secretValue = digitsOnly,
                            isConfigured = true
                        )
                    }.onFailure { e ->
                        isSuccess = false
                        message = e.message ?: "Failed to save recovery configuration"
                    }
                }
            },
            enabled = !isSaving && secretValue.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppLightGreen),
            shape = RoundedCornerShape(26.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (isSaving) {
                CircularProgressIndicator(color = Color.Black, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = "Verify & Save Recovery Option",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun MethodCard(
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
