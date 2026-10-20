package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SpotifyColorScheme = darkColorScheme(
    primary = SpotifyGreen,
    onPrimary = Color.Black,
    primaryContainer = SpotifyGreen.copy(alpha = 0.2f),
    onPrimaryContainer = SpotifyGreenBright,
    secondary = SpotifyGreenBright,
    onSecondary = Color.Black,
    secondaryContainer = SpotifyCardElevated,
    onSecondaryContainer = SpotifyWhite,
    tertiary = SpotifyTextSecondary,
    onTertiary = SpotifyWhite,
    background = SpotifyBlack,
    onBackground = SpotifyWhite,
    surface = SpotifyCard,
    onSurface = SpotifyWhite,
    surfaceVariant = SpotifyCardElevated,
    onSurfaceVariant = SpotifyTextSecondary,
    outline = SpotifyBorder,
    outlineVariant = SpotifyDivider
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = SpotifyBlack.toArgb()
                window.navigationBarColor = SpotifyBlack.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = SpotifyColorScheme,
        typography = Typography,
        content = content
    )
}
