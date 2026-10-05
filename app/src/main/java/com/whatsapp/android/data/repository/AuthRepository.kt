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
            db.reference.child(FirebaseConfig.Nodes.USERS).child(uid).child("accountPassword").setValue(password).await()
            
            // Reserve username in /usernames/$cleanUsername = uid
            val cleanKey = sanitizeFirebaseKey(cleanUsername)
            db.reference.child(FirebaseConfig.Nodes.USERNAMES).child(cleanKey).setValue(uid).await()
            
            // Store lookup entry in /userLookup for recovery
            val sanitizedEmailKey = sanitizeFirebaseKey(email.trim().lowercase())
            db.reference.child(FirebaseConfig.Nodes.USER_LOOKUP).child(cleanKey).setValue(uid).await()
            db.reference.child(FirebaseConfig.Nodes.USER_LOOKUP).child(sanitizedEmailKey).setValue(uid).await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithEmail(email: String, password: String): Result<User> {
        return try {
            val trimmedEmail = email.trim()
            val firebaseUser = try {
                val result = auth.signInWithEmailAndPassword(trimmedEmail, password).await()
                result.user
            } catch (authEx: Exception) {
                null
            }

            var uid = firebaseUser?.uid
            if (uid == null) {
                // Check if account has updated password in RTDB (via 3-option recovery)
                val sanitizedKey = sanitizeFirebaseKey(trimmedEmail.lowercase())
                val cleanUser = sanitizeFirebaseKey(trimmedEmail.removePrefix("@").lowercase())
                val lookupSnap = db.reference.child(FirebaseConfig.Nodes.USER_LOOKUP).child(sanitizedKey).get().await()
                val foundUid = lookupSnap.getValue(String::class.java)
                    ?: db.reference.child(FirebaseConfig.Nodes.USER_LOOKUP).child(cleanUser).get().await().getValue(String::class.java)

                if (foundUid != null) {
                    val userSnap = db.reference.child(FirebaseConfig.Nodes.USERS).child(foundUid).get().await()
                    val savedPass = userSnap.child("accountPassword").getValue(String::class.java)
                    if (savedPass != null && savedPass == password) {
                        uid = foundUid
                    }
                }
            }

            if (uid == null) {
                return Result.failure(Exception("Login failed. Check email and password."))
            }

            val userSnap = db.reference.child(FirebaseConfig.Nodes.USERS).child(uid).get().await()
            val user = userSnap.getValue(User::class.java) ?: User(
                uid = uid,
                email = firebaseUser?.email ?: trimmedEmail,
                username = "@user_${uid.take(6)}",
                displayName = firebaseUser?.displayName ?: "User",
                isOnline = true
            )

            // Update online state
            db.reference.child(FirebaseConfig.Nodes.USERS).child(uid).child("isOnline").setValue(true)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogleIdToken(idToken: String, forceSetup: Boolean = false): Result<Pair<User, Boolean>> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user ?: return Result.failure(Exception("Google sign in failed"))
            val uid = firebaseUser.uid
            val email = firebaseUser.email ?: ""
            val displayName = firebaseUser.displayName ?: "WhatsApp User"
            val photoUrl = firebaseUser.photoUrl?.toString()

            val userSnap = db.reference.child(FirebaseConfig.Nodes.USERS).child(uid).get().await()
            val existingUser = if (userSnap.exists()) userSnap.getValue(User::class.java) else null

            // If user doesn't exist OR has a default temporary username OR forceSetup is requested
            val isCustomUser = existingUser != null &&
                    existingUser.username.isNotEmpty() &&
                    !existingUser.username.startsWith("@user_")

            if (existingUser != null && isCustomUser && !forceSetup) {
                Result.success(Pair(existingUser, false))
            } else {
                val initialUsername = "@${email.substringBefore("@").lowercase().filter { it.isLetterOrDigit() || it == '_' }}"
                val newUser = (existingUser ?: User(
                    uid = uid,
                    email = email,
                    username = initialUsername,
                    displayName = displayName,
                    avatarUrl = photoUrl,
                    hasPasswordSet = false,
                    isOnline = true
                )).copy(isOnline = true)

                db.reference.child(FirebaseConfig.Nodes.USERS).child(uid).setValue(newUser).await()
                val cleanUser = initialUsername.removePrefix("@")
                db.reference.child(FirebaseConfig.Nodes.USERNAMES).child(cleanUser).setValue(uid).await()

                val sanitizedEmailKey = sanitizeFirebaseKey(email.lowercase())
                db.reference.child(FirebaseConfig.Nodes.USER_LOOKUP).child(cleanUser).setValue(uid).await()
                db.reference.child(FirebaseConfig.Nodes.USER_LOOKUP).child(sanitizedEmailKey).setValue(uid).await()

                Result.success(Pair(newUser, true)) // Proceed directly to username and profile setup
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

            // Also update recovery record if exists
            val recSnap = db.reference.child(FirebaseConfig.Nodes.RECOVERY).child(uid).get().await()
            if (recSnap.exists()) {
                val cfg = recSnap.getValue(RecoveryConfig::class.java)
                if (cfg != null) {
                    val updatedCfg = cfg.copy(username = "@$clean")
                    db.reference.child(FirebaseConfig.Nodes.RECOVERY).child(uid).setValue(updatedCfg)
                    db.reference.child(FirebaseConfig.Nodes.RECOVERY).child(clean).setValue(updatedCfg)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setPasswordForUser(password: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
            user.updatePassword(password).await()
            val updates = mapOf(
                "hasPasswordSet" to true,
                "accountPassword" to password
            )
            db.reference.child(FirebaseConfig.Nodes.USERS).child(user.uid).updateChildren(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun sanitizeFirebaseKey(raw: String): String {
        return raw.trim()
            .replace(".", "_")
            .replace("@", "_at_")
            .replace("#", "_")
            .replace("$", "_")
            .replace("[", "_")
            .replace("]", "_")
            .replace("/", "_")
    }

    suspend fun saveRecoveryOption(method: String, secretValue: String): Result<Unit> {
        return try {
            val currentAuth = auth.currentUser ?: return Result.failure(Exception("Not logged in"))
            val uid = currentAuth.uid
            val userSnap = db.reference.child(FirebaseConfig.Nodes.USERS).child(uid).get().await()
            val dbUser = userSnap.getValue(User::class.java)

            val email = dbUser?.email?.ifBlank { currentAuth.email ?: "" } ?: (currentAuth.email ?: "")
            val username = dbUser?.username?.ifBlank { "" } ?: ""
            val cleanUsername = username.removePrefix("@").lowercase().trim()
            val normalizedSecret = secretValue.filter { it.isDigit() }.ifBlank { secretValue.trim() }

            val config = RecoveryConfig(
                uid = uid,
                email = email,
                username = username,
                method = method,
                secretValue = normalizedSecret,
                isConfigured = true,
                updatedAt = System.currentTimeMillis()
            )

            // 1. Save directly under recovery/$uid
            db.reference.child(FirebaseConfig.Nodes.RECOVERY).child(uid).setValue(config).await()

            // 2. Save directly inside /users/$uid/recoveryConfig
            db.reference.child(FirebaseConfig.Nodes.USERS).child(uid).child("recoveryConfig").setValue(config).await()

            // 3. Save by sanitized email key
            if (email.isNotEmpty()) {
                val emailKey = sanitizeFirebaseKey(email.lowercase())
                db.reference.child(FirebaseConfig.Nodes.RECOVERY).child(emailKey).setValue(config).await()
                db.reference.child(FirebaseConfig.Nodes.USER_LOOKUP).child(emailKey).setValue(uid).await()
            }

            // 4. Save by sanitized clean username
            if (cleanUsername.isNotEmpty()) {
                val userKey = sanitizeFirebaseKey(cleanUsername)
                db.reference.child(FirebaseConfig.Nodes.RECOVERY).child(userKey).setValue(config).await()
                db.reference.child(FirebaseConfig.Nodes.USER_LOOKUP).child(userKey).setValue(uid).await()
            }

            // 5. Index by secret digits for instant lookup
            if (normalizedSecret.isNotEmpty()) {
                db.reference.child(FirebaseConfig.Nodes.RECOVERY).child("by_secret_$normalizedSecret").setValue(config).await()
                db.reference.child("recovery_secrets").child(normalizedSecret).setValue(uid).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecoveryConfig(query: String): Result<RecoveryConfig?> {
        return try {
            val clean = query.trim().lowercase(Locale.ROOT).removePrefix("@")
            val sanitizedKey = sanitizeFirebaseKey(clean)
            val digitsOnly = query.filter { it.isDigit() }

            var targetConfig: RecoveryConfig? = null

            // Index 1: By secret digits if query looks like CNIC or 8-digit code or phone
            if (digitsOnly.length >= 8) {
                val secretSnap = db.reference.child(FirebaseConfig.Nodes.RECOVERY).child("by_secret_$digitsOnly").get().await()
                if (secretSnap.exists()) {
                    targetConfig = secretSnap.getValue(RecoveryConfig::class.java)
                }
                if (targetConfig == null) {
                    val uidSnap = db.reference.child("recovery_secrets").child(digitsOnly).get().await()
                    val uid = uidSnap.getValue(String::class.java)
                    if (uid != null) {
                        val directSnap = db.reference.child(FirebaseConfig.Nodes.RECOVERY).child(uid).get().await()
                        if (directSnap.exists()) targetConfig = directSnap.getValue(RecoveryConfig::class.java)
                    }
                }
            }

            // Index 2: Directly by sanitized email/username key
            if (targetConfig == null && sanitizedKey.isNotEmpty()) {
                val snap = db.reference.child(FirebaseConfig.Nodes.RECOVERY).child(sanitizedKey).get().await()
                if (snap.exists()) targetConfig = snap.getValue(RecoveryConfig::class.java)
            }

            // Index 3: Via userLookup
            if (targetConfig == null && sanitizedKey.isNotEmpty()) {
                val uidSnap = db.reference.child(FirebaseConfig.Nodes.USER_LOOKUP).child(sanitizedKey).get().await()
                val uid = uidSnap.getValue(String::class.java)
                if (uid != null) {
                    val directSnap = db.reference.child(FirebaseConfig.Nodes.RECOVERY).child(uid).get().await()
                    if (directSnap.exists()) targetConfig = directSnap.getValue(RecoveryConfig::class.java)
                    if (targetConfig == null) {
                        val userSnap = db.reference.child(FirebaseConfig.Nodes.USERS).child(uid).child("recoveryConfig").get().await()
                        if (userSnap.exists()) targetConfig = userSnap.getValue(RecoveryConfig::class.java)
                    }
                }
            }

            // Index 4: Via usernames table
            if (targetConfig == null && sanitizedKey.isNotEmpty()) {
                val uidSnap = db.reference.child(FirebaseConfig.Nodes.USERNAMES).child(sanitizedKey).get().await()
                val uid = uidSnap.getValue(String::class.java)
                if (uid != null) {
                    val directSnap = db.reference.child(FirebaseConfig.Nodes.RECOVERY).child(uid).get().await()
                    if (directSnap.exists()) targetConfig = directSnap.getValue(RecoveryConfig::class.java)
                }
            }

            Result.success(targetConfig)
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
                Exception("No recovery setup found for account '$emailOrUsername'. Please check your username or email.")
            )

            if (!cfg.isConfigured) {
                return Result.failure(Exception("Password recovery has not been configured for this account."))
            }

            if (cfg.method != selectedMethod) {
                return Result.failure(Exception("Selected method '${selectedMethod}' does not match your configured method '${cfg.method}'."))
            }

            val savedDigits = cfg.secretValue.filter { it.isDigit() }
            val enteredDigits = enteredSecret.filter { it.isDigit() }
            val isMatch = if (savedDigits.isNotEmpty() && enteredDigits.isNotEmpty()) {
                savedDigits == enteredDigits
            } else {
                cfg.secretValue.trim().equals(enteredSecret.trim(), ignoreCase = true)
            }

            if (!isMatch) {
                return Result.failure(Exception("The entered recovery code/number is incorrect."))
            }

            // 1. Try to re-authenticate with existing stored password and update Firebase Auth password
            try {
                val oldPassSnap = db.reference.child(FirebaseConfig.Nodes.USERS).child(cfg.uid).child("accountPassword").get().await()
                val oldPass = oldPassSnap.getValue(String::class.java)
                if (oldPass != null && cfg.email.isNotEmpty()) {
                    val authRes = auth.signInWithEmailAndPassword(cfg.email, oldPass).await()
                    authRes.user?.updatePassword(newPassword)?.await()
                }
            } catch (ignored: Exception) {
                // Ignore if Firebase Auth sign-in failed, fallback to RTDB sync
            }

            // 2. Recovery verified! Update password record in Firebase RTDB
            val updates = mapOf(
                "hasPasswordSet" to true,
                "accountPassword" to newPassword,
                "passwordResetTimestamp" to System.currentTimeMillis()
            )
            db.reference.child(FirebaseConfig.Nodes.USERS).child(cfg.uid).updateChildren(updates).await()

            // 3. If account has email, also send official Firebase reset link as secondary confirmation
            if (cfg.email.isNotEmpty()) {
                try {
                    auth.sendPasswordResetEmail(cfg.email).await()
                } catch (ignored: Exception) {}
            }

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
