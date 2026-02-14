package com.tunc.androidlauncher.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AppDrawer : Screen("app_drawer")
    object LauncherSettings : Screen("launcher_settings")
    object AppLockSettings : Screen("app_lock_settings")
}
