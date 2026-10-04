package com.whatsapp.android.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.whatsapp.android.config.FirebaseConfig
import com.whatsapp.android.data.models.RecoveryConfig
import com.whatsapp.android.data.models.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Locale

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseDatabase = FirebaseDatabase.getInstance(FirebaseConfig.DATABASE_URL)

    fun getCurrentUid(): String? = auth.currentUser?.uid

    fun observeCurrentUser(): Flow<User?> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val ref = db.reference.child(FirebaseConfig.Nodes.USERS).child(uid)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                trySend(user)
            }
            override fun onCancelled(error: DatabaseError) {
                // handle error or close
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun checkUsernameAvailability(rawUsername: String): Boolean {
        val clean = rawUsername.removePrefix("@").trim().lowercase(Locale.ROOT)
        if (clean.length < 3) return false
        val snap = db.reference.child(FirebaseConfig.Nodes.USERNAMES).child(clean).get().await()
        return !snap.exists()
    }

    suspend fun createAccountWithEmail(
        email: String,
        password: String,
        rawUsername: String,
        displayName: String,
        avatarUrl: String?
    ): Result<User> {
        return try {
            val cleanUsername = rawUsername.removePrefix("@").trim().lowercase(Locale.ROOT)
            val isAvailable = checkUsernameAvailability(cleanUsername)
            if (!isAvailable) {
                return Result.failure(Exception("Username @$cleanUsername is already taken. Please choose another."))
            }

            val authResult = auth.createUserWithEmailAndPassword(email.trim(), password).await()
            val firebaseUser = authResult.user ?: return Result.failure(Exception("User creation failed"))
            val uid = firebaseUser.uid

            val user = User(
                uid = uid,
                email = email.trim(),
                username = "@$cleanUsername",
                displayName = displayName.ifBlank { cleanUsername },
                avatarUrl = avatarUrl,
                bio = "Hey there! I am using WhatsApp.",
                isOnline = true,
                lastSeen = System.currentTimeMillis(),
                hasPasswordSet = true
            )

            // Save to /users/$uid
            db.reference.child(FirebaseConfig.Nodes.USERS).child(uid).setValue(user).await()
            // Reserve username in /usernames/$cleanUsername = uid
            db.reference.child(FirebaseConfig.Nodes.USERNAMES).child(cleanUsername).setValue(uid).await()
            // Store lookup entry in /userLookup for recovery
            val sanitizedEmailKey = email.trim().lowercase().replace(".", "_").replace("@", "_at_")
            db.reference.child(FirebaseConfig.Nodes.USER_LOOKUP).child(cleanUsername).setValue(uid).await()
            db.reference.child(FirebaseConfig.Nodes.USER_LOOKUP).child(sanitizedEmailKey).setValue(uid).await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email.trim(), password).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("Sign in failed"))
            val uid = firebaseUser.uid

            val userSnap = db.reference.child(FirebaseConfig.Nodes.USERS).child(uid).get().await()
            val user = userSnap.getValue(User::class.java) ?: User(
                uid = uid,
                email = firebaseUser.email ?: email,
                username = "@user_${uid.take(6)}",
                displayName = firebaseUser.displayName ?: "User",
                isOnline = true
            )

            // Update online state
            db.reference.child(FirebaseConfig.Nodes.USERS).child(uid).child("isOnline").setValue(true)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogleIdToken(idToken: String): Result<Pair<User, Boolean>> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: return Result.failure(Exception("Google sign in failed"))
            val uid = firebaseUser.uid
            val email = firebaseUser.email ?: ""
            val displayName = firebaseUser.displayName ?: "WhatsApp User"
            val photoUrl = firebaseUser.photoUrl?.toString()

            val userSnap = db.reference.child(FirebaseConfig.Nodes.USERS).child(uid).get().await()
            if (userSnap.exists()) {
                val existingUser = userSnap.getValue(User::class.java)!!
                Result.success(Pair(existingUser, false)) // false = not newly created
            } else {
                // Brand new user from Google: Needs username setup
                val initialUsername = "@${email.substringBefore("@").lowercase().replace(Regex("[^a-z0-9_]"), "")}"
                val newUser = User(
                    uid = uid,
                    email = email,
                    username = initialUsername,
                    displayName = displayName,
                    avatarUrl = photoUrl,
                    hasPasswordSet = false, // Must show popup on dashboard!
                    isOnline = true
                )
                db.reference.child(FirebaseConfig.Nodes.USERS).child(uid).setValue(newUser).await()
                val cleanUser = initialUsername.removePrefix("@")
                db.reference.child(FirebaseConfig.Nodes.USERNAMES).child(cleanUser).setValue(uid).await()

                val sanitizedEmailKey = email.lowercase().replace(".", "_").replace("@", "_at_")
                db.reference.child(FirebaseConfig.Nodes.USER_LOOKUP).child(cleanUser).setValue(uid).await()
                db.reference.child(FirebaseConfig.Nodes.USER_LOOKUP).child(sanitizedEmailKey).setValue(uid).await()

                Result.success(Pair(newUser, true)) // true = needs setup
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUsernameAndAvatar(uid: String, rawUsername: String, avatarUrl: String?, displayName: String): Result<Unit> {
        return try {
            val clean = rawUsername.removePrefix("@").trim().lowercase(Locale.ROOT)
            val updates = mutableMapOf<String, Any>(
                "username" to "@$clean",
                "displayName" to displayName
            )
            avatarUrl?.let { updates["avatarUrl"] = it }

            db.reference.child(FirebaseConfig.Nodes.USERS).child(uid).updateChildren(updates).await()
            db.reference.child(FirebaseConfig.Nodes.USERNAMES).child(clean).setValue(uid).await()
            db.reference.child(FirebaseConfig.Nodes.USER_LOOKUP).child(clean).setValue(uid).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setPasswordForUser(password: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
            user.updatePassword(password).await()
            db.reference.child(FirebaseConfig.Nodes.USERS).child(user.uid).child("hasPasswordSet").setValue(true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveRecoveryOption(method: String, secretValue: String): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
            val userSnap = db.reference.child(FirebaseConfig.Nodes.USERS).child(uid).get().await()
            val user = userSnap.getValue(User::class.java)

            val config = RecoveryConfig(
                uid = uid,
                email = user?.email ?: "",
                username = user?.username ?: "",
                method = method,
                secretValue = secretValue.trim(),
                isConfigured = true,
                updatedAt = System.currentTimeMillis()
            )

            // Save under recovery/$uid and also recovery/$lookupKey
            db.reference.child(FirebaseConfig.Nodes.RECOVERY).child(uid).setValue(config).await()

            user?.email?.let { em ->
                val emailKey = em.lowercase().replace(".", "_").replace("@", "_at_")
                db.reference.child(FirebaseConfig.Nodes.RECOVERY).child(emailKey).setValue(config).await()
            }
            user?.username?.removePrefix("@")?.lowercase()?.let { uname ->
                db.reference.child(FirebaseConfig.Nodes.RECOVERY).child(uname).setValue(config).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecoveryConfig(query: String): Result<RecoveryConfig?> {
        return try {
            val clean = query.trim().lowercase(Locale.ROOT).removePrefix("@")
            val emailKey = clean.replace(".", "_").replace("@", "_at_")

            var snap = db.reference.child(FirebaseConfig.Nodes.RECOVERY).child(emailKey).get().await()
            if (!snap.exists()) {
                snap = db.reference.child(FirebaseConfig.Nodes.RECOVERY).child(clean).get().await()
            }

            if (!snap.exists()) {
                // Try resolving uid via userLookup
                val uidSnap = db.reference.child(FirebaseConfig.Nodes.USER_LOOKUP).child(clean).get().await()
                if (uidSnap.exists()) {
                    val uid = uidSnap.getValue(String::class.java)
                    if (uid != null) {
                        snap = db.reference.child(FirebaseConfig.Nodes.RECOVERY).child(uid).get().await()
                    }
                }
            }

            val cfg = snap.getValue(RecoveryConfig::class.java)
            Result.success(cfg)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPasswordWithRecovery(
        emailOrUsername: String,
        selectedMethod: String,
        enteredSecret: String,
        newPassword: String
    ): Result<Unit> {
        return try {
            val cfgRes = getRecoveryConfig(emailOrUsername)
            val cfg = cfgRes.getOrNull() ?: return Result.failure(
                Exception("No recovery setup found for this account. Please verify email/username.")
            )

            if (!cfg.isConfigured) {
                return Result.failure(Exception("Password recovery has not been configured for this account."))
            }

            if (cfg.method != selectedMethod) {
                return Result.failure(Exception("Incorrect recovery method selected. Choose the method you configured in Settings."))
            }

            if (cfg.secretValue.trim() != enteredSecret.trim()) {
                return Result.failure(Exception("The entered recovery code/number is incorrect."))
            }

            // Recovery verified! We can sign in or update password
            // If email is available in recovery config, send password reset or update in RTDB
            if (cfg.email.isNotEmpty()) {
                auth.sendPasswordResetEmail(cfg.email).await()
            }

            // Also record the update
            db.reference.child(FirebaseConfig.Nodes.USERS).child(cfg.uid).child("passwordResetRequestedAt").setValue(System.currentTimeMillis()).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.reference.child(FirebaseConfig.Nodes.USERS).child(uid).child("isOnline").setValue(false)
            db.reference.child(FirebaseConfig.Nodes.USERS).child(uid).child("lastSeen").setValue(System.currentTimeMillis())
        }
        auth.signOut()
    }
}
