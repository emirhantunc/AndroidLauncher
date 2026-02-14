package com.tunc.androidlauncher.ui.screens.appdrawer.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tunc.androidlauncher.core.models.AppInfo
import com.tunc.androidlauncher.data.AppLockManager
import com.tunc.androidlauncher.ui.components.NotificationBadge
import com.tunc.androidlauncher.ui.screens.launchersettings.applock.components.PinVerificationDialog

@Composable
fun AppItem(
    app: AppInfo,
    appLockManager: AppLockManager? = null,
    bgColor: Color = MaterialTheme.colorScheme.background,
    labelSmall: TextStyle = MaterialTheme.typography.labelSmall,
    onBackGround : Color = MaterialTheme.colorScheme.onBackground,
    onSurface: Color = MaterialTheme.colorScheme.onSurface
) {
    val context = LocalContext.current
    var showPinDialog by remember { mutableStateOf(false) }
    val isLocked = appLockManager?.isAppLocked(app.packageName) == true && appLockManager.isPinSet()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(bottom = 16.dp)
            .clickable {
                if (isLocked) {
                    showPinDialog = true
                } else {
                    launchApp(context, app.packageName)
                }
            }
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            app.icon?.let { icon ->
                AsyncImage(
                    model = icon,
                    contentDescription = app.name,
                    modifier = Modifier.size(40.dp)
                )
            } ?: run {
                Text(app.label.take(1), color = onSurface, fontWeight = FontWeight.Bold)
            }

            if (app.notificationCount > 0) {
                NotificationBadge(
                    count = app.notificationCount,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = app.name,
            color = onBackGround,
            style = labelSmall,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.width(64.dp)
        )
    }

    if (showPinDialog && appLockManager != null) {
        PinVerificationDialog(
            onDismiss = { showPinDialog = false },
            onPinVerified = {
                showPinDialog = false
                launchApp(context, app.packageName)
            },
            verifyPin = { pin -> appLockManager.verifyPin(pin) }
        )
    }
}

private fun launchApp(context: Context, packageName: String) {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
    launchIntent?.let { context.startActivity(it) }
}

