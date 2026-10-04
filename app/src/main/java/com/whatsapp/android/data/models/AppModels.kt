package com.whatsapp.android.data.models

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val uid: String = "",
    val email: String = "",
    val username: String = "",
    val displayName: String = "",
    val bio: String = "Hey there! I am using WhatsApp.",
    val avatarUrl: String? = null,
    val isOnline: Boolean = false,
    val lastSeen: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val hasPasswordSet: Boolean = true
) {
    fun getCleanUsername(): String = if (username.startsWith("@")) username else "@$username"
}

enum class RecoveryMethod(val title: String, val digits: Int, val description: String) {
    BACKUP_CODE("8-Digit Forget Code", 8, "Use a secret 8-digit numeric code to recover password"),
    CNIC("13-Digit CNIC Code", 13, "Use your 13-digit National Identity Card number"),
    PHONE("Restore Phone Number", 11, "Use a verified backup phone number")
}

@IgnoreExtraProperties
data class RecoveryConfig(
    val uid: String = "",
    val email: String = "",
    val username: String = "",
    val method: String = "", // "BACKUP_CODE", "CNIC", "PHONE"
    val secretValue: String = "",
    val isConfigured: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

enum class MessageType {
    TEXT, IMAGE, VIDEO, AUDIO, DOCUMENT
}

enum class MessageStatus {
    SENT,       // Single grey tick
    DELIVERED,  // Double grey tick
    SEEN        // Double blue tick
}

@IgnoreExtraProperties
data class ChatMessage(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val receiverId: String = "",
    val text: String = "",
    val type: String = MessageType.TEXT.name,
    val mediaUrl: String? = null,
    val isViewOnce: Boolean = false,
    val isViewed: Boolean = false,
    val isStarred: Boolean = false,
    val isDeletedForEveryone: Boolean = false,
    val deletedForUsers: Map<String, Boolean> = emptyMap(),
    val status: String = MessageStatus.SENT.name,
    val timestamp: Long = System.currentTimeMillis(),
    val audioDurationSeconds: Int = 0,
    val fileName: String? = null
)

@IgnoreExtraProperties
data class ChatPreview(
    val chatId: String = "",
    val peerUid: String = "",
    val peerUsername: String = "",
    val peerDisplayName: String = "",
    val peerAvatarUrl: String? = null,
    val lastMessage: String = "",
    val lastMessageType: String = MessageType.TEXT.name,
    val lastTimestamp: Long = 0L,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val lastMessageSenderId: String = "",
    val lastMessageStatus: String = MessageStatus.SENT.name
)

enum class CallType {
    AUDIO, VIDEO
}

enum class CallStatus {
    RINGING, CONNECTED, REJECTED, ENDED, MISSED
}

@IgnoreExtraProperties
data class CallSession(
    val callId: String = "",
    val callerUid: String = "",
    val callerName: String = "",
    val callerAvatar: String? = null,
    val receiverUid: String = "",
    val receiverName: String = "",
    val receiverAvatar: String? = null,
    val type: String = CallType.AUDIO.name,
    val status: String = CallStatus.RINGING.name,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0
)

enum class StatusPrivacy {
    ALL, CONTACTS, PRIVATE
}

@IgnoreExtraProperties
data class StatusStory(
    val id: String = "",
    val uid: String = "",
    val username: String = "",
    val displayName: String = "",
    val avatarUrl: String? = null,
    val mediaUrl: String = "",
    val caption: String = "",
    val type: String = "IMAGE", // "IMAGE" or "VIDEO"
    val timestamp: Long = System.currentTimeMillis(),
    val privacy: String = StatusPrivacy.ALL.name,
    val viewers: Map<String, Long> = emptyMap()
)
