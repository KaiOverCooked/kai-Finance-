package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class AppThemeMode {
    SYSTEM, DARK, LIGHT, AMOLED
}

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryMonochromeDark,
    onPrimary = OnPrimaryMonochromeDark,
    primaryContainer = CardDark,
    onPrimaryContainer = PureWhite,
    secondary = SilverLight,
    onSecondary = DarkCharcoal,
    background = DarkCharcoal,
    onBackground = PureWhite,
    surface = DarkCharcoal,
    onSurface = PureWhite,
    surfaceVariant = CardDark,
    onSurfaceVariant = SilverMuted,
    outline = CardBorderDark,
    outlineVariant = Color(0xFF3A3A3A)
)

private val AmoledColorScheme = darkColorScheme(
    primary = PrimaryMonochromeDark,
    onPrimary = OnPrimaryMonochromeDark,
    primaryContainer = Color(0xFF101010),
    onPrimaryContainer = PureWhite,
    secondary = SilverLight,
    onSecondary = ObsidianBlack,
    background = ObsidianBlack,
    onBackground = PureWhite,
    surface = ObsidianBlack,
    onSurface = PureWhite,
    surfaceVariant = Color(0xFF141414),
    onSurfaceVariant = SilverMuted,
    outline = Color(0xFF262626),
    outlineVariant = Color(0xFF333333)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryMonochromeLight,
    onPrimary = OnPrimaryMonochromeLight,
    primaryContainer = OffWhite,
    onPrimaryContainer = ObsidianBlack,
    secondary = DarkGrayText,
    onSecondary = PureWhite,
    background = OffWhite,
    onBackground = ObsidianBlack,
    surface = PureWhite,
    onSurface = ObsidianBlack,
    surfaceVariant = PureWhite,
    onSurfaceVariant = DarkGrayText,
    outline = CardBorderLight,
    outlineVariant = SilverLight
)

@Composable
fun KaiFinanceTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.DARK, AppThemeMode.AMOLED -> true
        AppThemeMode.LIGHT -> false
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        themeMode == AppThemeMode.AMOLED -> AmoledColorScheme
        isDark -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !isDark
                isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
