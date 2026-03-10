package com.tunc.androidlauncher

import HiddenAppsSettings
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.tunc.androidlauncher.data.ThemeMode
import com.tunc.androidlauncher.navigation.Screen
import com.tunc.androidlauncher.ui.LauncherMainScreen
import com.tunc.androidlauncher.ui.screens.launchersettings.LauncherSettings
import com.tunc.androidlauncher.ui.screens.launchersettings.applock.AppLockSettings
import com.tunc.androidlauncher.ui.screens.launchersettings.languagesettings.LanguageSettings
import com.tunc.androidlauncher.ui.screens.launchersettings.layoutsettings.AppCustomizationSettings
import com.tunc.androidlauncher.ui.screens.launchersettings.layoutsettings.LayoutSettings
import com.tunc.androidlauncher.ui.screens.themesettings.ThemeSettings

@RequiresApi(Build.VERSION_CODES.O)
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
                    onNavigateToAppLock = {
                        navController.navigate(Screen.Settings.AppLock.route)
                    },
                    onNavigateToTheme = {
                        navController.navigate(Screen.Settings.Theme.route)
                    },
                    onNavigateToHiddenApps = {
                        navController.navigate(Screen.Settings.HiddenApps.route)
                    },
                    onNavigateToLayout = {
                        navController.navigate(Screen.Settings.Layout.route)
                    },
                    onNavigateToLanguage = {
                        navController.navigate(Screen.Settings.Language.route)
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

            composable(Screen.Settings.HiddenApps.route) {
                HiddenAppsSettings(
                    innerPadding = innerPadding,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Settings.Layout.route) {
                LayoutSettings(
                    innerPadding = innerPadding,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onNavigateToCustomization = {
                        navController.navigate(Screen.Settings.AppCustomization.route)
                    }
                )
            }

            composable(Screen.Settings.AppCustomization.route) {
                AppCustomizationSettings(
                    innerPadding = innerPadding,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Settings.Language.route) {
                LanguageSettings(
                    innerPadding = innerPadding,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }

}