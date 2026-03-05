package com.tunc.androidlauncher.ui.screens.home.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tunc.androidlauncher.core.models.AppInfo
import com.tunc.androidlauncher.data.AppManager
import com.tunc.androidlauncher.data.HiddenAppsManager
import com.tunc.androidlauncher.data.LauncherMode
import com.tunc.androidlauncher.data.LayoutManager
import com.tunc.androidlauncher.ui.screens.home.HomeApps
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch


class HomeViewModel : ViewModel() {
    private val _gridApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val gridApps = _gridApps.asStateFlow()

    private var isObserving = false

    fun loadApps(context: Context) {
        viewModelScope.launch {
            val layoutManager = LayoutManager(context)
            val launcherMode = layoutManager.getLauncherMode()

            if (launcherMode == LauncherMode.HOME_GRID) {
                // HOME_GRID modunda tüm uygulamaları yükle
                val appManager = AppManager.getInstance(context)
                val hiddenAppsManager = HiddenAppsManager(context)
                appManager.loadApps()

                // İlk kez yükleme
                val allApps = appManager.allApps.value
                val hiddenPackages = hiddenAppsManager.hiddenAppsFlow.value

                _gridApps.value = allApps
                    .filter { !hiddenPackages.contains(it.packageName) }
                    .sortedBy { it.name.lowercase() }

                // Flow'u dinle - uygulama eklenince/silinince otomatik güncelle
                if (!isObserving) {
                    isObserving = true
                    viewModelScope.launch {
                        combine(
                            appManager.allApps,
                            hiddenAppsManager.hiddenAppsFlow
                        ) { apps, hidden ->
                            apps.filter { !hidden.contains(it.packageName) }
                                .sortedBy { it.name.lowercase() }
                        }.collect { filteredApps ->
                            _gridApps.value = filteredApps
                        }
                    }
                }
            } else {
                // APP_DRAWER modunda sadece belirli uygulamaları göster
                HomeApps.loadAppsIfNeeded(context)
                _gridApps.value = HomeApps.cachedGridApps
            }
        }
    }
}