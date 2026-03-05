package com.tunc.androidlauncher.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class IconSize(val displayName: String, val appDrawerSize: Int, val homeScreenSize: Int, val bottomBarSize: Int) {
    SMALL("Small", 28, 20, 24),
    MEDIUM("Medium", 36, 28, 30),
    LARGE("Large", 44, 36, 36)
}

enum class LauncherMode(val displayName: String) {
    APP_DRAWER("App Drawer"),
    HOME_GRID("Home Grid")
}

class LayoutManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("layout_prefs", Context.MODE_PRIVATE)

    private val _iconSizeFlow = MutableStateFlow(IconSize.MEDIUM)
    val iconSizeFlow: StateFlow<IconSize> = _iconSizeFlow.asStateFlow()

    private val _launcherModeFlow = MutableStateFlow(LauncherMode.APP_DRAWER)
    val launcherModeFlow: StateFlow<LauncherMode> = _launcherModeFlow.asStateFlow()

    companion object {
        private const val KEY_ICON_SIZE = "icon_size"
        private const val KEY_LAUNCHER_MODE = "launcher_mode"
    }

    init {
        _iconSizeFlow.value = getIconSize()
        _launcherModeFlow.value = getLauncherMode()
    }

    fun setIconSize(size: IconSize) {
        prefs.edit().putString(KEY_ICON_SIZE, size.name).apply()
        _iconSizeFlow.value = size
    }

    fun getIconSize(): IconSize {
        val sizeName = prefs.getString(KEY_ICON_SIZE, IconSize.MEDIUM.name) ?: IconSize.MEDIUM.name
        return try {
            IconSize.valueOf(sizeName)
        } catch (_: IllegalArgumentException) {
            IconSize.MEDIUM
        }
    }

    fun setLauncherMode(mode: LauncherMode) {
        prefs.edit().putString(KEY_LAUNCHER_MODE, mode.name).apply()
        _launcherModeFlow.value = mode
    }

    fun getLauncherMode(): LauncherMode {
        val modeName = prefs.getString(KEY_LAUNCHER_MODE, LauncherMode.APP_DRAWER.name) ?: LauncherMode.APP_DRAWER.name
        return try {
            LauncherMode.valueOf(modeName)
        } catch (_: IllegalArgumentException) {
            LauncherMode.APP_DRAWER
        }
    }
}
