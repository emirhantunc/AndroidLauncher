package com.tunc.androidlauncher.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RecentAppsManager(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("recent_apps_prefs", Context.MODE_PRIVATE)

    private val _recentAppsFlow = MutableStateFlow<List<String>>(emptyList())
    val recentAppsFlow: StateFlow<List<String>> = _recentAppsFlow.asStateFlow()

    companion object {
        private const val KEY_RECENT_APPS = "recent_apps"
        private const val MAX_RECENT_APPS = 4
    }
    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == KEY_RECENT_APPS) {
            loadAppsFromPrefs()
        }
    }

    init {
        prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
        loadAppsFromPrefs()
    }

    fun addRecentApp(packageName: String) {
        if (packageName == context.packageName) return
        val currentList = _recentAppsFlow.value.toMutableList()
        currentList.remove(packageName)
        currentList.add(0, packageName)
        val trimmedList = currentList.take(MAX_RECENT_APPS)
        prefs.edit().putString(KEY_RECENT_APPS, trimmedList.joinToString(",")).apply()
    }

    private fun loadAppsFromPrefs() {
        val appsString = prefs.getString(KEY_RECENT_APPS, "") ?: ""
        if (appsString.isEmpty()) {
            _recentAppsFlow.value = emptyList()
        } else {
            _recentAppsFlow.value = appsString.split(",")
        }
    }
}
