package com.tunc.androidlauncher.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class IconSize(val displayName: String, val appDrawerSize: Int, val homeScreenSize: Int, val bottomBarSize: Int) {
    SMALL("Small", 36, 28, 24),
    MEDIUM("Medium", 44, 36, 30),
    LARGE("Large", 52, 44, 36)
}

class LayoutManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("layout_prefs", Context.MODE_PRIVATE)

    private val _iconSizeFlow = MutableStateFlow(IconSize.MEDIUM)
    val iconSizeFlow: StateFlow<IconSize> = _iconSizeFlow.asStateFlow()

    companion object {
        private const val KEY_ICON_SIZE = "icon_size"
    }

    init {
        _iconSizeFlow.value = getIconSize()
    }

    fun setIconSize(size: IconSize) {
        prefs.edit().putString(KEY_ICON_SIZE, size.name).apply()
        _iconSizeFlow.value = size
    }

    fun getIconSize(): IconSize {
        val sizeName = prefs.getString(KEY_ICON_SIZE, IconSize.MEDIUM.name) ?: IconSize.MEDIUM.name
        return try {
            IconSize.valueOf(sizeName)
        } catch (e: IllegalArgumentException) {
            IconSize.MEDIUM
        }
    }
}
