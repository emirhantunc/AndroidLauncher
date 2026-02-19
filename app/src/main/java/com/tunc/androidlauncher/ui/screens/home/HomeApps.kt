package com.tunc.androidlauncher.ui.screens.home

import android.content.Context
import com.tunc.androidlauncher.core.models.AppInfo
import com.tunc.androidlauncher.data.AppManager
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
            val appManager = AppManager.getInstance(context)
            appManager.loadApps()

            val phone = appManager.findApp(listOf("dialer", "phone", "call", "telefon"))
            val sms = appManager.findApp(listOf("message", "sms", "messaging", "mesaj"))
            val browser = appManager.findApp(listOf("chrome", "browser", "internet", "web"))
            val camera = appManager.findApp(listOf("camera", "kamera", "foto"))

            val selectedApps = listOfNotNull(phone, sms, browser, camera)

            cachedGridApps = selectedApps
            cachedDockApps = selectedApps

            isLoaded = true
        }
    }
}

