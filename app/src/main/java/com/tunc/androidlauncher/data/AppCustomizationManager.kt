package com.tunc.androidlauncher.data

import android.content.Context
import com.tunc.androidlauncher.data.database.AppCustomization
import com.tunc.androidlauncher.data.database.AppDatabase
import kotlinx.coroutines.flow.Flow

class AppCustomizationManager(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val dao = database.appCustomizationDao()

    fun getCustomization(packageName: String): Flow<AppCustomization?> {
        return dao.getCustomization(packageName)
    }

    fun getAllCustomizations(): Flow<List<AppCustomization>> {
        return dao.getAllCustomizations()
    }

    suspend fun setCustomIcon(packageName: String, iconUri: String) {
        dao.insertCustomization(
            AppCustomization(
                packageName = packageName,
                customIconUri = iconUri
            )
        )
    }

    suspend fun setCustomName(packageName: String, name: String) {
        dao.insertCustomization(
            AppCustomization(
                packageName = packageName,
                customName = name
            )
        )
    }

    suspend fun updateCustomIcon(packageName: String, iconUri: String?) {
        dao.updateIcon(packageName, iconUri)
    }

    suspend fun updateCustomName(packageName: String, name: String?) {
        dao.updateName(packageName, name)
    }

    suspend fun removeCustomization(packageName: String) {
        dao.deleteCustomization(packageName)
    }

    suspend fun saveCustomization(packageName: String, iconUri: String?, name: String?) {
        dao.insertCustomization(
            AppCustomization(
                packageName = packageName,
                customIconUri = iconUri,
                customName = name
            )
        )
    }
}
