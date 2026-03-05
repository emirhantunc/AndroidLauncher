package com.tunc.androidlauncher.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object AppDrawer : Screen("app_drawer")
    data object SettingsGraph : Screen("settings_graph_root")

    sealed class Settings(route: String) : Screen(route) {
        data object Menu : Settings("settings_menu")

        data object AppLock : Settings("settings_app_lock")
        data object Theme : Settings("settings_theme")
        data object HiddenApps : Settings("settings_hidden_apps")
        data object Layout : Settings("settings_layout")
        data object AppCustomization : Settings("settings_app_customization")
        data object Language : Settings("settings_language")
    }
}
