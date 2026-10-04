package com.whatsapp.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.whatsapp.android.config.FirebaseConfig
import com.whatsapp.android.data.models.*
import com.whatsapp.android.data.repository.*
import com.whatsapp.android.ui.components.FullscreenImageViewer
import com.whatsapp.android.ui.screens.auth.*
import com.whatsapp.android.ui.screens.call.ActiveCallScreen
import com.whatsapp.android.ui.screens.chat.ChatScreen
import com.whatsapp.android.ui.screens.home.HomeScreen
import com.whatsapp.android.ui.screens.permissions.PermissionsScreen
import com.whatsapp.android.ui.screens.profile.EditMyProfileScreen
import com.whatsapp.android.ui.screens.profile.UserProfileScreen
import com.whatsapp.android.ui.screens.settings.PrivacySettingsScreen
import com.whatsapp.android.ui.screens.settings.RecoverySetupScreen
import com.whatsapp.android.ui.screens.splash.SplashScreen
import com.whatsapp.android.ui.screens.status.AddStatusScreen
import com.whatsapp.android.ui.screens.status.ViewStatusScreen
import com.whatsapp.android.ui.theme.WhatsAppDarkBg
import com.whatsapp.android.ui.theme.WhatsAppTheme
import kotlinx.coroutines.launch

sealed interface AppRoute {
    data object Splash : AppRoute
    data object Permissions : AppRoute
    data object WelcomeAuth : AppRoute
    data object Login : AppRoute
    data object RegisterEmail : AppRoute
    data class SetPassword(val email: String) : AppRoute
    data class UsernameProfileSetup(
        val email: String,
        val password: String?,
        val initialDisplayName: String = "",
        val initialAvatarUrl: String? = null,
        val isGoogle: Boolean = false
    ) : AppRoute
    data object ForgetPassword : AppRoute
    data object Home : AppRoute
    data class Chat(val chatId: String, val peer: User) : AppRoute
    data class ActiveCall(val session: CallSession) : AppRoute
    data class UserProfile(val user: User) : AppRoute
    data object EditMyProfile : AppRoute
    data object RecoverySetup : AppRoute
    data object PrivacySettings : AppRoute
    data object AddStatus : AppRoute
    data class ViewStatus(val story: StatusStory) : AppRoute
    data class FullscreenPhoto(val url: String) : AppRoute
}

class MainActivity : ComponentActivity() {

    private val authRepo = AuthRepository()
    private val realtimeRepo = RealtimeRepository()
    private val statusRepo = StatusRepository()
    private val callRepo = CallRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WhatsAppTheme(darkTheme = true) {
                WhatsAppMainApp(
                    activity = this,
                    authRepo = authRepo,
                    realtimeRepo = realtimeRepo,
                    statusRepo = statusRepo,
                    callRepo = callRepo
                )
            }
        }
    }
}

