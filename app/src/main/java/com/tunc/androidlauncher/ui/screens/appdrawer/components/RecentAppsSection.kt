package com.tunc.androidlauncher.ui.screens.appdrawer.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tunc.androidlauncher.core.models.AppInfo
import com.tunc.androidlauncher.data.AppLockManager

@Composable
fun RecentAppsSection(
    recentApps: List<AppInfo>,
    appLockManager: AppLockManager,
    iconSize: Int = 40,
    onSurfaceVariant: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    titleMedium: TextStyle = MaterialTheme.typography.titleMedium
) {
    if (recentApps.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Recent Apps",
                color = onSurfaceVariant,
                style = titleMedium,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(recentApps) { app ->
                    AppItem(
                        app = app,
                        appLockManager = appLockManager,
                        iconSize = iconSize
                    )
                }
            }
        }
    }
}
