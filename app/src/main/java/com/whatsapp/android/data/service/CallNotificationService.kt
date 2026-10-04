package com.whatsapp.android.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.whatsapp.android.R
import com.whatsapp.android.ui.screens.call.IncomingCallActivity

class CallNotificationService : Service() {

    companion object {
        const val CHANNEL_ID = "whatsapp_call_channel"
        const val NOTIFICATION_ID = 9999
        const val ACTION_INCOMING_CALL = "action_incoming_call"
        const val ACTION_STOP_CALL = "action_stop_call"
        const val EXTRA_CALL_ID = "extra_call_id"
        const val EXTRA_CALLER_NAME = "extra_caller_name"
        const val EXTRA_CALL_TYPE = "extra_call_type"
        const val EXTRA_CALLER_AVATAR = "extra_caller_avatar"

        fun startIncomingCall(
            context: Context,
            callId: String,
            callerName: String,
            callType: String,
            callerAvatar: String?
        ) {
            val intent = Intent(context, CallNotificationService::class.java).apply {
                action = ACTION_INCOMING_CALL
                putExtra(EXTRA_CALL_ID, callId)
                putExtra(EXTRA_CALLER_NAME, callerName)
                putExtra(EXTRA_CALL_TYPE, callType)
                putExtra(EXTRA_CALLER_AVATAR, callerAvatar)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopCall(context: Context) {
            val intent = Intent(context, CallNotificationService::class.java).apply {
                action = ACTION_STOP_CALL
            }
            context.startService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_INCOMING_CALL -> {
                val callId = intent.getStringExtra(EXTRA_CALL_ID) ?: ""
                val callerName = intent.getStringExtra(EXTRA_CALLER_NAME) ?: "WhatsApp Caller"
                val callType = intent.getStringExtra(EXTRA_CALL_TYPE) ?: "AUDIO"
                val callerAvatar = intent.getStringExtra(EXTRA_CALLER_AVATAR)
                showIncomingCallNotification(callId, callerName, callType, callerAvatar)
            }
            ACTION_STOP_CALL -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun showIncomingCallNotification(
        callId: String,
        callerName: String,
        callType: String,
        callerAvatar: String?
    ) {
        val fullScreenIntent = Intent(this, IncomingCallActivity::class.java).apply {
            putExtra(EXTRA_CALL_ID, callId)
            putExtra(EXTRA_CALLER_NAME, callerName)
            putExtra(EXTRA_CALL_TYPE, callType)
            putExtra(EXTRA_CALLER_AVATAR, callerAvatar)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            callId.hashCode(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(callerName)
            .setContentText("Incoming WhatsApp ${if (callType == "VIDEO") "Video" else "Audio"} Call")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(false)
            .setOngoing(true)
            .setFullScreenIntent(pendingIntent, true)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        // Directly launch the fullscreen incoming call activity as requested by user
        startActivity(fullScreenIntent)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                "WhatsApp Incoming Calls",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming WhatsApp audio and video calls"
                setSound(ringtoneUri, audioAttributes)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 1000, 1000, 1000, 1000)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
