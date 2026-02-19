package com.tunc.androidlauncher.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WallpaperManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("wallpaper_prefs", Context.MODE_PRIVATE)

    private val _wallpaperUriFlow = MutableStateFlow<String?>(null)
    val wallpaperUriFlow: StateFlow<String?> = _wallpaperUriFlow.asStateFlow()

    companion object {
        private const val KEY_WALLPAPER_URI = "wallpaper_uri"
    }

    init {
        _wallpaperUriFlow.value = getWallpaperUri()
    }

    fun setWallpaper(uri: String) {
        prefs.edit().putString(KEY_WALLPAPER_URI, uri).apply()
        _wallpaperUriFlow.value = uri
    }

    fun clearWallpaper() {
        prefs.edit().remove(KEY_WALLPAPER_URI).apply()
        _wallpaperUriFlow.value = null
    }

    fun getWallpaperUri(): String? {
        return prefs.getString(KEY_WALLPAPER_URI, null)
    }

    fun hasWallpaper(): Boolean {
        return getWallpaperUri() != null
    }
}
