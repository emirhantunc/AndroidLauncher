package com.tunc.androidlauncher.ui.screens.home

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.tunc.androidlauncher.core.models.AppInfo
import com.tunc.androidlauncher.data.AppLockManager
import com.tunc.androidlauncher.data.AppPlacementManager
import com.tunc.androidlauncher.data.LauncherMode
import com.tunc.androidlauncher.data.LayoutManager
import com.tunc.androidlauncher.data.WallpaperManager
import com.tunc.androidlauncher.ui.screens.home.components.BottomBar
import com.tunc.androidlauncher.ui.screens.home.components.HomeGrid
import com.tunc.androidlauncher.ui.screens.home.components.HomeSearchBar
import com.tunc.androidlauncher.ui.screens.home.components.LockScreenClock
import com.tunc.androidlauncher.ui.screens.home.viewmodels.HomeViewModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


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
    val placementManager = remember { AppPlacementManager.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()

    val wallpaperUri by wallpaperManager.wallpaperUriFlow.collectAsStateWithLifecycle()
    val iconSize by layoutManager.iconSizeFlow.collectAsStateWithLifecycle()
    val launcherMode by layoutManager.launcherModeFlow.collectAsStateWithLifecycle()

    LaunchedEffect(launcherMode) {
        viewModel.loadApps(context)
    }

    val gridApps by viewModel.gridApps.collectAsStateWithLifecycle()
    val bottomBarApps by viewModel.bottomBarApps.collectAsStateWithLifecycle()

    // Cross-drag bounds tracking
    var bottomBarBounds by remember { mutableStateOf<Rect?>(null) }
    var gridBounds by remember { mutableStateOf<Rect?>(null) }

    // Drag overlay state - sürüklenen ikon en üst katmanda gösterilecek
    var dragOverlayApp by remember { mutableStateOf<AppInfo?>(null) }
    var dragOverlayPosition by remember { mutableStateOf(Offset.Zero) }
    var dragOverlayIconSize by remember { mutableIntStateOf(0) }
    // Root Box'ın pozisyonunu takip et
    var rootPosition by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coordinates ->
                rootPosition = coordinates.positionInRoot()
            }
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
                Box(
                    modifier = (if (launcherMode == LauncherMode.HOME_GRID) Modifier.weight(1f) else Modifier)
                        .onGloballyPositioned { coordinates ->
                            val pos = coordinates.positionInRoot()
                            val size = coordinates.size
                            gridBounds = Rect(
                                pos.x,
                                pos.y,
                                pos.x + size.width,
                                pos.y + size.height
                            )
                        }
                ) {
                    HomeGrid(
                        apps = gridApps,
                        context = context,
                        modifier = Modifier,
                        appLockManager = appLockManager,
                        iconSize = iconSize.homeScreenSize,
                        isFullScreen = launcherMode == LauncherMode.HOME_GRID,
                        bottomBarBounds = bottomBarBounds,
                        onAppDroppedToBottomBar = { app ->
                            coroutineScope.launch {
                                placementManager.moveFromGridToBottomBar(app.packageName)
                            }
                        },
                        onDragOverlayStart = { app, position, size ->
                            dragOverlayApp = app
                            dragOverlayPosition = position
                            dragOverlayIconSize = size
                        },
                        onDragOverlayMove = { position ->
                            dragOverlayPosition = position
                        },
                        onDragOverlayEnd = {
                            dragOverlayApp = null
                        }
                    )
                }
            }

            if (launcherMode == LauncherMode.APP_DRAWER) {
                Spacer(modifier = Modifier.weight(1f))
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (bottomBarApps.isNotEmpty()) {
                BottomBar(
                    apps = bottomBarApps.take(4),
                    context = context,
                    appLockManager = appLockManager,
                    iconSize = iconSize.bottomBarSize,
                    onAppsReordered = { reorderedApps ->
                        coroutineScope.launch {
                            placementManager.reorderBottomBar(reorderedApps.map { it.packageName })
                        }
                    },
                    onAppRemovedFromBar = { app ->
                        coroutineScope.launch {
                            placementManager.removeFromBottomBar(app.packageName)
                        }
                    },
                    gridBounds = gridBounds,
                    onBoundsChanged = { bounds ->
                        bottomBarBounds = bounds
                    },
                    onDragOverlayStart = { app, position, size ->
                        dragOverlayApp = app
                        dragOverlayPosition = position
                        dragOverlayIconSize = size
                    },
                    onDragOverlayMove = { position ->
                        dragOverlayPosition = position
                    },
                    onDragOverlayEnd = {
                        dragOverlayApp = null
                    }
                )
            } else {
                // Bottom bar boş olsa bile bounds'u track etmek için boş bir Row koy
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .onGloballyPositioned { coordinates ->
                            val pos = coordinates.positionInRoot()
                            val size = coordinates.size
                            bottomBarBounds = Rect(
                                pos.x,
                                pos.y,
                                pos.x + size.width,
                                pos.y + size.height
                            )
                        }
                ) {}
            }
        }

        // Drag Overlay - sürüklenen ikon en üst katmanda render edilir
        dragOverlayApp?.let { app ->
            val density = LocalDensity.current
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(100f)
            ) {
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (dragOverlayPosition.x - rootPosition.x).roundToInt(),
                                (dragOverlayPosition.y - rootPosition.y).roundToInt()
                            )
                        }
                        .size(dragOverlayIconSize.dp)
                        .graphicsLayer {
                            scaleX = 1.1f
                            scaleY = 1.1f
                            alpha = 0.95f
                        }
                        .clip(RoundedCornerShape(if (dragOverlayIconSize > 40) 20.dp else 14.dp))
                ) {
                    app.icon?.let { icon ->
                        val bitmap = remember(icon) {
                            val w = icon.intrinsicWidth.takeIf { it > 0 } ?: 144
                            val h = icon.intrinsicHeight.takeIf { it > 0 } ?: 144
                            icon.toBitmap(w, h).asImageBitmap()
                        }
                        Image(
                            bitmap = bitmap,
                            contentDescription = app.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(if (dragOverlayIconSize > 40) 20.dp else 14.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}







