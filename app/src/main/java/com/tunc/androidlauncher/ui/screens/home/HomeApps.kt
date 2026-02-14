package com.tunc.androidlauncher.ui.screens.home

import android.content.Context
import com.tunc.androidlauncher.core.findApp
import com.tunc.androidlauncher.core.getInstalledApps
import com.tunc.androidlauncher.core.models.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


object HomeApps {
    var cachedGridApps: List<AppInfo> = emptyList()
        private set
    var cachedDockApps: List<AppInfo> = emptyList()
        private set

    private var isLoaded = false

    suspend fun loadAppsIfNeeded(context: Context) {
        if (isLoaded) return

        withContext(Dispatchers.IO) {
            val allApps = getInstalledApps(context)

            val phone = findApp(allApps, listOf("dialer", "phone", "call", "telefon"))
            val sms = findApp(allApps, listOf("message", "sms", "messaging", "mesaj"))
            val browser = findApp(allApps, listOf("chrome", "browser", "internet", "web"))
            val camera = findApp(allApps, listOf("camera", "kamera", "foto"))

            val selectedApps = listOfNotNull(phone, sms, browser, camera)

            cachedGridApps = selectedApps
            cachedDockApps = selectedApps

            isLoaded = true
        }
    }
}