package com.tunc.androidlauncher.core

import android.content.Context
import android.content.Intent
import com.tunc.androidlauncher.core.models.AppInfo

fun getInstalledApps(context: Context): List<AppInfo> {
    val packageManager = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN, null).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    val resolveInfoList = packageManager.queryIntentActivities(intent, 0)
    val appList = resolveInfoList.map { resolveInfo ->
        val appName = resolveInfo.loadLabel(packageManager).toString()
        AppInfo(
            name = appName,
            packageName = resolveInfo.activityInfo.packageName,
            icon = resolveInfo.loadIcon(packageManager),
            label = appName.firstOrNull()?.uppercase() ?: "#"
        )
    }
    val filteredList = appList.filter { it.packageName != context.packageName }

    return filteredList.sortedBy { it.label.lowercase() }
}