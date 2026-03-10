package com.tunc.androidlauncher.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tunc.androidlauncher.core.models.AppInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.bottomBarDataStore by preferencesDataStore(name = "bottom_bar_prefs")

class BottomBarManager(private val context: Context) {

    private val bottomBarAppsKey = stringPreferencesKey("bottom_bar_apps")

    val bottomBarAppsFlow: Flow<List<String>> = context.bottomBarDataStore.data
        .map { preferences ->
            val appsString = preferences[bottomBarAppsKey] ?: ""
            if (appsString.isEmpty()) emptyList() else appsString.split(",")
        }

    suspend fun setBottomBarApps(packageNames: List<String>) {
        context.bottomBarDataStore.edit { preferences ->
            preferences[bottomBarAppsKey] = packageNames.take(4).joinToString(",")
        }
    }

    suspend fun addAppToBottomBar(packageName: String, position: Int) {
        context.bottomBarDataStore.edit { preferences ->
            val appsString = preferences[bottomBarAppsKey] ?: ""
            val currentApps = if (appsString.isEmpty()) mutableListOf() else appsString.split(",").toMutableList()

            // Eğer uygulama zaten varsa, önce kaldır
            currentApps.remove(packageName)

            // Belirtilen pozisyona ekle
            if (position >= currentApps.size) {
                currentApps.add(packageName)
            } else {
                currentApps.add(position, packageName)
            }

            // Maksimum 4 uygulama
            preferences[bottomBarAppsKey] = currentApps.take(4).joinToString(",")
        }
    }

    suspend fun removeAppFromBottomBar(packageName: String) {
        context.bottomBarDataStore.edit { preferences ->
            val appsString = preferences[bottomBarAppsKey] ?: ""
            if (appsString.isNotEmpty()) {
                val currentApps = appsString.split(",").toMutableList()
                currentApps.remove(packageName)
                preferences[bottomBarAppsKey] = currentApps.joinToString(",")
            }
        }
    }

    fun getRandomBottomBarApps(allApps: List<AppInfo>, count: Int = 4): List<String> {
        return allApps.shuffled().take(count).map { it.packageName }
    }
}
