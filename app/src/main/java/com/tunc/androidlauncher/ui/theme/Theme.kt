package com.tunc.androidlauncher.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext



private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    background = BgDark,
    onBackground = Slate100,
    surface = SurfaceDark,
    onSurface = TextWhite,
    onSurfaceVariant = TextGray,
    onSecondary = Color(0xFF52525B),
    outlineVariant =  Color(0xFF27272a)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    background = BgDark,
    surface = SurfaceDark,
    onSurface = TextWhite,
    onSurfaceVariant = TextGray
)

@Composable
fun AndroidLauncherTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}