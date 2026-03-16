package com.tunc.androidlauncher.ui.screens.home

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import com.tunc.androidlauncher.core.models.AppInfo
import com.tunc.androidlauncher.data.AppManager
import com.tunc.androidlauncher.ui.screens.home.HomeApps.cachedDockApps
import com.tunc.androidlauncher.ui.screens.home.viewmodels.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object HomeApps {
    var cachedGridApps: List<AppInfo> = emptyList()
        private set

    var cachedDockApps: List<AppInfo> = emptyList()
        private set


    private var isLoaded = false


    suspend fun getMostUsedApps(context: Context, forceRefresh: Boolean = false) {
        if (isLoaded && !forceRefresh) return

        val appManager = AppManager.getInstance(context)
        if (!appManager.hasUsageStatsPermission(context)) return

        withContext(Dispatchers.IO) {
            // 1. Cihazdaki tüm yüklü uygulamaları belleğe al (getApp'in null dönmemesi için)
            appManager.loadApps()

            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val endTime = System.currentTimeMillis()
            val startTime = endTime - (1000 * 60 * 60 * 24 * 7) // Son 7 gün

            val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, startTime, endTime)

            if (stats.isNullOrEmpty()) {
                appManager.loadApps()
                appManager.allApps.collect {
                        if (it.isNotEmpty()) {
                            cachedGridApps = it.take(4)
                            cachedDockApps = it.take(4)
                            isLoaded = true
                        }
                }
                return@withContext
            }

            val sortedPackages = stats.groupBy { it.packageName }
                .mapValues { entry ->
                    entry.value.sumOf { it.totalTimeInForeground }
                }
                .filter { it.key != context.packageName }
                .toList()
                .sortedByDescending { it.second }
                .take(10)

            val finalApps = sortedPackages.mapNotNull { (pkgName, _) ->
                appManager.getApp(pkgName)
            }.take(4)

            if (finalApps.isNotEmpty()) {
                cachedGridApps = finalApps
                cachedDockApps = finalApps // Dock için de aynısını kullanıyorsan
                isLoaded = true
                println("DEBUG_LAUNCHER: cachedGridApps başarıyla güncellendi. Sayı: ${finalApps.size}")
            } else {
                appManager.loadApps()
                appManager.allApps.collect {
                    if (it.isNotEmpty()) {
                        cachedGridApps = it.take(4)
                        cachedDockApps = it.take(4)
                        isLoaded = true
                    }
                }
                isLoaded = true
            }
        }
    }
}

