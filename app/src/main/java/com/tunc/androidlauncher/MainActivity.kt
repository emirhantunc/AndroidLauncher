package com.tunc.androidlauncher

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.tunc.androidlauncher.data.ThemeManager
import com.tunc.androidlauncher.data.ThemeMode
import com.tunc.androidlauncher.navigation.Screen
import com.tunc.androidlauncher.ui.LauncherMainScreen
import com.tunc.androidlauncher.ui.screens.launchersettings.applock.AppLockSettings
import com.tunc.androidlauncher.ui.screens.launchersettings.LauncherSettings
import com.tunc.androidlauncher.ui.screens.themesettings.ThemeSettings
import com.tunc.androidlauncher.ui.theme.AndroidLauncherTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeManager = remember { ThemeManager(this) }
            var themeMode by remember { mutableStateOf(themeManager.getThemeMode()) }
            val systemInDarkTheme = isSystemInDarkTheme()

            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> systemInDarkTheme
            }

            AndroidLauncherTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) { innerPadding ->
                    AppNavigation(
                        navController = navController,
                        innerPadding = innerPadding
                    ) { newThemeMode ->
                        themeMode = newThemeMode
                        themeManager.saveThemeMode(newThemeMode)
                    }
                }
            }
        }
    }
}