@Composable
fun WhatsAppMainApp(
    activity: MainActivity,
    authRepo: AuthRepository,
    realtimeRepo: RealtimeRepository,
    statusRepo: StatusRepository,
    callRepo: CallRepository
) {
    val coroutineScope = rememberCoroutineScope()
    var currentRoute by remember { mutableStateOf<AppRoute>(AppRoute.Splash) }
    var currentUser by remember { mutableStateOf<User?>(null) }
    var authError by remember { mutableStateOf<String?>(null) }
    var isAuthLoading by remember { mutableStateOf(false) }

    // Listen to Firebase Auth state
    LaunchedEffect(Unit) {
        authRepo.observeCurrentUser().collect { user ->
            currentUser = user
            if (user != null && currentRoute is AppRoute.Splash) {
                currentRoute = AppRoute.Home
            }
        }
    }

    // Google Sign-In Client configuration
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(FirebaseConfig.WEB_CLIENT_ID)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(activity, gso) }

    // Google Sign-In Activity Result Launcher
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                isAuthLoading = true
                authError = null
                coroutineScope.launch {
                    val authRes = authRepo.signInWithGoogleIdToken(idToken)
                    isAuthLoading = false
                    authRes.onSuccess { (user, needsSetup) ->
                        currentUser = user
                        if (needsSetup) {
                            currentRoute = AppRoute.UsernameProfileSetup(
                                email = user.email,
                                password = null,
                                initialDisplayName = user.displayName,
                                initialAvatarUrl = user.avatarUrl,
                                isGoogle = true
                            )
                        } else {
                            currentRoute = AppRoute.Home
                        }
                    }.onFailure { e ->
                        authError = e.message ?: "Google sign in failed"
                    }
                }
            }
        } catch (e: Exception) {
            isAuthLoading = false
            authError = "Google Sign In: ${e.message ?: "cancelled"}"
        }
    }

    // Listen to incoming calls when user is logged in
    LaunchedEffect(currentUser?.uid) {
        val uid = currentUser?.uid ?: return@LaunchedEffect
        callRepo.observeIncomingCalls(uid).collect { incomingCall ->
            if (incomingCall != null && currentRoute !is AppRoute.ActiveCall) {
                // Incoming call detected
                currentRoute = AppRoute.ActiveCall(incomingCall)
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = WhatsAppDarkBg
    ) {
        when (val route = currentRoute) {
            AppRoute.Splash -> {
                SplashScreen(
                    onNavigateNext = {
                        if (currentUser != null) {
                            currentRoute = AppRoute.Home
                        } else {
                            currentRoute = AppRoute.Permissions
                        }
                    }
                )
            }

            AppRoute.Permissions -> {
                PermissionsScreen(
                    onPermissionsComplete = {
                        currentRoute = AppRoute.WelcomeAuth
                    }
                )
            }

            AppRoute.WelcomeAuth -> {
                WelcomeAuthScreen(
                    onCreateAccountClick = { currentRoute = AppRoute.RegisterEmail },
                    onLoginAccountClick = { currentRoute = AppRoute.Login }
                )
            }

            AppRoute.Login -> {
                LoginScreen(
                    isLoading = isAuthLoading,
                    errorMessage = authError,
                    onBack = { currentRoute = AppRoute.WelcomeAuth },
                    onLoginClick = { email, pass ->
                        isAuthLoading = true
                        authError = null
                        coroutineScope.launch {
                            val res = authRepo.signInWithEmail(email, pass)
                            isAuthLoading = false
                            res.onSuccess { user ->
                                currentUser = user
                                currentRoute = AppRoute.Home
                            }.onFailure { e ->
                                authError = e.message ?: "Login failed. Check email and password."
                            }
                        }
                    },
                    onForgetPasswordClick = { currentRoute = AppRoute.ForgetPassword },
                    onGoogleSignInClick = {
                        googleLauncher.launch(googleSignInClient.signInIntent)
                    }
                )
            }

            AppRoute.RegisterEmail -> {
                RegisterEmailScreen(
                    onBack = { currentRoute = AppRoute.WelcomeAuth },
                    onContinueEmail = { email ->
                        currentRoute = AppRoute.SetPassword(email)
                    },
                    onSignUpWithGoogle = {
                        googleLauncher.launch(googleSignInClient.signInIntent)
                    }
                )
            }

            is AppRoute.SetPassword -> {
                SetPasswordScreen(
                    email = route.email,
                    onBack = { currentRoute = AppRoute.RegisterEmail },
                    onPasswordSet = { pass ->
                        currentRoute = AppRoute.UsernameProfileSetup(
                            email = route.email,
                            password = pass,
                            isGoogle = false
                        )
                    }
                )
            }

            is AppRoute.UsernameProfileSetup -> {
                UsernameProfileSetupScreen(
                    authRepo = authRepo,
                    initialDisplayName = route.initialDisplayName,
                    initialAvatarUrl = route.initialAvatarUrl,
                    onComplete = { username, displayName, avatarUrl ->
                        coroutineScope.launch {
                            if (route.isGoogle && currentUser != null) {
                                authRepo.updateUsernameAndAvatar(currentUser!!.uid, username, avatarUrl, displayName)
                                currentUser = currentUser!!.copy(
                                    username = username,
                                    displayName = displayName,
                                    avatarUrl = avatarUrl
                                )
                                currentRoute = AppRoute.Home
                            } else if (route.password != null) {
                                val res = authRepo.createAccountWithEmail(
                                    email = route.email,
                                    password = route.password,
                                    rawUsername = username,
                                    displayName = displayName,
                                    avatarUrl = avatarUrl
                                )
                                res.onSuccess { newUser ->
                                    currentUser = newUser
                                    currentRoute = AppRoute.Home
                                }.onFailure { e ->
                                    authError = e.message
                                }
                            }
                        }
                    }
                )
            }

            AppRoute.ForgetPassword -> {
                ForgetPasswordRecoveryScreen(
                    authRepo = authRepo,
                    onBack = { currentRoute = AppRoute.Login },
                    onPasswordResetSuccess = { currentRoute = AppRoute.Login }
                )
            }

            AppRoute.Home -> {
                if (currentUser != null) {
                    HomeScreen(
                        currentUser = currentUser!!,
                        authRepo = authRepo,
                        realtimeRepo = realtimeRepo,
                        statusRepo = statusRepo,
                        callRepo = callRepo,
                        onOpenChat = { chatId, peer ->
                            currentRoute = AppRoute.Chat(chatId, peer)
                        },
                        onStartCall = { peer, callType ->
                            coroutineScope.launch {
                                val sessionRes = callRepo.startCall(currentUser!!, peer, callType)
                                sessionRes.onSuccess { session ->
                                    currentRoute = AppRoute.ActiveCall(session)
                                }
                            }
                        },
                        onViewProfile = { user -> currentRoute = AppRoute.UserProfile(user) },
                        onViewPhoto = { url -> url?.let { currentRoute = AppRoute.FullscreenPhoto(it) } },
                        onAddStatusClick = { currentRoute = AppRoute.AddStatus },
                        onViewStatusClick = { story -> currentRoute = AppRoute.ViewStatus(story) },
                        onStatusPrivacyClick = { currentRoute = AppRoute.PrivacySettings },
                        onEditMyProfileClick = { currentRoute = AppRoute.EditMyProfile },
                        onRecoverySetupClick = { currentRoute = AppRoute.RecoverySetup },
                        onPrivacySettingsClick = { currentRoute = AppRoute.PrivacySettings },
                        onSignOutClick = {
                            authRepo.signOut()
                            currentUser = null
                            currentRoute = AppRoute.WelcomeAuth
                        }
                    )
                } else {
                    currentRoute = AppRoute.WelcomeAuth
                }
            }

            is AppRoute.Chat -> {
                if (currentUser != null) {
                    ChatScreen(
                        chatId = route.chatId,
                        currentUser = currentUser!!,
                        peerUser = route.peer,
                        realtimeRepo = realtimeRepo,
                        onBack = { currentRoute = AppRoute.Home },
                        onPeerProfileClick = { currentRoute = AppRoute.UserProfile(route.peer) },
                        onStartCall = { peer, callType ->
                            coroutineScope.launch {
                                val sessionRes = callRepo.startCall(currentUser!!, peer, callType)
                                sessionRes.onSuccess { session ->
                                    currentRoute = AppRoute.ActiveCall(session)
                                }
                            }
                        },
                        onPhotoClick = { url -> currentRoute = AppRoute.FullscreenPhoto(url) }
                    )
                }
            }

            is AppRoute.ActiveCall -> {
                ActiveCallScreen(
                    callSession = route.session,
                    callRepo = callRepo,
                    onCallEnded = { currentRoute = AppRoute.Home }
                )
            }

            is AppRoute.UserProfile -> {
                if (currentUser != null) {
                    UserProfileScreen(
                        user = route.user,
                        currentUid = currentUser!!.uid,
                        realtimeRepo = realtimeRepo,
                        onBack = { currentRoute = AppRoute.Home },
                        onMessageClick = {
                            val chatId = realtimeRepo.getChatId(currentUser!!.uid, route.user.uid)
                            currentRoute = AppRoute.Chat(chatId, route.user)
                        },
                        onStartCall = { peer, callType ->
                            coroutineScope.launch {
                                val sessionRes = callRepo.startCall(currentUser!!, peer, callType)
                                sessionRes.onSuccess { session ->
                                    currentRoute = AppRoute.ActiveCall(session)
                                }
                            }
                        },
                        onPhotoClick = { url -> url?.let { currentRoute = AppRoute.FullscreenPhoto(it) } }
                    )
                }
            }

            AppRoute.EditMyProfile -> {
                if (currentUser != null) {
                    EditMyProfileScreen(
                        currentUser = currentUser!!,
                        authRepo = authRepo,
                        onBack = { currentRoute = AppRoute.Home }
                    )
                }
            }

            AppRoute.RecoverySetup -> {
                RecoverySetupScreen(
                    authRepo = authRepo,
                    onBack = { currentRoute = AppRoute.Home }
                )
            }

            AppRoute.PrivacySettings -> {
                if (currentUser != null) {
                    PrivacySettingsScreen(
                        currentUid = currentUser!!.uid,
                        realtimeRepo = realtimeRepo,
                        onBack = { currentRoute = AppRoute.Home }
                    )
                }
            }

            AppRoute.AddStatus -> {
                if (currentUser != null) {
                    AddStatusScreen(
                        currentUser = currentUser!!,
                        statusRepo = statusRepo,
                        onBack = { currentRoute = AppRoute.Home },
                        onStatusPosted = { currentRoute = AppRoute.Home }
                    )
                }
            }

            is AppRoute.ViewStatus -> {
                if (currentUser != null) {
                    ViewStatusScreen(
                        story = route.story,
                        currentUid = currentUser!!.uid,
                        statusRepo = statusRepo,
                        onClose = { currentRoute = AppRoute.Home }
                    )
                }
            }

            is AppRoute.FullscreenPhoto -> {
                FullscreenImageViewer(
                    imageUrl = route.url,
                    onBack = { currentRoute = AppRoute.Home }
                )
            }
        }
    }
}
