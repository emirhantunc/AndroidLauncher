package com.tunc.androidlauncher

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.tunc.androidlauncher.data.ThemeMode
import com.tunc.androidlauncher.navigation.Screen
import com.tunc.androidlauncher.ui.LauncherMainScreen
import com.tunc.androidlauncher.ui.screens.launchersettings.LauncherSettings
import com.tunc.androidlauncher.ui.screens.launchersettings.applock.AppLockSettings
import com.tunc.androidlauncher.ui.screens.themesettings.ThemeSettings

@Composable
fun AppNavigation(
    navController: NavHostController,
    innerPadding: PaddingValues,
    onThemeChanged: (ThemeMode) -> Unit = { }
) {

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            LauncherMainScreen(
                innerPadding = innerPadding,
                onNavigateToSettings = {
                    navController.navigate(Screen.SettingsGraph.route)
                }
            )
        }

        navigation(
            startDestination = Screen.Settings.Menu.route,
            route = Screen.SettingsGraph.route
        ) {
            composable(Screen.Settings.Menu.route) {
                LauncherSettings(
                    innerPadding = innerPadding,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onSettingClicked = { id ->
                        when (id) {
                            "app_lock" -> navController.navigate(Screen.Settings.AppLock.route)
                            "theme" -> navController.navigate(Screen.Settings.Theme.route)
                        }
                    }
                )
            }

            composable(Screen.Settings.AppLock.route) {
                AppLockSettings(
                    innerPadding = innerPadding,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Settings.Theme.route) {
                ThemeSettings(
                    innerPadding = innerPadding,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onThemeChange = { newMode ->
                        onThemeChanged(newMode)
                    }
                )
            }
        }
    }

}