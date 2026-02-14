package com.tunc.androidlauncher.ui.screens.launchersettings.applock.models

data class AppLockSettings(
    val pin: String = "",
    val lockedApps: Set<String> = emptySet(),
    val isPinSet: Boolean = false
)
