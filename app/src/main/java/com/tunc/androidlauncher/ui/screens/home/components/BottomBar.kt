package com.tunc.androidlauncher.ui.screens.home.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tunc.androidlauncher.core.models.AppInfo
import com.tunc.androidlauncher.data.AppLockManager
import com.tunc.androidlauncher.ui.components.NotificationBadge
import com.tunc.androidlauncher.ui.screens.launchersettings.applock.components.PinVerificationDialog
import kotlin.collections.forEach

@Composable
fun BottomBar(
    apps: List<AppInfo?>,
    context: Context,
    appLockManager: AppLockManager? = null,
    surface: Color = MaterialTheme.colorScheme.surface,
    colorBorder: Color = MaterialTheme.colorScheme.outlineVariant
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(surface.copy(alpha = 0.4f))
            .border(1.dp, colorBorder.copy(alpha = 0.5f), RoundedCornerShape(32.dp))
            .padding(vertical = 16.dp, horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        apps.forEach { app ->
            BottomIcon(app, context, appLockManager)
        }
    }
}

@Composable
private fun BottomIcon(app: AppInfo?, context: Context, appLockManager: AppLockManager?) {
    if (app == null) {
        Spacer(modifier = Modifier.size(24.dp))
        return
    }

    var showPinDialog by remember { mutableStateOf(false) }
    val isLocked = appLockManager?.isAppLocked(app.packageName) == true && appLockManager.isPinSet()

    Box(contentAlignment = Alignment.Center) {
        app.icon?.let { icon ->
            AsyncImage(
                model = icon,
                contentDescription = app.name,
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        if (isLocked) {
                            showPinDialog = true
                        } else {
                            val launchIntent =
                                context.packageManager.getLaunchIntentForPackage(app.packageName)
                            launchIntent?.let { context.startActivity(it) }
                        }
                    }
            )
        }

        if (app.notificationCount > 0) {
            NotificationBadge(
                count = app.notificationCount,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }

    if (showPinDialog && appLockManager != null) {
        PinVerificationDialog(
            onDismiss = { showPinDialog = false },
            onPinVerified = {
                showPinDialog = false
                val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                launchIntent?.let { context.startActivity(it) }
            },
            verifyPin = { pin -> appLockManager.verifyPin(pin) }
        )
    }
}