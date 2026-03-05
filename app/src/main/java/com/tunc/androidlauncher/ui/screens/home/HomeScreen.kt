package com.tunc.androidlauncher.ui.screens.home

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.tunc.androidlauncher.data.AppLockManager
import com.tunc.androidlauncher.data.LauncherMode
import com.tunc.androidlauncher.data.LayoutManager
import com.tunc.androidlauncher.data.WallpaperManager
import com.tunc.androidlauncher.ui.screens.home.components.BottomBar
import com.tunc.androidlauncher.ui.screens.home.components.HomeGrid
import com.tunc.androidlauncher.ui.screens.home.components.HomeSearchBar
import com.tunc.androidlauncher.ui.screens.home.components.LockScreenClock
import com.tunc.androidlauncher.ui.screens.home.viewmodels.HomeViewModel
import kotlin.collections.isNotEmpty


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    innerPadding: PaddingValues,
    backGround: Color = MaterialTheme.colorScheme.background,
    viewModel: HomeViewModel = viewModel(),
    onNavigateToSettings: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val appLockManager = remember { AppLockManager(context) }
    val wallpaperManager = remember { WallpaperManager(context) }
    val layoutManager = remember { LayoutManager(context) }
    val wallpaperUri by wallpaperManager.wallpaperUriFlow.collectAsStateWithLifecycle()
    val iconSize by layoutManager.iconSizeFlow.collectAsStateWithLifecycle()
    val launcherMode by layoutManager.launcherModeFlow.collectAsStateWithLifecycle()

    LaunchedEffect(launcherMode) {
        viewModel.loadApps(context)
    }

    val gridApps by viewModel.gridApps.collectAsStateWithLifecycle()
    val dockApps = gridApps

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (wallpaperUri != null) {
            Image(
                painter = rememberAsyncImagePainter(Uri.parse(wallpaperUri)),
                contentDescription = "Wallpaper",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backGround)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (launcherMode == LauncherMode.APP_DRAWER) {
                HomeSearchBar()
                Spacer(modifier = Modifier.height(26.dp))
            } else {
                // HOME_GRID modunda sadece settings butonu
                if (onNavigateToSettings != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            if (launcherMode == LauncherMode.APP_DRAWER) {
                LockScreenClock()
                Spacer(modifier = Modifier.height(14.dp))
            }

            if (gridApps.isNotEmpty()) {
                HomeGrid(
                    apps = gridApps,
                    context = context,
                    modifier = if (launcherMode == LauncherMode.HOME_GRID) Modifier.weight(1f) else Modifier,
                    appLockManager = appLockManager,
                    iconSize = iconSize.homeScreenSize,
                    isFullScreen = launcherMode == LauncherMode.HOME_GRID
                )
            }

            if (launcherMode == LauncherMode.APP_DRAWER) {
                Spacer(modifier = Modifier.weight(1f))
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            BottomBar(
                apps = dockApps.take(4),
                context = context,
                appLockManager = appLockManager,
                iconSize = iconSize.bottomBarSize
            )
        }
    }
}







