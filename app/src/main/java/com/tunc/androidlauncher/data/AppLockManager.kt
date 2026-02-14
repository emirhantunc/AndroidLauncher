package com.tunc.androidlauncher.data

import android.content.Context
import android.content.SharedPreferences

class AppLockManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "app_lock_prefs",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_PIN = "app_lock_pin"
        private const val KEY_LOCKED_APPS = "locked_apps"
        private const val KEY_IS_PIN_SET = "is_pin_set"
    }

    fun savePin(pin: String) {
        prefs.edit().apply {
            putString(KEY_PIN, pin)
            putBoolean(KEY_IS_PIN_SET, true)
            apply()
        }
    }

    fun getPin(): String {
        return prefs.getString(KEY_PIN, "") ?: ""
    }

    fun isPinSet(): Boolean {
        return prefs.getBoolean(KEY_IS_PIN_SET, false)
    }

    fun verifyPin(inputPin: String): Boolean {
        return getPin() == inputPin
    }

    fun saveLockedApps(packageNames: Set<String>) {
        prefs.edit().apply {
            putStringSet(KEY_LOCKED_APPS, packageNames)
            apply()
        }
    }

    fun getLockedApps(): Set<String> {
        return prefs.getStringSet(KEY_LOCKED_APPS, emptySet()) ?: emptySet()
    }

    fun isAppLocked(packageName: String): Boolean {
        return getLockedApps().contains(packageName)
    }

    fun toggleAppLock(packageName: String) {
        val currentLocked = getLockedApps().toMutableSet()
        if (currentLocked.contains(packageName)) {
            currentLocked.remove(packageName)
        } else {
            currentLocked.add(packageName)
        }
        saveLockedApps(currentLocked)
    }

    fun clearAllData() {
        prefs.edit().clear().apply()
    }
}
