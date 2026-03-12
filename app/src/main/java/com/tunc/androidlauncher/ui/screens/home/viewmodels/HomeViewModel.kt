package com.tunc.androidlauncher.ui.screens.home.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tunc.androidlauncher.core.models.AppInfo
import com.tunc.androidlauncher.data.AppManager
import com.tunc.androidlauncher.data.AppPlacementManager
import com.tunc.androidlauncher.data.HiddenAppsManager
import com.tunc.androidlauncher.data.LauncherMode
import com.tunc.androidlauncher.data.LayoutManager
import com.tunc.androidlauncher.ui.screens.home.HomeApps
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch


class HomeViewModel : ViewModel() {
    /** Grid'de gösterilecek uygulamalar (placement index sırasına göre, index >= 4) */
    private val _gridApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val gridApps = _gridApps.asStateFlow()

    /** Bottom bar uygulamaları (placement index sırasına göre, index 0-3) */
    private val _bottomBarApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val bottomBarApps = _bottomBarApps.asStateFlow()

    private var isObserving = false

    fun loadApps(context: Context) {
        viewModelScope.launch {
            val layoutManager = LayoutManager(context)
            val launcherMode = layoutManager.getLauncherMode()

            if (launcherMode == LauncherMode.HOME_GRID) {
                val appManager = AppManager.getInstance(context)
                val hiddenAppsManager = HiddenAppsManager(context)
                val placementManager = AppPlacementManager.getInstance(context)
                appManager.loadApps()

                // İlk kurulum: placement tablosu boşsa dolduralım
                val visibleApps = appManager.allApps.value
                    .filter { !hiddenAppsManager.hiddenAppsFlow.value.contains(it.packageName) }
                placementManager.initializeIfNeeded(visibleApps)

                // Flow'u dinle
                if (!isObserving) {
                    isObserving = true
                    viewModelScope.launch {
                        combine(
                            appManager.allApps,
                            hiddenAppsManager.hiddenAppsFlow,
                            placementManager.allPlacementsFlow
                        ) { apps, hidden, placements ->
                            val visibleApps2 = apps.filter { !hidden.contains(it.packageName) }
                            val placementMap = placements.associateBy { it.packageName }

                            // Bottom bar (index 0-3)
                            val bottomBar = placements
                                .filter { it.sortIndex in 0..AppPlacementManager.BOTTOM_BAR_MAX_INDEX }
                                .sortedBy { it.sortIndex }
                                .mapNotNull { placement ->
                                    visibleApps2.find { it.packageName == placement.packageName }
                                }

                            // Grid (index >= 4)
                            val grid = placements
                                .filter { it.sortIndex >= AppPlacementManager.GRID_START_INDEX }
                                .sortedBy { it.sortIndex }
                                .mapNotNull { placement ->
                                    visibleApps2.find { it.packageName == placement.packageName }
                                }

                            // Placement'ı olmayan yeni uygulamaları da grid sonuna ekle
                            val placedPackages = placementMap.keys
                            val unplaced = visibleApps2.filter { it.packageName !in placedPackages }

                            Triple(bottomBar, grid + unplaced, visibleApps2)
                        }.collect { (bottomBar, grid, allVisible) ->
                            _bottomBarApps.value = bottomBar
                            _gridApps.value = grid

                            // Yeni uygulamaları sync et
                            val placementManager2 = AppPlacementManager.getInstance(context)
                            placementManager2.initializeIfNeeded(allVisible)
                        }
                    }
                }
            } else {
                // APP_DRAWER modunda sadece belirli uygulamaları göster
                HomeApps.loadAppsIfNeeded(context)
                _gridApps.value = HomeApps.cachedGridApps
                _bottomBarApps.value = HomeApps.cachedDockApps
            }
        }
    }
}