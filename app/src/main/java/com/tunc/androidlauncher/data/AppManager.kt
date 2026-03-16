package com.tunc.androidlauncher.data

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import coil.imageLoader
import coil.request.ImageRequest
import com.tunc.androidlauncher.core.models.AppInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.provider.Settings

class AppManager private constructor(private val context: Context) {

    companion object {
        private const val TAG = "AppManager"

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
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _allApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val allApps = _allApps.asStateFlow()

    private var isLoaded = false

    // BroadcastReceiver: Uygulama eklendiğinde/silindiğinde otomatik güncelleme
    private val packageChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(TAG, "BroadcastReceiver.onReceive called")
            Log.d(TAG, "Intent: ${intent?.action}")

            when (intent?.action) {
                Intent.ACTION_PACKAGE_ADDED,
                Intent.ACTION_PACKAGE_REMOVED,
                Intent.ACTION_PACKAGE_REPLACED -> {
                    val packageName = intent.data?.schemeSpecificPart
                    Log.d(TAG, "✅ Package changed: ${intent.action}, package: $packageName")

                    // Uygulamaları yeniden yükle
                    scope.launch {
                        Log.d(TAG, "🔄 Starting app reload...")
                        loadApps(forceReload = true)
                        Log.d(TAG, "✅ App reload completed. Total apps: ${_allApps.value.size}")
                    }
                }
                else -> {
                    Log.d(TAG, "⚠️ Unknown action: ${intent?.action}")
                }
            }
        }
    }

    init {
        // BroadcastReceiver'ı kaydet
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }

        // Android 13+ (API 33+) için flag gerekiyor
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                packageChangeReceiver,
                filter,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            context.registerReceiver(packageChangeReceiver, filter)
        }

        Log.d(TAG, "Package change receiver registered (SDK: ${android.os.Build.VERSION.SDK_INT})")
    }

    fun unregister() {
        try {
            context.unregisterReceiver(packageChangeReceiver)
            Log.d(TAG, "Package change receiver unregistered")
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering receiver", e)
        }
    }

    suspend fun loadApps(forceReload: Boolean = false) {
        Log.d(TAG, "loadApps called (forceReload: $forceReload, isLoaded: $isLoaded)")

        if (isLoaded && !forceReload) {
            Log.d(TAG, "Apps already loaded, skipping")
            return
        }

        Log.d(TAG, "Loading apps from package manager...")

        withContext(Dispatchers.IO) {
            val packageManager = context.packageManager
            val intent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfoList = packageManager.queryIntentActivities(intent, 0)

            Log.d(TAG, "Found ${resolveInfoList.size} launchable apps")

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

            val sortedList = appList.sortedBy { it.label.lowercase() }
            _allApps.value = sortedList
            isLoaded = true

            Log.d(TAG, "✅ Apps loaded and updated: ${sortedList.size} apps")
            Log.d(TAG, "📱 Sample apps: ${sortedList.take(5).map { it.name }}")
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

    fun getMostUsedApps(context: Context): List<UsageStats> {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val endTime = System.currentTimeMillis()
        val startTime = endTime - (1000 * 60 * 60 * 24 * 7)

        val stats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST,
            startTime,
            endTime
        )
        return stats
            .filter { it.totalTimeInForeground > 0 }
            .sortedByDescending { it.totalTimeInForeground }
            .take(4)
    }
    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun isUsageStatsPermissionGranted(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun requestUsageStatsPermission(context: Context) {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
        }
        context.startActivity(intent)
    }
}
