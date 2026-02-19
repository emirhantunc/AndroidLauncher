package com.tunc.androidlauncher.data

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import coil.imageLoader
import coil.request.ImageRequest
import com.tunc.androidlauncher.core.models.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class AppManager private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: AppManager? = null

        fun getInstance(context: Context): AppManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    private val customizationManager = AppCustomizationManager(context)

    private val _allApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val allApps = _allApps.asStateFlow()

    private var isLoaded = false

    suspend fun loadApps(forceReload: Boolean = false) {
        if (isLoaded && !forceReload) return

        withContext(Dispatchers.IO) {
            val packageManager = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfoList = packageManager.queryIntentActivities(intent, 0)

            val customizations = customizationManager.getAllCustomizations().firstOrNull() ?: emptyList()
            val customizationMap = customizations.associateBy { it.packageName }

            val appList = resolveInfoList.mapNotNull { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName
                if (packageName == context.packageName) return@mapNotNull null

                val defaultName = resolveInfo.loadLabel(packageManager).toString()
                val defaultIcon = resolveInfo.loadIcon(packageManager)

                val customization = customizationMap[packageName]

                val finalName = customization?.customName ?: defaultName
                val finalIcon = if (customization?.customIconUri != null) {
                    loadIconFromUri(customization.customIconUri)
                } else {
                    defaultIcon
                }

                AppInfo(
                    name = finalName,
                    packageName = packageName,
                    icon = finalIcon,
                    label = finalName.firstOrNull()?.uppercase() ?: "#"
                )
            }

            _allApps.value = appList.sortedBy { it.label.lowercase() }
            isLoaded = true
        }
    }

    private suspend fun loadIconFromUri(uriString: String): Drawable? {
        return try {
            withContext(Dispatchers.IO) {
                val uri = Uri.parse(uriString)
                val request = ImageRequest.Builder(context)
                    .data(uri)
                    .build()

                val result = context.imageLoader.execute(request)
                result.drawable
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateAppCustomization(packageName: String, iconUri: String?, name: String?) {
        withContext(Dispatchers.IO) {
            if (iconUri != null || name != null) {
                customizationManager.saveCustomization(packageName, iconUri, name)
            } else {
                customizationManager.removeCustomization(packageName)
            }
            loadApps(forceReload = true)
        }
    }

    fun getApp(packageName: String): AppInfo? {
        return _allApps.value.find { it.packageName == packageName }
    }

    fun findApp(keywords: List<String>): AppInfo? {
        return _allApps.value.firstOrNull { app ->
            keywords.any { keyword ->
                app.packageName.contains(keyword, ignoreCase = true) ||
                        app.label.contains(keyword, ignoreCase = true) ||
                        app.name.contains(keyword, ignoreCase = true)
            }
        }
    }
}
