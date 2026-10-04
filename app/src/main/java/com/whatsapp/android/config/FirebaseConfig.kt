package com.whatsapp.android.config

object FirebaseConfig {
    const val DATABASE_URL = "https://whatsapp-app-437b9-default-rtdb.firebaseio.com"
    const val WEB_CLIENT_ID = "255490839980-8845cass8kmq3avda8uku4246u6ot85d.apps.googleusercontent.com"

    object Nodes {
        const val USERS = "users"
        const val USERNAMES = "usernames"
        const val USER_LOOKUP = "userLookup"
        const val RECOVERY = "recovery"
        const val CHATS = "chats"
        const val MESSAGES = "messages"
        const val CALLS = "calls"
        const val STATUSES = "statuses"
        const val BLOCKED = "blocked"
    }
}
