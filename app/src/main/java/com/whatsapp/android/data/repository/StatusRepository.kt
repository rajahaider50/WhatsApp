package com.whatsapp.android.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.whatsapp.android.config.FirebaseConfig
import com.whatsapp.android.data.models.StatusStory
import com.whatsapp.android.data.models.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class StatusRepository {
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance(FirebaseConfig.DATABASE_URL)

    // Observe active statuses (within 24 hours)
    fun observeStatuses(): Flow<List<StatusStory>> = callbackFlow {
        val ref = db.reference.child(FirebaseConfig.Nodes.STATUSES)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<StatusStory>()
                val cutoff = System.currentTimeMillis() - 24 * 60 * 60 * 1000 // 24 hours
                for (child in snapshot.children) {
                    val status = child.getValue(StatusStory::class.java)
                    if (status != null && status.timestamp >= cutoff) {
                        list.add(status)
                    }
                }
                list.sortByDescending { it.timestamp }
                trySend(list)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun postStatus(
        user: User,
        mediaUrl: String,
        caption: String,
        type: String = "IMAGE",
        privacy: String = "ALL"
    ): Result<StatusStory> {
        return try {
            val statusId = db.reference.child(FirebaseConfig.Nodes.STATUSES).push().key
                ?: "status_${System.currentTimeMillis()}"

            val status = StatusStory(
                id = statusId,
                uid = user.uid,
                username = user.username,
                displayName = user.displayName,
                avatarUrl = user.avatarUrl,
                mediaUrl = mediaUrl,
                caption = caption,
                type = type,
                timestamp = System.currentTimeMillis(),
                privacy = privacy
            )

            db.reference.child(FirebaseConfig.Nodes.STATUSES).child(statusId).setValue(status).await()
            Result.success(status)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markStatusViewed(statusId: String, currentUid: String) {
        db.reference.child(FirebaseConfig.Nodes.STATUSES).child(statusId)
            .child("viewers").child(currentUid).setValue(System.currentTimeMillis()).await()
    }

    suspend fun deleteStatus(statusId: String) {
        db.reference.child(FirebaseConfig.Nodes.STATUSES).child(statusId).removeValue().await()
    }
}
