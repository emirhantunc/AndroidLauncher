package com.tunc.androidlauncher.core.models

import android.graphics.drawable.Drawable


data class AppInfo(
    val name: String,
    val label: String,
    val packageName: String,
    val icon: Drawable?
)