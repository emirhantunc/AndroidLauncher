package com.tunc.androidlauncher.core

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.tunc.androidlauncher.core.models.AppInfo
import com.tunc.androidlauncher.data.AppCustomizationManager
import com.tunc.androidlauncher.data.database.AppCustomization
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun AppInfo.applyCustomization(
    context: Context,
    customization: AppCustomization?
): AppInfo {
    if (customization == null) return this

    val customIcon = customization.customIconUri?.let { uri ->
        loadDrawableFromUri(context, uri)
    }

    return this.copy(
        name = customization.customName ?: this.name,
        icon = customIcon ?: this.icon
    )
}

private suspend fun loadDrawableFromUri(context: Context, uriString: String): Drawable? {
    return withContext(Dispatchers.IO) {
        try {
            val imageLoader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(Uri.parse(uriString))
                .build()

            val result = imageLoader.execute(request)
            if (result is SuccessResult) {
                result.drawable
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

suspend fun List<AppInfo>.applyCustomizations(
    context: Context,
    customizations: List<AppCustomization>
): List<AppInfo> {
    return this.map { app ->
        val customization = customizations.find { it.packageName == app.packageName }
        app.applyCustomization(context, customization)
    }
}
