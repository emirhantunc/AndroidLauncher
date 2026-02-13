package com.tunc.androidlauncher.core

import com.tunc.androidlauncher.core.models.AppInfo

fun findApp(apps: List<AppInfo>, keywords: List<String>): AppInfo? {
    return apps.firstOrNull { app ->
        keywords.any { keyword ->
            app.packageName.contains(keyword, ignoreCase = true) ||
                    app.label.contains(keyword, ignoreCase = true)
        }
    }
}