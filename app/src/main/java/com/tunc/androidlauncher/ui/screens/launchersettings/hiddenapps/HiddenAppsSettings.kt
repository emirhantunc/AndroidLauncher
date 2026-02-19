package com.tunc.androidlauncher.ui.screens.launchersettings.hiddenapps

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.tunc.androidlauncher.core.models.AppInfo
import com.tunc.androidlauncher.data.AppManager
import com.tunc.androidlauncher.data.HiddenAppsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HiddenAppsSettings(
    onBackClick: () -> Unit,
    innerPadding: PaddingValues = PaddingValues(0.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    onBackground: Color = MaterialTheme.colorScheme.onBackground,
    onSurfaceVariant: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    surface: Color = MaterialTheme.colorScheme.surface,
    primary: Color = MaterialTheme.colorScheme.primary,
    titleLarge: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.titleLarge,
    bodyMedium: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    bodySmall: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodySmall
) {
    val context = LocalContext.current
    val appManager = remember { AppManager.getInstance(context) }
    val hiddenAppsManager = remember { HiddenAppsManager(context) }
    val allApps by appManager.allApps.collectAsStateWithLifecycle()
    val hiddenPackages by hiddenAppsManager.hiddenAppsFlow.collectAsStateWithLifecycle()
    var currentTab by remember { mutableStateOf(HiddenAppsTab.ALL_APPS) }

    LaunchedEffect(Unit) {
        appManager.loadApps()
    }

    val hiddenApps = remember(hiddenPackages, allApps) {
        hiddenPackages.mapNotNull { packageName ->
            allApps.find { it.packageName == packageName }
        }
    }

    val visibleApps = remember(hiddenPackages, allApps) {
        allApps.filter { !hiddenPackages.contains(it.packageName) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(innerPadding)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = onBackground
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = "Hidden Apps",
                        style = titleLarge,
                        color = onBackground,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Hide apps from app drawer",
                        style = bodySmall,
                        color = onSurfaceVariant
                    )
                }
            }

            TabRow(
                selectedTabIndex = currentTab.ordinal,
                containerColor = backgroundColor,
                contentColor = primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = currentTab == HiddenAppsTab.ALL_APPS,
                    onClick = { currentTab = HiddenAppsTab.ALL_APPS },
                    text = {
                        Text(
                            text = "All Apps (${visibleApps.size})",
                            color = if (currentTab == HiddenAppsTab.ALL_APPS) primary else onSurfaceVariant
                        )
                    }
                )
                Tab(
                    selected = currentTab == HiddenAppsTab.HIDDEN,
                    onClick = { currentTab = HiddenAppsTab.HIDDEN },
                    text = {
                        Text(
                            text = "Hidden (${hiddenApps.size})",
                            color = if (currentTab == HiddenAppsTab.HIDDEN) primary else onSurfaceVariant
                        )
                    }
                )
            }

            when (currentTab) {
                HiddenAppsTab.ALL_APPS -> {
                    AllAppsTab(
                        apps = visibleApps,
                        onHideApp = { hiddenAppsManager.addHiddenApp(it) },
                        surface = surface,
                        onBackground = onBackground,
                        bodyMedium = bodyMedium
                    )
                }
                HiddenAppsTab.HIDDEN -> {
                    HiddenAppsTab(
                        apps = hiddenApps,
                        onUnhideApp = { hiddenAppsManager.removeHiddenApp(it) },
                        surface = surface,
                        onBackground = onBackground,
                        bodyMedium = bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun AllAppsTab(
    apps: List<AppInfo>,
    onHideApp: (String) -> Unit,
    surface: Color,
    onBackground: Color,
    bodyMedium: androidx.compose.ui.text.TextStyle
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(apps) { app ->
            AppItemRow(
                app = app,
                onActionClick = { onHideApp(app.packageName) },
                actionText = "Hide",
                surface = surface,
                onBackground = onBackground,
                bodyMedium = bodyMedium
            )
        }
    }
}

@Composable
private fun HiddenAppsTab(
    apps: List<AppInfo>,
    onUnhideApp: (String) -> Unit,
    surface: Color,
    onBackground: Color,
    bodyMedium: androidx.compose.ui.text.TextStyle
) {
    val context = LocalContext.current

    if (apps.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No hidden apps",
                style = bodyMedium,
                color = onBackground.copy(alpha = 0.6f)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(apps) { app ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(surface)
                        .clickable {
                            val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                            launchIntent?.let { context.startActivity(it) }
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                        ) {
                            app.icon?.let { icon ->
                                AsyncImage(
                                    model = icon,
                                    contentDescription = app.name,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        Text(
                            text = app.name,
                            style = bodyMedium,
                            color = onBackground,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    IconButton(
                        onClick = { onUnhideApp(app.packageName) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Unhide",
                            tint = onBackground.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppItemRow(
    app: AppInfo,
    onActionClick: () -> Unit,
    actionText: String,
    surface: Color,
    onBackground: Color,
    bodyMedium: androidx.compose.ui.text.TextStyle
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                app.icon?.let { icon ->
                    AsyncImage(
                        model = icon,
                        contentDescription = app.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Text(
                text = app.label,
                style = bodyMedium,
                color = onBackground,
                fontWeight = FontWeight.Medium
            )
        }

        TextButton(onClick = onActionClick) {
            Text(
                text = actionText,
                color = onBackground.copy(alpha = 0.7f),
                fontSize = 14.sp
            )
        }
    }
}

private enum class HiddenAppsTab {
    ALL_APPS,
    HIDDEN
}
