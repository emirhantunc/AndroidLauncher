package com.tunc.androidlauncher.ui.screens.home.components

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tunc.androidlauncher.core.models.AppInfo
import com.tunc.androidlauncher.core.toBitmap
import com.tunc.androidlauncher.ui.screens.home.ColorBorder
import com.tunc.androidlauncher.ui.screens.home.ColorSurfaceDark
import kotlin.collections.forEach

@Composable
fun BottomBar(apps: List<AppInfo?>, context: Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(ColorSurfaceDark.copy(alpha = 0.4f))
            .border(1.dp, ColorBorder.copy(alpha = 0.5f), RoundedCornerShape(32.dp))
            .padding(vertical = 16.dp, horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        apps.forEach { app ->
            BottomIcon(app, context)
        }
    }
}

@Composable
private fun BottomIcon(app: AppInfo?, context: Context) {
    if (app == null) {
        Spacer(modifier = Modifier.size(24.dp))
        return
    }

    app.icon?.let {icon->
        AsyncImage(
            model = icon,
            contentDescription = app.name,
            modifier = Modifier
                .size(28.dp)
                .clickable {
                    val launchIntent =
                        context.packageManager.getLaunchIntentForPackage(app.packageName)
                    launchIntent?.let { context.startActivity(it) }
                }
        )
    }
}