package com.whatsapp.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.whatsapp.android.config.FirebaseConfig

class WhatsAppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        try {
            FirebaseDatabase.getInstance(FirebaseConfig.DATABASE_URL).setPersistenceEnabled(true)
        } catch (ignored: Exception) {
            // Persistence already enabled or initialized
        }
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            val msgChannel = NotificationChannel(
                "whatsapp_messages",
                "WhatsApp Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Chat messages and notifications"
                enableVibration(true)
            }

            manager.createNotificationChannel(msgChannel)
        }
    }
}
