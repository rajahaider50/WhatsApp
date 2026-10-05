package com.whatsapp.android.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.whatsapp.android.config.FirebaseConfig
import com.whatsapp.android.data.models.ChatMessage
import com.whatsapp.android.data.models.ChatPreview
import com.whatsapp.android.data.models.MessageStatus
import com.whatsapp.android.data.models.MessageType
import com.whatsapp.android.data.models.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Locale

class RealtimeRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance(FirebaseConfig.DATABASE_URL)

    fun getCurrentUid(): String? = auth.currentUser?.uid

    // Generate consistent chatId between two users (supports self-chat "self_$uid")
    fun getChatId(uid1: String, uid2: String): String {
        return if (uid1 == uid2) "self_$uid1" else if (uid1 < uid2) "${uid1}_${uid2}" else "${uid2}_${uid1}"
    }

    // Observe chats list for a user
    fun observeChats(uid: String): Flow<List<ChatPreview>> = callbackFlow {
        val ref = db.reference.child(FirebaseConfig.Nodes.CHATS).child(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ChatPreview>()
                for (child in snapshot.children) {
                    val preview = child.getValue(ChatPreview::class.java)
                    if (preview != null) {
                        list.add(preview)
                    }
                }
                // Sort by last timestamp descending
                list.sortByDescending { it.lastTimestamp }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // Observe messages in a chat
    fun observeMessages(chatId: String, currentUid: String): Flow<List<ChatMessage>> = callbackFlow {
        val ref = db.reference.child(FirebaseConfig.Nodes.MESSAGES).child(chatId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<ChatMessage>()
                for (child in snapshot.children) {
                    val msg = child.getValue(ChatMessage::class.java)
                    if (msg != null) {
                        // Check if deleted for me
                        val isDeletedForMe = msg.deletedForUsers[currentUid] == true
                        if (!isDeletedForMe) {
                            list.add(msg)
                            // If received message is not seen yet, mark as seen
                            if (msg.receiverId == currentUid && msg.status != MessageStatus.SEEN.name) {
                                child.ref.child("status").setValue(MessageStatus.SEEN.name)
                            }
                        }
                    }
                }
                list.sortBy { it.timestamp }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // Send a message
    suspend fun sendMessage(
        chatId: String,
        sender: User,
        receiver: User,
        text: String,
        type: MessageType = MessageType.TEXT,
        mediaUrl: String? = null,
        isViewOnce: Boolean = false,
        audioDurationSeconds: Int = 0,
        fileName: String? = null
    ): Result<ChatMessage> {
        return try {
            val msgId = db.reference.child(FirebaseConfig.Nodes.MESSAGES).child(chatId).push().key
                ?: "msg_${System.currentTimeMillis()}"

            val isSelf = sender.uid == receiver.uid || chatId.startsWith("self_")
            val initialStatus = if (isSelf) MessageStatus.SEEN.name else MessageStatus.SENT.name

            val message = ChatMessage(
                id = msgId,
                chatId = chatId,
                senderId = sender.uid,
                senderName = sender.displayName,
                receiverId = receiver.uid,
                text = text,
                type = type.name,
                mediaUrl = mediaUrl,
                isViewOnce = isViewOnce,
                isViewed = isSelf,
                isStarred = false,
                isDeletedForEveryone = false,
                status = initialStatus,
                timestamp = System.currentTimeMillis(),
                audioDurationSeconds = audioDurationSeconds,
                fileName = fileName
            )

            // Save message under /messages/$chatId/$msgId
            db.reference.child(FirebaseConfig.Nodes.MESSAGES).child(chatId).child(msgId).setValue(message).await()

            val lastMsgText = when (type) {
                MessageType.TEXT -> text
                MessageType.IMAGE -> if (isViewOnce) "📷 1 Photo (View Once)" else "📷 Photo"
                MessageType.VIDEO -> "📹 Video"
                MessageType.AUDIO -> "🎤 Voice message (${audioDurationSeconds}s)"
                MessageType.DOCUMENT -> "📄 ${fileName ?: "Document"}"
            }

            // Update sender's chat preview
            val senderPreview = ChatPreview(
                chatId = chatId,
                peerUid = receiver.uid,
                peerUsername = if (isSelf) sender.username else receiver.username,
                peerDisplayName = if (isSelf) "You (Message yourself)" else receiver.displayName,
                peerAvatarUrl = receiver.avatarUrl,
                lastMessage = lastMsgText,
                lastMessageType = type.name,
                lastTimestamp = message.timestamp,
                unreadCount = 0,
                isOnline = true,
                lastMessageSenderId = sender.uid,
                lastMessageStatus = initialStatus
            )
            db.reference.child(FirebaseConfig.Nodes.CHATS).child(sender.uid).child(chatId).setValue(senderPreview).await()

            // Update receiver's chat preview with incremented unread count (if not self chat)
            if (!isSelf) {
                val receiverPreview = ChatPreview(
                    chatId = chatId,
                    peerUid = sender.uid,
                    peerUsername = sender.username,
                    peerDisplayName = sender.displayName,
                    peerAvatarUrl = sender.avatarUrl,
                    lastMessage = lastMsgText,
                    lastMessageType = type.name,
                    lastTimestamp = message.timestamp,
                    unreadCount = 1,
                    isOnline = sender.isOnline,
                    lastMessageSenderId = sender.uid,
                    lastMessageStatus = initialStatus
                )
                db.reference.child(FirebaseConfig.Nodes.CHATS).child(receiver.uid).child(chatId).setValue(receiverPreview).await()
            }

            Result.success(message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Toggle star on message
    suspend fun toggleStar(chatId: String, msgId: String, currentStarred: Boolean) {
        db.reference.child(FirebaseConfig.Nodes.MESSAGES)
            .child(chatId).child(msgId).child("isStarred").setValue(!currentStarred).await()
    }

    // Delete message for me
    suspend fun deleteForMe(chatId: String, msgId: String, currentUid: String) {
        db.reference.child(FirebaseConfig.Nodes.MESSAGES)
            .child(chatId).child(msgId).child("deletedForUsers").child(currentUid).setValue(true).await()
    }

    // Delete message for everyone
    suspend fun deleteForEveryone(chatId: String, msgId: String) {
        db.reference.child(FirebaseConfig.Nodes.MESSAGES)
            .child(chatId).child(msgId).updateChildren(
                mapOf(
                    "isDeletedForEveryone" to true,
                    "text" to "This message was deleted",
                    "mediaUrl" to null
                )
            ).await()
    }

    // Mark view once as viewed
    suspend fun markViewOnceOpened(chatId: String, msgId: String) {
        db.reference.child(FirebaseConfig.Nodes.MESSAGES)
            .child(chatId).child(msgId).updateChildren(
                mapOf(
                    "isViewed" to true,
                    "mediaUrl" to null // erase media URL for view once security
                )
            ).await()
    }

    // Search user by exact username (@xxxxxx)
    suspend fun findUserByUsername(rawQuery: String): User? {
        val clean = rawQuery.removePrefix("@").trim().lowercase(Locale.ROOT)
        if (clean.isEmpty()) return null

        val uidSnap = db.reference.child(FirebaseConfig.Nodes.USERNAMES).child(clean).get().await()
        if (!uidSnap.exists()) return null
        val uid = uidSnap.getValue(String::class.java) ?: return null

        val userSnap = db.reference.child(FirebaseConfig.Nodes.USERS).child(uid).get().await()
        return userSnap.getValue(User::class.java)
    }

    // Get user by UID
    suspend fun getUser(uid: String): User? {
        val snap = db.reference.child(FirebaseConfig.Nodes.USERS).child(uid).get().await()
        return snap.getValue(User::class.java)
    }

    // Block / Unblock user
    suspend fun setBlockedStatus(currentUid: String, peerUid: String, isBlocked: Boolean) {
        if (isBlocked) {
            db.reference.child(FirebaseConfig.Nodes.BLOCKED).child(currentUid).child(peerUid).setValue(true).await()
        } else {
            db.reference.child(FirebaseConfig.Nodes.BLOCKED).child(currentUid).child(peerUid).removeValue().await()
        }
    }

    // Observe blocked users
    fun observeBlockedUsers(currentUid: String): Flow<List<String>> = callbackFlow {
        val ref = db.reference.child(FirebaseConfig.Nodes.BLOCKED).child(currentUid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<String>()
                for (child in snapshot.children) {
                    list.add(child.key ?: "")
                }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // Observe starred messages for user across all their chats
    fun observeStarredMessages(currentUid: String): Flow<List<ChatMessage>> = callbackFlow {
        val chatsRef = db.reference.child(FirebaseConfig.Nodes.CHATS).child(currentUid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatIds = snapshot.children.mapNotNull { it.key }
                if (chatIds.isEmpty()) {
                    trySend(emptyList())
                    return
                }
                val allStarred = mutableListOf<ChatMessage>()
                var remaining = chatIds.size
                for (cid in chatIds) {
                    db.reference.child(FirebaseConfig.Nodes.MESSAGES).child(cid)
                        .orderByChild("isStarred").equalTo(true)
                        .get().addOnCompleteListener { task ->
                            if (task.isSuccessful && task.result != null) {
                                for (msgChild in task.result.children) {
                                    val msg = msgChild.getValue(ChatMessage::class.java)
                                    if (msg != null && msg.deletedForUsers[currentUid] != true) {
                                        allStarred.add(msg)
                                    }
                                }
                            }
                            remaining--
                            if (remaining == 0) {
                                allStarred.sortByDescending { it.timestamp }
                                trySend(allStarred.toList())
                            }
                        }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                trySend(emptyList())
            }
        }
        chatsRef.addValueEventListener(listener)
        awaitClose { chatsRef.removeEventListener(listener) }
    }
}
