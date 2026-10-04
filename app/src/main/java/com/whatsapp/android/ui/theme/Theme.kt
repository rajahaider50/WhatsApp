package com.whatsapp.android.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = WhatsAppLightGreen,
    secondary = WhatsAppTeal,
    background = WhatsAppDarkBg,
    surface = WhatsAppDarkSurface,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = WhatsAppTextLight,
    onSurface = WhatsAppTextLight,
    surfaceVariant = WhatsAppDarkCard,
    onSurfaceVariant = WhatsAppTextMuted
)

private val LightColorScheme = lightColorScheme(
    primary = WhatsAppGreen,
    secondary = WhatsAppTeal,
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF111B21),
    onSurface = Color(0xFF111B21),
    surfaceVariant = Color(0xFFF0F2F5),
    onSurfaceVariant = Color(0xFF54656F)
)

@Composable
fun WhatsAppTheme(
    darkTheme: Boolean = true, // Default to WhatsApp Dark theme for premium look
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = if (darkTheme) WhatsAppDarkSurface.toArgb() else WhatsAppGreen.toArgb()
                window.navigationBarColor = if (darkTheme) WhatsAppDarkSurface.toArgb() else Color.White.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
