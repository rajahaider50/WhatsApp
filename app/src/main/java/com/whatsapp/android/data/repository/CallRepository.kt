package com.whatsapp.android.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.whatsapp.android.config.FirebaseConfig
import com.whatsapp.android.data.models.CallSession
import com.whatsapp.android.data.models.CallStatus
import com.whatsapp.android.data.models.CallType
import com.whatsapp.android.data.models.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CallRepository {
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance(FirebaseConfig.DATABASE_URL)

    // Listen for incoming calls targeted to current user
    fun observeIncomingCalls(currentUid: String): Flow<CallSession?> = callbackFlow {
        val ref = db.reference.child(FirebaseConfig.Nodes.CALLS)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var activeIncoming: CallSession? = null
                for (child in snapshot.children) {
                    val session = child.getValue(CallSession::class.java)
                    if (session != null &&
                        session.receiverUid == currentUid &&
                        session.status == CallStatus.RINGING.name &&
                        System.currentTimeMillis() - session.timestamp < 60000 // within 60s
                    ) {
                        activeIncoming = session
                        break
                    }
                }
                trySend(activeIncoming)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // Observe specific call session
    fun observeCall(callId: String): Flow<CallSession?> = callbackFlow {
        val ref = db.reference.child(FirebaseConfig.Nodes.CALLS).child(callId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val session = snapshot.getValue(CallSession::class.java)
                trySend(session)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // Initiate a call
    suspend fun startCall(caller: User, receiver: User, type: CallType): Result<CallSession> {
        return try {
            val callId = db.reference.child(FirebaseConfig.Nodes.CALLS).push().key
                ?: "call_${System.currentTimeMillis()}"

            val session = CallSession(
                callId = callId,
                callerUid = caller.uid,
                callerName = caller.displayName,
                callerAvatar = caller.avatarUrl,
                receiverUid = receiver.uid,
                receiverName = receiver.displayName,
                receiverAvatar = receiver.avatarUrl,
                type = type.name,
                status = CallStatus.RINGING.name,
                timestamp = System.currentTimeMillis()
            )

            db.reference.child(FirebaseConfig.Nodes.CALLS).child(callId).setValue(session).await()
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptCall(callId: String) {
        db.reference.child(FirebaseConfig.Nodes.CALLS).child(callId).child("status")
            .setValue(CallStatus.CONNECTED.name).await()
    }

    suspend fun rejectCall(callId: String) {
        db.reference.child(FirebaseConfig.Nodes.CALLS).child(callId).child("status")
            .setValue(CallStatus.REJECTED.name).await()
    }

    suspend fun endCall(callId: String, durationSeconds: Int = 0) {
        val updates = mapOf(
            "status" to CallStatus.ENDED.name,
            "durationSeconds" to durationSeconds
        )
        db.reference.child(FirebaseConfig.Nodes.CALLS).child(callId).updateChildren(updates).await()
    }

    // Observe call history for user
    fun observeCallHistory(currentUid: String): Flow<List<CallSession>> = callbackFlow {
        val ref = db.reference.child(FirebaseConfig.Nodes.CALLS)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<CallSession>()
                for (child in snapshot.children) {
                    val session = child.getValue(CallSession::class.java)
                    if (session != null && (session.callerUid == currentUid || session.receiverUid == currentUid)) {
                        list.add(session)
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
}
