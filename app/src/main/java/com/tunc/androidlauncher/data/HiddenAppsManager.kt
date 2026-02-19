package com.tunc.androidlauncher.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HiddenAppsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("hidden_apps_prefs", Context.MODE_PRIVATE)

    private val _hiddenAppsFlow = MutableStateFlow<Set<String>>(emptySet())
    val hiddenAppsFlow: StateFlow<Set<String>> = _hiddenAppsFlow.asStateFlow()

    companion object {
        private const val KEY_HIDDEN_APPS = "hidden_apps"
    }

    init {
        _hiddenAppsFlow.value = getHiddenApps()
    }

    fun addHiddenApp(packageName: String) {
        val hiddenApps = getHiddenApps().toMutableSet()
        hiddenApps.add(packageName)
        saveHiddenApps(hiddenApps)
        _hiddenAppsFlow.value = hiddenApps
    }

    fun removeHiddenApp(packageName: String) {
        val hiddenApps = getHiddenApps().toMutableSet()
        hiddenApps.remove(packageName)
        saveHiddenApps(hiddenApps)
        _hiddenAppsFlow.value = hiddenApps
    }

    fun isAppHidden(packageName: String): Boolean {
        return getHiddenApps().contains(packageName)
    }

    fun getHiddenApps(): Set<String> {
        val appsString = prefs.getString(KEY_HIDDEN_APPS, "") ?: ""
        return if (appsString.isEmpty()) emptySet() else appsString.split(",").toSet()
    }

    private fun saveHiddenApps(apps: Set<String>) {
        prefs.edit().putString(KEY_HIDDEN_APPS, apps.joinToString(",")).apply()
    }
}
