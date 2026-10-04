package com.whatsapp.android.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.whatsapp.android.data.models.*
import com.whatsapp.android.data.repository.AuthRepository
import com.whatsapp.android.data.repository.CallRepository
import com.whatsapp.android.data.repository.RealtimeRepository
import com.whatsapp.android.data.repository.StatusRepository
import com.whatsapp.android.ui.components.SetPasswordReminderDialog
import com.whatsapp.android.ui.components.WhatsAppBottomNav
import com.whatsapp.android.ui.components.WhatsAppTab
import com.whatsapp.android.ui.components.WhatsAppTopBar
import com.whatsapp.android.ui.screens.settings.SettingsScreen
import com.whatsapp.android.ui.theme.WhatsAppDarkBg
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    currentUser: User,
    authRepo: AuthRepository,
    realtimeRepo: RealtimeRepository,
    statusRepo: StatusRepository,
    callRepo: CallRepository,
    onOpenChat: (chatId: String, peer: User) -> Unit,
    onStartCall: (User, CallType) -> Unit,
    onViewProfile: (User) -> Unit,
    onViewPhoto: (String?) -> Unit,
    onAddStatusClick: () -> Unit,
    onViewStatusClick: (StatusStory) -> Unit,
    onStatusPrivacyClick: () -> Unit,
    onEditMyProfileClick: () -> Unit,
    onRecoverySetupClick: () -> Unit,
    onPrivacySettingsClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(WhatsAppTab.CHATS) }
    var showNewChatDialog by remember { mutableStateOf(false) }

    // Recurring Password Reminder Dialog for Google sign-in users
    var showPasswordReminder by remember { mutableStateOf(!currentUser.hasPasswordSet) }

    // Observe Chats
    var chats by remember { mutableStateOf<List<ChatPreview>>(emptyList()) }
    LaunchedEffect(currentUser.uid) {
        realtimeRepo.observeChats(currentUser.uid).collect {
            chats = it
        }
    }

    // Observe Statuses
    var statuses by remember { mutableStateOf<List<StatusStory>>(emptyList()) }
    LaunchedEffect(Unit) {
        statusRepo.observeStatuses().collect {
            statuses = it
        }
    }

    // Observe Call History
    var callHistory by remember { mutableStateOf<List<CallSession>>(emptyList()) }
    LaunchedEffect(currentUser.uid) {
        callRepo.observeCallHistory(currentUser.uid).collect {
            callHistory = it
        }
    }

    // Calculate unread count for badge
    val unreadChatsCount = remember(chats) {
        chats.sumOf { it.unreadCount }
    }

    Scaffold(
        topBar = {
            WhatsAppTopBar(
                title = when (selectedTab) {
                    WhatsAppTab.CHATS -> "WhatsApp"
                    WhatsAppTab.UPDATES -> "Updates"
                    WhatsAppTab.CALLS -> "Calls"
                    WhatsAppTab.SETTINGS -> "Settings"
                },
                onCameraClick = onAddStatusClick,
                onSearchClick = { showNewChatDialog = true },
                onSettingsClick = { selectedTab = WhatsAppTab.SETTINGS },
                onStarredClick = {},
                onRecoveryClick = onRecoverySetupClick,
                onProfileClick = onEditMyProfileClick,
                onSignOutClick = onSignOutClick
            )
        },
        bottomBar = {
            WhatsAppBottomNav(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                unreadChatsCount = unreadChatsCount
            )
        },
        containerColor = WhatsAppDarkBg,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(WhatsAppDarkBg)
        ) {
            when (selectedTab) {
                WhatsAppTab.CHATS -> {
                    ChatsTab(
                        chats = chats,
                        currentUid = currentUser.uid,
                        onChatClick = { preview ->
                            val peer = User(
                                uid = preview.peerUid,
                                username = preview.peerUsername,
                                displayName = preview.peerDisplayName,
                                avatarUrl = preview.peerAvatarUrl,
                                isOnline = preview.isOnline
                            )
                            onOpenChat(preview.chatId, peer)
                        },
                        onNewChatClick = { showNewChatDialog = true },
                        onAudioCallClick = { peer -> onStartCall(peer, CallType.AUDIO) },
                        onVideoCallClick = { peer -> onStartCall(peer, CallType.VIDEO) },
                        onInfoClick = onViewProfile,
                        onPhotoClick = onViewPhoto
                    )
                }

                WhatsAppTab.UPDATES -> {
                    StatusTab(
                        currentUser = currentUser,
                        statuses = statuses,
                        onAddStatusClick = onAddStatusClick,
                        onStatusClick = onViewStatusClick,
                        onPrivacyClick = onStatusPrivacyClick
                    )
                }

                WhatsAppTab.CALLS -> {
                    CallsTab(
                        currentUid = currentUser.uid,
                        callHistory = callHistory,
                        onStartCall = onStartCall,
                        onNewCallClick = { showNewChatDialog = true }
                    )
                }

                WhatsAppTab.SETTINGS -> {
                    SettingsScreen(
                        currentUser = currentUser,
                        onProfileClick = onEditMyProfileClick,
                        onAccountRecoveryClick = onRecoverySetupClick,
                        onPrivacyClick = onPrivacySettingsClick,
                        onStarredClick = {},
                        onSignOutClick = onSignOutClick
                    )
                }
            }
        }
    }

    // New Chat / Search Dialog
    if (showNewChatDialog) {
        NewChatDialog(
            realtimeRepo = realtimeRepo,
            onDismiss = { showNewChatDialog = false },
            onUserSelected = { selectedUser ->
                val chatId = realtimeRepo.getChatId(currentUser.uid, selectedUser.uid)
                onOpenChat(chatId, selectedUser)
            }
        )
    }

    // Set Password Reminder Dialog (reappears until set for Google accounts)
    if (showPasswordReminder && !currentUser.hasPasswordSet) {
        SetPasswordReminderDialog(
            onDismiss = { showPasswordReminder = false },
            onSavePassword = { newPass ->
                coroutineScope.launch {
                    val res = authRepo.setPasswordForUser(newPass)
                    if (res.isSuccess) {
                        showPasswordReminder = false
                    }
                }
            }
        )
    }
}
