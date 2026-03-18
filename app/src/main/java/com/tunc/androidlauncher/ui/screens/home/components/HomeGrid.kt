package com.tunc.androidlauncher.ui.screens.home.components

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.tunc.androidlauncher.core.models.AppInfo
import com.tunc.androidlauncher.data.AppLockManager
import com.tunc.androidlauncher.data.AppManager
import com.tunc.androidlauncher.data.AppPlacementManager
import com.tunc.androidlauncher.data.FolderManager
import com.tunc.androidlauncher.data.RecentAppsManager
import com.tunc.androidlauncher.ui.components.AppContextMenu
import com.tunc.androidlauncher.ui.components.CreateFolderDialog
import com.tunc.androidlauncher.ui.components.FolderDialog
import com.tunc.androidlauncher.ui.components.NotificationBadge
import com.tunc.androidlauncher.ui.components.RenameFolderDialog
import com.tunc.androidlauncher.ui.screens.appdrawer.components.FolderItem
import com.tunc.androidlauncher.ui.screens.home.viewmodels.HomeViewModel
import com.tunc.androidlauncher.ui.screens.launchersettings.applock.components.PinVerificationDialog
import com.tunc.androidlauncher.utils.AppUninstaller
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeGrid(
    apps: List<AppInfo?>,
    mostUsedApps:List<AppInfo?>,
    context: Context,
    modifier: Modifier = Modifier,
    appLockManager: AppLockManager? = null,
    iconSize: Int = 36,
    isFullScreen: Boolean = false,
    viewModel: HomeViewModel,
    bottomBarBounds: androidx.compose.ui.geometry.Rect? = null,
    bottomBarIconBounds: Map<String, androidx.compose.ui.geometry.Rect> = emptyMap(),
    onAppDroppedToBottomBar: ((AppInfo, String?) -> Unit)? = null,
    onDragOverlayStart: ((AppInfo, Offset, Int) -> Unit)? = null,
    onDragOverlayMove: ((Offset) -> Unit)? = null,
    onDragOverlayEnd: (() -> Unit)? = null
) {
    val folderManager = remember { FolderManager(context) }
    val appManager = remember { AppManager.getInstance(context) }
    val placementManager = remember { AppPlacementManager.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()

    val foldersWithApps by folderManager.getFoldersWithApps()
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val allPlacements by placementManager.allPlacementsFlow
        .collectAsStateWithLifecycle(initialValue = emptyList())
    var selectedFolder by remember { mutableStateOf<Long?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var folderToRename by remember { mutableStateOf<Pair<Long, String>?>(null) }

    var editMode by remember { mutableStateOf(false) }

    // HOISTED CONTEXT MENU STATES
    var appForContextMenu by remember { mutableStateOf<AppInfo?>(null) }
    var contextMenuPosition by remember { mutableStateOf(Offset.Zero) }

    var draggedApp by remember { mutableStateOf<AppInfo?>(null) }
    var draggedFolderId by remember { mutableStateOf<Long?>(null) }
    var draggedAppStartPosition by remember { mutableStateOf(Offset.Zero) }
    var currentDragPosition by remember { mutableStateOf(Offset.Zero) }
    val appPositions = remember { mutableStateMapOf<String, Pair<Offset, IntSize>>() }
    val folderPositions = remember { mutableStateMapOf<Long, Pair<Offset, IntSize>>() }
    var hoveredApp by remember { mutableStateOf<AppInfo?>(null) }
    var hoveredFolderId by remember { mutableStateOf<Long?>(null) }
    var isFolderDrop by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    // Triple: (draggedApp, targetApp, targetSortIndex)
    var appsToFolder by remember { mutableStateOf<Triple<AppInfo, AppInfo, Int>?>(null) }

    val appsInFolders = remember(foldersWithApps) {
        foldersWithApps.flatMap { it.apps.map { app -> app.packageName } }.toSet()
    }

    val allApps by appManager.allApps.collectAsStateWithLifecycle()

    // Klasörler ve uygulamaları sortIndex'e göre birlikte sırala
    val combinedItems = remember(apps, foldersWithApps, appsInFolders, allPlacements) {
        val filteredApps = apps.filterNotNull().filter { !appsInFolders.contains(it.packageName) }
        val placementMap = allPlacements.associateBy { it.packageName }

        // Her öğeye bir sortIndex ata
        data class SortedItem(val item: Any, val sortIndex: Int)

        val sortedItems = mutableListOf<SortedItem>()

        // Uygulamaları ekle (grid placement'tan sortIndex al)
        filteredApps.forEach { app ->
            val placement = placementMap[app.packageName]
            val index = placement?.sortIndex ?: Int.MAX_VALUE
            sortedItems.add(SortedItem(app, index))
        }

        // Klasörleri ekle (folder sortIndex'ten al)
        foldersWithApps.forEach { folderWithApps ->
            sortedItems.add(SortedItem(folderWithApps, folderWithApps.folder.sortIndex))
        }

        // Hepsini sortIndex'e göre sırala
        sortedItems.sortedBy { it.sortIndex }.map { it.item }
    }

    // ROOT BOX FOR LAYERING
    Box(modifier = modifier.fillMaxSize()) {
        if (isFullScreen) {
            val columns = 4
            // Actual item height: icon box (iconSize+28) + column spacing (12) + text (~16) + grid vertical spacing (16)
            val itemHeightDp = iconSize + 28 + 12 + 16 + 16
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                // Subtract vertical content padding (8dp top + 8dp bottom)
                val availableHeightDp = maxHeight.value - 16f
                val rowsPerPage = (availableHeightDp / itemHeightDp).toInt().coerceAtLeast(1)
                val itemsPerPage = columns * rowsPerPage
                val pageCount = ceil(combinedItems.size.toDouble() / itemsPerPage).toInt().coerceAtLeast(1)
                val pagerState = rememberPagerState(pageCount = { pageCount })

                LaunchedEffect(pagerState.currentPage) {
                    if (editMode) editMode = false
                    appForContextMenu = null // Hide context menu when swiping pages
                }

                var showPageIndicator by remember { mutableStateOf(false) }

                LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
                    if (pagerState.isScrollInProgress || pagerState.currentPage > 0) {
                        showPageIndicator = true
                    }
                    if (!pagerState.isScrollInProgress && showPageIndicator) {
                        delay(3000L)
                        showPageIndicator = false
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize().padding(bottom = 0.dp)
                    ) { page ->
                        val startIndex = page * itemsPerPage
                        val endIndex = (startIndex + itemsPerPage).coerceAtMost(combinedItems.size)
                        val pageItems = combinedItems.subList(startIndex, endIndex)

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            userScrollEnabled = false,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(pageItems, key = { item ->
                                when (item) {
                                    is com.tunc.androidlauncher.data.database.FolderWithApps -> "folder_${item.folder.id}"
                                    is AppInfo -> "app_${item.packageName}"
                                    else -> item.hashCode()
                                }
                            }) { item ->
                                Box(contentAlignment = Alignment.Center) {
                                    when (item) {
                                        is com.tunc.androidlauncher.data.database.FolderWithApps -> {
                                            val folderApps = item.apps.mapNotNull { folderApp ->
                                                allApps.find { it.packageName == folderApp.packageName }
                                            }
                                            val isFolderHovered = hoveredFolderId == item.folder.id
                                            val isFolderBeingDragged = draggedFolderId == item.folder.id
                                            var folderDragOffset by remember { mutableStateOf(Offset.Zero) }
                                            var isFolderDragging by remember { mutableStateOf(false) }
                                            var folderIconPosition by remember { mutableStateOf(Offset.Zero) }
                                            Box(
                                                modifier = Modifier
                                                    .onGloballyPositioned { coordinates ->
                                                        folderPositions[item.folder.id] = Pair(
                                                            coordinates.positionInRoot(),
                                                            coordinates.size
                                                        )
                                                        if (!isFolderDragging) {
                                                            folderIconPosition = coordinates.positionInRoot()
                                                        }
                                                    }
                                                    .offset {
                                                        IntOffset(
                                                            folderDragOffset.x.roundToInt(),
                                                            folderDragOffset.y.roundToInt()
                                                        )
                                                    }
                                                    .graphicsLayer {
                                                        if (isFolderHovered) {
                                                            scaleX = 1.15f
                                                            scaleY = 1.15f
                                                        }
                                                        alpha = if (isFolderDragging) 0f else 1f
                                                    }
                                                    .pointerInput(item.folder.id) {
                                                        var localHasDragged = false
                                                        detectDragGesturesAfterLongPress(
                                                            onDragStart = {
                                                                localHasDragged = false
                                                                folderDragOffset = Offset.Zero
                                                            },
                                                            onDrag = { change, dragAmount ->
                                                                change.consume()
                                                                folderDragOffset += dragAmount
                                                                if (!localHasDragged && folderDragOffset.getDistance() > 5f) {
                                                                    localHasDragged = true
                                                                    isFolderDragging = true
                                                                    draggedFolderId = item.folder.id
                                                                    appForContextMenu = null
                                                                    onDragOverlayStart?.invoke(
                                                                        // Klasör için geçici bir AppInfo oluştur (overlay için)
                                                                        folderApps.firstOrNull() ?: return@detectDragGesturesAfterLongPress,
                                                                        folderIconPosition,
                                                                        iconSize + 28
                                                                    )
                                                                }
                                                                if (localHasDragged) {
                                                                    currentDragPosition = folderIconPosition + folderDragOffset
                                                                    onDragOverlayMove?.invoke(folderIconPosition + folderDragOffset)

                                                                    // Uygulama pozisyonlarını kontrol et (klasör taşıma için)
                                                                    var foundHover: AppInfo? = null
                                                                    var minDistance = Float.MAX_VALUE
                                                                    appPositions.entries.forEach { (packageName, posSize) ->
                                                                        val (appPos, appSize) = posSize
                                                                        val appCenter = appPos + Offset(appSize.width / 2f, appSize.height / 2f)
                                                                        val dragCenter = currentDragPosition + Offset(appSize.width / 2f, appSize.height / 2f)
                                                                        val distance = (dragCenter - appCenter).getDistance()
                                                                        if (distance < appSize.width && distance < minDistance) {
                                                                            minDistance = distance
                                                                            foundHover = allApps.find { it.packageName == packageName }
                                                                        }
                                                                    }
                                                                    hoveredApp = foundHover
                                                                }
                                                            },
                                                            onDragEnd = {
                                                                if (localHasDragged) {
                                                                    val hovered = hoveredApp
                                                                    if (hovered != null) {
                                                                        // Klasörü hedef uygulamanın pozisyonuna taşı
                                                                        coroutineScope.launch {
                                                                            val targetPlacement = placementManager.getPlacementForApp(hovered.packageName)
                                                                            if (targetPlacement != null) {
                                                                                viewModel.moveFolderToPosition(context, item.folder.id, targetPlacement.sortIndex)
                                                                            }
                                                                        }
                                                                    }
                                                                    isFolderDragging = false
                                                                    draggedFolderId = null
                                                                    hoveredApp = null
                                                                    onDragOverlayEnd?.invoke()
                                                                }
                                                                folderDragOffset = Offset.Zero
                                                                localHasDragged = false
                                                            },
                                                            onDragCancel = {
                                                                if (localHasDragged) {
                                                                    isFolderDragging = false
                                                                    draggedFolderId = null
                                                                    hoveredApp = null
                                                                    onDragOverlayEnd?.invoke()
                                                                }
                                                                folderDragOffset = Offset.Zero
                                                                localHasDragged = false
                                                            }
                                                        )
                                                    }
                                            ) {
                                                FolderItem(
                                                    folderName = item.folder.name,
                                                    apps = folderApps,
                                                    onClick = { selectedFolder = item.folder.id },
                                                    iconSize = iconSize
                                                )
                                            }
                                        }

                                        is AppInfo -> {
                                            val isHovered = hoveredApp?.packageName == item.packageName
                                            Box(
                                                modifier = Modifier
                                                    .onGloballyPositioned { coordinates ->
                                                        appPositions[item.packageName] = Pair(
                                                            coordinates.positionInRoot(),
                                                            coordinates.size
                                                        )
                                                    }
                                                    .graphicsLayer {
                                                        if (isHovered && editMode) {
                                                            scaleX = 1.1f
                                                            scaleY = 1.1f
                                                        }
                                                    }
                                            ) {
                                                HomeIconItemWithDrag(
                                                    app = item,
                                                    context = context,
                                                    appLockManager = appLockManager,
                                                    iconSize = iconSize,
                                                    editMode = editMode,
                                                    onEditModeChange = { editMode = it },
                                                    onDeleteClick = {
                                                        AppUninstaller.uninstallApp(context, item.packageName)
                                                        editMode = false
                                                    },
                                                    isSelected = appForContextMenu == item,
                                                    isHovered = hoveredApp == item,
                                                    onShowContextMenu = { position ->
                                                        appForContextMenu = item
                                                        contextMenuPosition = position
                                                    },
                                                    onDragStart = { startPosition ->
                                                        appForContextMenu = null
                                                        draggedApp = item
                                                        draggedAppStartPosition = startPosition
                                                        hoveredApp = null
                                                        isFolderDrop = false
                                                        onDragOverlayStart?.invoke(item, startPosition, iconSize + 28)
                                                    },
                                                    onDrag = { currentPos ->
                                                        currentDragPosition = currentPos
                                                        onDragOverlayMove?.invoke(currentPos)

                                                        var foundHover: AppInfo? = null
                                                        var nearbyFolder = false
                                                        var foundFolderId: Long? = null

                                                        // 1. Sürüklenen öğenin (dragged object) geçerli Rect'ini (kutusunu) bul
                                                        // Metin alanını dahil etmemek ve simetri sağlamak için kare şeklinde (width = height) oluşturulur
                                                        val draggedSize = appPositions[item.packageName]?.second ?: IntSize(150, 150)
                                                        val iconPx = draggedSize.width.toFloat()
                                                        val draggedRect = androidx.compose.ui.geometry.Rect(
                                                            currentPos.x,
                                                            currentPos.y,
                                                            currentPos.x + iconPx,
                                                            currentPos.y + iconPx
                                                        )

                                                        // Önce klasör pozisyonlarını kontrol et
                                                        var maxFolderOverlapArea = 0f
                                                        folderPositions.entries.forEach { (folderId, posSize) ->
                                                            val (folderPos, folderSize) = posSize

                                                            // Klasör için toleranslı bir "hitbox" tanımlıyoruz.
                                                            // Kare olması için dikeyde de folderSize.width kullanıyoruz.
                                                            val hitPadding = 45f
                                                            val folderRect = androidx.compose.ui.geometry.Rect(
                                                                folderPos.x - hitPadding,
                                                                folderPos.y - hitPadding,
                                                                folderPos.x + folderSize.width + hitPadding,
                                                                folderPos.y + folderSize.width + hitPadding
                                                            )

                                                            if (draggedRect.overlaps(folderRect)) {
                                                                val intersection = draggedRect.intersect(folderRect)
                                                                val area = intersection.width * intersection.height
                                                                if (area > maxFolderOverlapArea) {
                                                                    maxFolderOverlapArea = area
                                                                    foundFolderId = folderId
                                                                }
                                                            }
                                                        }

                                                        // Klasör bulunduysa klasör hover'ını ayarla
                                                        if (foundFolderId != null) {
                                                            hoveredFolderId = foundFolderId
                                                            hoveredApp = null
                                                            isFolderDrop = false
                                                        } else {
                                                            hoveredFolderId = null

                                                            // Klasör bulunamadıysa Uygulama pozisyonlarını kontrol et
                                                            var maxAppOverlapArea = 0f
                                                            var closestPkg: String? = null

                                                            appPositions.entries.forEach { (packageName, posSize) ->
                                                                if (packageName != item.packageName) {
                                                                    val (appPos, appSize) = posSize
                                                                    
                                                                    // Kare şeklinde dokunma toleransı (hitbox) tanımlıyoruz.
                                                                    val hitPadding = 35f
                                                                    val appRect = androidx.compose.ui.geometry.Rect(
                                                                        appPos.x - hitPadding,
                                                                        appPos.y - hitPadding,
                                                                        appPos.x + appSize.width + hitPadding,
                                                                        appPos.y + appSize.width + hitPadding
                                                                    )

                                                                    if (draggedRect.overlaps(appRect)) {
                                                                        val intersection = draggedRect.intersect(appRect)
                                                                        val area = intersection.width * intersection.height
                                                                        if (area > maxAppOverlapArea) {
                                                                            maxAppOverlapArea = area
                                                                            closestPkg = packageName
                                                                        }
                                                                    }
                                                                }
                                                            }

                                                            if (closestPkg != null) {
                                                                foundHover = allApps.find { it.packageName == closestPkg }
                                                                // Kesişme varsa her türlü tetiklesin.
                                                                nearbyFolder = true
                                                            }

                                                            hoveredApp = foundHover
                                                            isFolderDrop = nearbyFolder
                                                        }
                                                    },
                                                    onDragEnd = {
                                                        val hovered = hoveredApp
                                                        val targetFolderId = hoveredFolderId
                                                        val droppedOnBottomBar = bottomBarBounds?.let { bounds ->
                                                            currentDragPosition.y >= (bounds.top - 150f) &&
                                                                    currentDragPosition.y <= (bounds.bottom + 150f) &&
                                                                    currentDragPosition.x >= (bounds.left - 50f) &&
                                                                    currentDragPosition.x <= (bounds.right + 50f)
                                                        } == true
                                                        if (droppedOnBottomBar && onAppDroppedToBottomBar != null) {
                                                            var targetPkg: String? = null
                                                            for ((pkg, rect) in bottomBarIconBounds) {
                                                                if (currentDragPosition.x >= rect.left - 30f &&
                                                                    currentDragPosition.x <= rect.right + 30f &&
                                                                    currentDragPosition.y >= rect.top - 30f &&
                                                                    currentDragPosition.y <= rect.bottom + 30f) {
                                                                    targetPkg = pkg
                                                                    break
                                                                }
                                                            }
                                                            onAppDroppedToBottomBar(item, targetPkg)
                                                        } else if (targetFolderId != null) {
                                                            // Mevcut klasöre ekle
                                                            coroutineScope.launch {
                                                                folderManager.addAppToFolder(targetFolderId, item.packageName)
                                                            }
                                                        } else if (hovered != null && hovered.packageName != item.packageName) {
                                                            if (isFolderDrop) {
                                                                // Hedef uygulamanın sortIndex'ini al
                                                                coroutineScope.launch {
                                                                    val targetPlacement = placementManager.getPlacementForApp(hovered.packageName)
                                                                    val targetSortIndex = targetPlacement?.sortIndex ?: 0
                                                                    appsToFolder = Triple(item, hovered, targetSortIndex)
                                                                    showCreateFolderDialog = true
                                                                }
                                                            } else {
                                                                viewModel.moveApp(context, item.packageName, hovered.packageName)
                                                            }
                                                        }
                                                        draggedApp = null
                                                        hoveredApp = null
                                                        hoveredFolderId = null
                                                        isFolderDrop = false
                                                        draggedAppStartPosition = Offset.Zero
                                                        currentDragPosition = Offset.Zero
                                                        onDragOverlayEnd?.invoke()
                                                    },
                                                    onDragCancel = {
                                                        draggedApp = null
                                                        hoveredApp = null
                                                        hoveredFolderId = null
                                                        isFolderDrop = false
                                                        draggedAppStartPosition = Offset.Zero
                                                        currentDragPosition = Offset.Zero
                                                        onDragOverlayEnd?.invoke()
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (pageCount > 1) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 0.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = showPageIndicator,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    repeat(pageCount) { index ->
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (pagerState.currentPage == index)
                                                        MaterialTheme.colorScheme.primary
                                                    else
                                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                userScrollEnabled = false,
                modifier = Modifier.fillMaxWidth()
            ) {
                items(mostUsedApps.take(4), key = { it?.packageName ?: "null_${it.hashCode()}" }) { app ->
                    Box(contentAlignment = Alignment.Center) {
                        HomeIconItem(
                            app = app,
                            context = context,
                            appLockManager = appLockManager,
                            iconSize = iconSize
                        )
                    }
                }
            }
        }

        appForContextMenu?.let { app ->
            val configuration = LocalConfiguration.current
            val density = LocalDensity.current
            val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
            var menuSizePx by remember { mutableStateOf(IntSize.Zero) }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(100f)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { appForContextMenu = null })
                    }
            ) {
                val menuWidthPx = with(density) { 200.dp.toPx() }
                val iconWidthPx = with(density) { 84.dp.toPx() }
                val screenMiddle = screenWidthPx / 2f

                val isRightSide = contextMenuPosition.x > screenMiddle

                var calculatedX = if (isRightSide) {
                    (contextMenuPosition.x + iconWidthPx) - menuWidthPx - with(density) { 12.dp.toPx() }
                } else {

                    val iconCenter = contextMenuPosition.x + (iconWidthPx / 2f)
                    iconCenter - (menuWidthPx / 2f)
                }


                val edgePadding = with(density) { 16.dp.toPx() }
                if (calculatedX < edgePadding) {
                    calculatedX = edgePadding
                } else if (calculatedX + menuWidthPx > screenWidthPx - edgePadding) {
                    calculatedX = screenWidthPx - menuWidthPx - edgePadding
                }

                val isNearTop = contextMenuPosition.y < 300f
                val calculatedY = if (isNearTop) {
                    contextMenuPosition.y + with(density) { 5.dp.toPx() }
                } else {
                    contextMenuPosition.y - menuSizePx.height - with(density) { 95.dp.toPx() }
                }
                Box(
                    modifier = Modifier.offset {
                        IntOffset(
                            x = calculatedX.roundToInt(),
                            y = calculatedY.roundToInt()
                        )
                    }
                ) {
                    AppContextMenu(
                        onDismiss = { appForContextMenu = null },
                        onDelete = {
                            AppUninstaller.uninstallApp(context, app.packageName)
                            appForContextMenu = null
                        }
                    )
                }
            }
        }

        selectedFolder?.let { folderId ->
            val folder = foldersWithApps.find { it.folder.id == folderId }
            folder?.let {
                val folderApps = it.apps.mapNotNull { folderApp ->
                    allApps.find { app -> app.packageName == folderApp.packageName }
                }
                FolderDialog(
                    folderName = it.folder.name,
                    apps = folderApps,
                    onDismiss = { selectedFolder = null },
                    onAppClick = { app ->
                        val recentAppsManager = RecentAppsManager(context)
                        recentAppsManager.addRecentApp(app.packageName)
                        val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                        launchIntent?.let { intent -> context.startActivity(intent) }
                        selectedFolder = null
                    },
                    onRenameFolder = { newName ->
                        coroutineScope.launch { folderManager.updateFolderName(it.folder.id, newName) }
                    },
                    onAppRemove = { app ->
                        coroutineScope.launch { folderManager.removeAppFromFolder(it.folder.id, app.packageName) }
                    },
                    iconSize = 48
                )
            }
        }

        if (showRenameDialog && folderToRename != null) {
            RenameFolderDialog(
                currentName = folderToRename!!.second,
                onDismiss = {
                    showRenameDialog = false
                    folderToRename = null
                },
                onConfirm = { newName ->
                    coroutineScope.launch {
                        folderManager.updateFolderName(folderToRename!!.first, newName)
                        showRenameDialog = false
                        folderToRename = null
                    }
                }
            )
        }

        if (showCreateFolderDialog && appsToFolder != null) {
            val suggestedCategory = folderManager.getCategoryForPackage(appsToFolder!!.first.packageName)
            CreateFolderDialog(
                onDismiss = {
                    showCreateFolderDialog = false
                    appsToFolder = null
                },
                onConfirm = { folderName ->
                    coroutineScope.launch {
                        val targetSortIndex = appsToFolder!!.third
                        val folderId = folderManager.createFolder(folderName, targetSortIndex)
                        folderManager.addAppToFolder(folderId, appsToFolder!!.first.packageName)
                        folderManager.addAppToFolder(folderId, appsToFolder!!.second.packageName)
                        // Klasöre eklenen uygulamaları grid placement'tan kaldır
                        placementManager.removeAppsFromGrid(
                            listOf(appsToFolder!!.first.packageName, appsToFolder!!.second.packageName)
                        )
                        showCreateFolderDialog = false
                        appsToFolder = null
                    }
                },
                suggestedCategory = suggestedCategory
            )
        }
    }
}

@Composable
private fun HomeIconItem(
    app: AppInfo?,
    context: Context,
    appLockManager: AppLockManager?,
    iconSize: Int = 36
) {
    if (app == null) {
        Spacer(modifier = Modifier.size(64.dp))
        return
    }

    var showPinDialog by remember { mutableStateOf(false) }
    val isLocked = appLockManager?.isAppLocked(app.packageName) == true && appLockManager.isPinSet()
    val recentAppsManager = remember { RecentAppsManager(context) }

    Column(
        modifier = Modifier.width(84.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size((iconSize + 28).dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Transparent)
                .clickable {
                    if (isLocked) {
                        showPinDialog = true
                    } else {
                        recentAppsManager.addRecentApp(app.packageName)
                        val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                        launchIntent?.let { context.startActivity(it) }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            app.icon?.let { icon ->
                AsyncImage(
                    model = icon,
                    contentDescription = app.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            if (app.notificationCount > 0) {
                NotificationBadge(
                    count = app.notificationCount,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }

        Text(
            text = app.name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .widthIn(max = 70.dp)
        )
    }

    if (showPinDialog && appLockManager != null) {
        PinVerificationDialog(
            onDismiss = { showPinDialog = false },
            onPinVerified = {
                showPinDialog = false
                recentAppsManager.addRecentApp(app.packageName)
                val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                launchIntent?.let { context.startActivity(it) }
            },
            verifyPin = { pin -> appLockManager.verifyPin(pin) }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeIconItemWithDrag(
    app: AppInfo,
    context: Context,
    appLockManager: AppLockManager?,
    iconSize: Int = 36,
    editMode: Boolean,
    onEditModeChange: (Boolean) -> Unit,
    onDeleteClick: () -> Unit,
    onDragStart: (Offset) -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    onShowContextMenu: (Offset) -> Unit,
    isSelected: Boolean = false,
    isHovered: Boolean = false
) {
    var showPinDialog by remember { mutableStateOf(false) }
    val isLocked = appLockManager?.isAppLocked(app.packageName) == true && appLockManager.isPinSet()
    val recentAppsManager = remember { RecentAppsManager(context) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }
    var exactIconPosition by remember { mutableStateOf(Offset.Zero) }

    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    val currentOnDragCancel by rememberUpdatedState(onDragCancel)

    // We don't need local context menu states here anymore!

    Column(
        modifier = Modifier
            .width(84.dp)
            .offset {
                IntOffset(
                    dragOffset.x.roundToInt(),
                    dragOffset.y.roundToInt()
                )
            }
            .graphicsLayer {
                scaleX = if (isDragging || isSelected) 1.15f else if (isHovered) 1.08f else 1f
                scaleY = if (isDragging || isSelected) 1.15f else if (isHovered) 1.08f else 1f
                alpha = if (isDragging) 0f else 1f
                translationY = if (isSelected && !isDragging) -8f else 0f
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(contentAlignment = Alignment.TopStart) {
            Box(
                modifier = Modifier
                    .size((iconSize + 28).dp)
                    .clip(RoundedCornerShape(20.dp))
                    .onGloballyPositioned { coordinates ->
                        if (!isDragging) {
                            exactIconPosition = coordinates.positionInRoot()
                        }
                    }
                    .pointerInput(editMode) {
                        detectTapGestures(
                            onTap = {
                                if (editMode) {
                                    onEditModeChange(false)
                                } else if (isLocked) {
                                    showPinDialog = true
                                } else {
                                    recentAppsManager.addRecentApp(app.packageName)
                                    val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                    launchIntent?.let { context.startActivity(it) }
                                }
                            }
                        )
                    }
                    .pointerInput(app.packageName) {
                        var localHasDragged = false
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                localHasDragged = false
                                dragOffset = Offset.Zero

                                // TRIGGERS THE MENU IMMEDIATELY ON LONG PRESS
                                onShowContextMenu(exactIconPosition)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffset += dragAmount
                                if (!localHasDragged && dragOffset.getDistance() > 5f) {
                                    localHasDragged = true
                                    isDragging = true
                                    currentOnDragStart(exactIconPosition) // This hides the menu via HomeGrid
                                }
                                if (localHasDragged) {
                                    currentOnDrag(exactIconPosition + dragOffset)
                                }
                            },
                            onDragEnd = {
                                if (localHasDragged) {
                                    isDragging = false
                                    currentOnDragEnd()
                                }
                                dragOffset = Offset.Zero
                                localHasDragged = false
                            },
                            onDragCancel = {
                                if (localHasDragged) {
                                    isDragging = false
                                    currentOnDragCancel()
                                }
                                dragOffset = Offset.Zero
                                localHasDragged = false
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                app.icon?.let { icon ->
                    AsyncImage(
                        model = icon,
                        contentDescription = app.name,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                if (app.notificationCount > 0 && !editMode) {
                    NotificationBadge(
                        count = app.notificationCount,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
            }

            if (editMode) {
                Box(
                    modifier = Modifier
                        .offset(x = 2.dp, y = 2.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error)
                        .clickable { onDeleteClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // The AppContextMenu Box was entirely removed from here because it is now safely inside HomeGrid!
        }

        Text(
            text = app.name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .widthIn(max = 70.dp)
        )
    }

    if (showPinDialog && appLockManager != null) {
        PinVerificationDialog(
            onDismiss = { showPinDialog = false },
            onPinVerified = {
                showPinDialog = false
                recentAppsManager.addRecentApp(app.packageName)
                val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                launchIntent?.let { context.startActivity(it) }
            },
            verifyPin = { pin -> appLockManager.verifyPin(pin) }
        )
    }
}