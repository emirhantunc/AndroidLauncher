package com.tunc.androidlauncher

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.tunc.androidlauncher.navigation.Screen
import com.tunc.androidlauncher.ui.LauncherMainScreen
import com.tunc.androidlauncher.ui.screens.launchersettings.applock.AppLockSettings
import com.tunc.androidlauncher.ui.screens.launchersettings.LauncherSettings
import com.tunc.androidlauncher.ui.theme.AndroidLauncherTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidLauncherTheme {
                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route
                    ) {
                        composable(Screen.Home.route) {
                            LauncherMainScreen(
                                innerPadding = innerPadding,
                                onNavigateToSettings = {
                                    navController.navigate(Screen.LauncherSettings.route)
                                }
                            )
                        }

                        composable(Screen.LauncherSettings.route) {
                            LauncherSettings(
                                innerPadding = innerPadding,
                                onNavigateToAppLock = {
                                    navController.navigate(Screen.AppLockSettings.route)
                                },
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(Screen.AppLockSettings.route) {
                            AppLockSettings(
                                innerPadding = innerPadding,
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
