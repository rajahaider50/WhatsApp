package com.whatsapp.android.ui.screens.call

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.whatsapp.android.MainActivity
import com.whatsapp.android.data.repository.CallRepository
import com.whatsapp.android.data.service.CallNotificationService
import com.whatsapp.android.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IncomingCallActivity : ComponentActivity() {

    private val callRepo = CallRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Wake screen and show over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        val callId = intent.getStringExtra(CallNotificationService.EXTRA_CALL_ID) ?: ""
        val callerName = intent.getStringExtra(CallNotificationService.EXTRA_CALLER_NAME) ?: "WhatsApp Caller"
        val callType = intent.getStringExtra(CallNotificationService.EXTRA_CALL_TYPE) ?: "AUDIO"
        val callerAvatar = intent.getStringExtra(CallNotificationService.EXTRA_CALLER_AVATAR)

        setContent {
            WhatsAppTheme(darkTheme = true) {
                IncomingCallContent(
                    callerName = callerName,
                    callType = callType,
                    callerAvatar = callerAvatar,
                    onAccept = {
                        CallNotificationService.stopCall(this)
                        CoroutineScope(Dispatchers.Main).launch {
                            callRepo.acceptCall(callId)
                            val mainIntent = Intent(this@IncomingCallActivity, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                putExtra("active_call_id", callId)
                            }
                            startActivity(mainIntent)
                            finish()
                        }
                    },
                    onDecline = {
                        CallNotificationService.stopCall(this)
                        CoroutineScope(Dispatchers.Main).launch {
                            callRepo.rejectCall(callId)
                            finish()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun IncomingCallContent(
    callerName: String,
    callType: String,
    callerAvatar: String?,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WhatsAppDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(32.dp)
    ) {
        // Caller Info Header
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .background(WhatsAppDarkCard),
                contentAlignment = Alignment.Center
            ) {
                if (!callerAvatar.isNullOrEmpty()) {
                    AsyncImage(
                        model = callerAvatar,
                        contentDescription = callerName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = WhatsAppTextMuted,
                        modifier = Modifier.size(70.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = callerName,
                color = WhatsAppTextLight,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "WhatsApp ${if (callType == "VIDEO") "Video" else "Audio"} Call",
                color = WhatsAppLightGreen,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Ringing...",
                color = WhatsAppTextMuted,
                fontSize = 13.sp
            )
        }

        // Bottom Accept / Decline Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 30.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Decline (Red Button)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onDecline,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(WhatsAppRed)
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "Decline Call",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Decline", color = WhatsAppTextLight, fontSize = 13.sp)
            }

            // Accept (Green Button)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onAccept,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(WhatsAppLightGreen)
                ) {
                    Icon(
                        imageVector = if (callType == "VIDEO") Icons.Default.Videocam else Icons.Default.Call,
                        contentDescription = "Accept Call",
                        tint = Color.Black,
                        modifier = Modifier.size(34.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("Accept", color = WhatsAppTextLight, fontSize = 13.sp)
            }
        }
    }
}
