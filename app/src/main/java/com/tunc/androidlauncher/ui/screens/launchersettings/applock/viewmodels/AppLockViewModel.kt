package com.tunc.androidlauncher.ui.screens.launchersettings.applock.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.tunc.androidlauncher.data.AppLockManager
import com.tunc.androidlauncher.ui.screens.launchersettings.applock.models.AppLockSettings

class AppLockViewModel(application: Application) : AndroidViewModel(application) {
    private val appLockManager = AppLockManager(application.applicationContext)

    private val _appLockSettings = MutableStateFlow(loadSettings())
    val appLockSettings: StateFlow<AppLockSettings> = _appLockSettings.asStateFlow()

    private fun loadSettings(): AppLockSettings {
        return AppLockSettings(
            pin = appLockManager.getPin(),
            lockedApps = appLockManager.getLockedApps(),
            isPinSet = appLockManager.isPinSet()
        )
    }

    fun setPin(pin: String) {
        appLockManager.savePin(pin)
        _appLockSettings.value = _appLockSettings.value.copy(
            pin = pin,
            isPinSet = true
        )
    }

    fun verifyPin(inputPin: String): Boolean {
        return appLockManager.verifyPin(inputPin)
    }

    fun changePin(newPin: String) {
        appLockManager.savePin(newPin)
        _appLockSettings.value = _appLockSettings.value.copy(pin = newPin)
    }

    fun toggleAppLock(packageName: String) {
        appLockManager.toggleAppLock(packageName)
        _appLockSettings.value = _appLockSettings.value.copy(
            lockedApps = appLockManager.getLockedApps()
        )
    }

    fun isAppLocked(packageName: String): Boolean {
        return appLockManager.isAppLocked(packageName)
    }

    fun isPinSet(): Boolean {
        return appLockManager.isPinSet()
    }

    fun getAppLockManager(): AppLockManager {
        return appLockManager
    }
}
