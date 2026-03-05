package com.tunc.androidlauncher.ui.screens.home.components

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.tunc.androidlauncher.R
import com.tunc.androidlauncher.core.models.AppInfo
import com.tunc.androidlauncher.data.AppLockManager
import com.tunc.androidlauncher.data.AppManager
import com.tunc.androidlauncher.data.FolderManager
import com.tunc.androidlauncher.data.RecentAppsManager
import com.tunc.androidlauncher.ui.components.CreateFolderDialog
import com.tunc.androidlauncher.ui.components.FolderDialog
import com.tunc.androidlauncher.ui.components.NotificationBadge
import com.tunc.androidlauncher.ui.components.RenameFolderDialog
import com.tunc.androidlauncher.ui.screens.appdrawer.components.FolderItem
import com.tunc.androidlauncher.ui.screens.launchersettings.applock.components.PinVerificationDialog
import com.tunc.androidlauncher.utils.AppUninstaller
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.roundToInt


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeGrid(
    apps: List<AppInfo?>,
    context: Context,
    modifier: Modifier = Modifier,
    appLockManager: AppLockManager? = null,
    iconSize: Int = 36,
    isFullScreen: Boolean = false
) {
    val folderManager = remember { FolderManager(context) }
    val appManager = remember { AppManager.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()

    // Klasör state'leri
    val foldersWithApps by folderManager.getFoldersWithApps().collectAsStateWithLifecycle(initialValue = emptyList())
    var selectedFolder by remember { mutableStateOf<Long?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var folderToRename by remember { mutableStateOf<Pair<Long, String>?>(null) }

    // Düzenleme modu
    var editMode by remember { mutableStateOf(false) }

    // Drag & Drop state'leri
    var draggedApp by remember { mutableStateOf<AppInfo?>(null) }
    var draggedAppStartPosition by remember { mutableStateOf(Offset.Zero) }
    var currentDragPosition by remember { mutableStateOf(Offset.Zero) }
    val appPositions = remember { mutableStateMapOf<String, Pair<Offset, IntSize>>() }
    var hoveredApp by remember { mutableStateOf<AppInfo?>(null) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var appsToFolder by remember { mutableStateOf<Pair<AppInfo, AppInfo>?>(null) }

    // Klasörde olan uygulamaları filtrele
    val appsInFolders = remember(foldersWithApps) {
        foldersWithApps.flatMap { it.apps.map { app -> app.packageName } }.toSet()
    }

    val allApps by appManager.allApps.collectAsStateWithLifecycle()

    // Klasörleri ve uygulamaları birleştir
    val combinedItems = remember(apps, foldersWithApps, appsInFolders) {
        val folders = foldersWithApps.sortedBy { it.folder.name.lowercase() }
        val filteredApps = apps.filterNotNull().filter { !appsInFolders.contains(it.packageName) }

        // Klasörleri önce, sonra uygulamaları ekle
        val items = mutableListOf<Any>()
        items.addAll(folders)
        items.addAll(filteredApps)
        items
    }

    if (isFullScreen) {
        // iOS tarzı yatay kaydırmalı sayfalama
        val itemsPerPage = 20 // 4 sütun x 5 satır = 20 item
        val pageCount = ceil(combinedItems.size.toDouble() / itemsPerPage).toInt().coerceAtLeast(1)
        val pagerState = rememberPagerState(pageCount = { pageCount })

        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
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
                    items(pageItems) { item ->
                        Box(contentAlignment = Alignment.Center) {
                            when (item) {
                                is com.tunc.androidlauncher.data.database.FolderWithApps -> {
                                    val folderApps = item.apps.mapNotNull { folderApp ->
                                        allApps.find { it.packageName == folderApp.packageName }
                                    }
                                    FolderItem(
                                        folderName = item.folder.name,
                                        apps = folderApps,
                                        onClick = { selectedFolder = item.folder.id },
                                        iconSize = iconSize
                                    )
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
                                                // Hover edildiğinde scale up
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
                                            onDragStart = { startPosition ->
                                                draggedApp = item
                                                draggedAppStartPosition = startPosition
                                                hoveredApp = null
                                            },
                                            onDrag = { dragOffset ->
                                                // Global pozisyonu hesapla
                                                currentDragPosition = draggedAppStartPosition + dragOffset

                                                // Hangi app'in üzerine geldiğimizi kontrol et
                                                hoveredApp = appPositions.entries.find { (packageName, posSize) ->
                                                    if (packageName == item.packageName) return@find false

                                                    val (appPos, appSize) = posSize
                                                    currentDragPosition.x >= appPos.x &&
                                                    currentDragPosition.x <= appPos.x + appSize.width &&
                                                    currentDragPosition.y >= appPos.y &&
                                                    currentDragPosition.y <= appPos.y + appSize.height
                                                }?.let { entry ->
                                                    allApps.find { it.packageName == entry.key }
                                                }
                                            },
                                            onDragEnd = {
                                                // Drag sona erdiğinde hover edilen app varsa klasör oluştur
                                                val hovered = hoveredApp
                                                if (hovered != null && hovered.packageName != item.packageName) {
                                                    appsToFolder = Pair(item, hovered)
                                                    showCreateFolderDialog = true
                                                }
                                                draggedApp = null
                                                hoveredApp = null
                                                draggedAppStartPosition = Offset.Zero
                                                currentDragPosition = Offset.Zero
                                            },
                                            onDragCancel = {
                                                draggedApp = null
                                                hoveredApp = null
                                                draggedAppStartPosition = Offset.Zero
                                                currentDragPosition = Offset.Zero
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // iOS tarzı sayfa göstergeleri
            if (pageCount > 1) {
                Row(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .height(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            userScrollEnabled = false,
            modifier = modifier.fillMaxWidth()
        ) {
            items(apps.take(4)) { app ->
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

    // Klasör dialog'u
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
                    coroutineScope.launch {
                        folderManager.updateFolderName(it.folder.id, newName)
                    }
                },
                onAppRemove = { app ->
                    coroutineScope.launch {
                        folderManager.removeAppFromFolder(it.folder.id, app.packageName)
                    }
                },
                iconSize = 48
            )
        }
    }

    // Klasör yeniden adlandırma dialog'u
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

    // Klasör oluşturma dialog'u
    if (showCreateFolderDialog && appsToFolder != null) {
        val suggestedCategory = folderManager.getCategoryForPackage(appsToFolder!!.first.packageName)

        CreateFolderDialog(
            onDismiss = {
                showCreateFolderDialog = false
                appsToFolder = null
            },
            onConfirm = { folderName ->
                coroutineScope.launch {
                    val folderId = folderManager.createFolder(folderName)
                    folderManager.addAppToFolder(folderId, appsToFolder!!.first.packageName)
                    folderManager.addAppToFolder(folderId, appsToFolder!!.second.packageName)
                    showCreateFolderDialog = false
                    appsToFolder = null
                }
            },
            suggestedCategory = suggestedCategory
        )
    }
}

@Composable
private fun HomeIconItem(
    app: AppInfo?,
    context: Context,
    appLockManager: AppLockManager?,
    iconSize: Int = 36,
    onSurfaceVariant: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    backGround: Color = MaterialTheme.colorScheme.background,
    bodySmall: TextStyle = MaterialTheme.typography.bodySmall
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
                .background(backGround)
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                .clickable {
                    if (isLocked) {
                        showPinDialog = true
                    } else {
                        recentAppsManager.addRecentApp(app.packageName)
                        val launchIntent =
                            context.packageManager.getLaunchIntentForPackage(app.packageName)
                        launchIntent?.let { context.startActivity(it) }
                    }
                }, contentAlignment = Alignment.Center
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
            style = bodySmall,
            color = onSurfaceVariant,
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
    onSurfaceVariant: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    backGround: Color = MaterialTheme.colorScheme.background,
    bodySmall: TextStyle = MaterialTheme.typography.bodySmall
) {
    var showPinDialog by remember { mutableStateOf(false) }
    val isLocked = appLockManager?.isAppLocked(app.packageName) == true && appLockManager.isPinSet()
    val recentAppsManager = remember { RecentAppsManager(context) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var isDragging by remember { mutableStateOf(false) }
    var itemPosition by remember { mutableStateOf(Offset.Zero) }

    Column(
        modifier = Modifier
            .width(84.dp)
            .onGloballyPositioned { coordinates ->
                itemPosition = coordinates.positionInRoot()
            }
            .offset {
                IntOffset(
                    dragOffset.x.roundToInt(),
                    dragOffset.y.roundToInt()
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // App icon ve silme butonu
        Box(contentAlignment = Alignment.TopStart) {
            Box(
                modifier = Modifier
                    .size((iconSize + 28).dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(backGround)
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(20.dp))
                    .combinedClickable(
                        onClick = {
                            if (editMode || isDragging) {
                                // Düzenleme modunda veya drag sırasında tıklama yapmaz
                            } else if (isLocked) {
                                showPinDialog = true
                            } else {
                                recentAppsManager.addRecentApp(app.packageName)
                                val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                launchIntent?.let { context.startActivity(it) }
                            }
                        },
                        onLongClick = {
                            if (!editMode) {
                                onEditModeChange(true)
                            }
                        }
                    )
                    .pointerInput(editMode) {
                        if (editMode) {
                            detectDragGestures(
                                onDragStart = {
                                    isDragging = true
                                    onDragStart(itemPosition)
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragOffset += dragAmount
                                    onDrag(dragOffset)
                                },
                                onDragEnd = {
                                    dragOffset = Offset.Zero
                                    isDragging = false
                                    onDragEnd()
                                },
                                onDragCancel = {
                                    dragOffset = Offset.Zero
                                    isDragging = false
                                    onDragCancel()
                                }
                            )
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

                if (app.notificationCount > 0 && !editMode) {
                    NotificationBadge(
                        count = app.notificationCount,
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
            }

            // iOS tarzı silme butonu - Icon'un sol üst köşesinde
            // Sadece sistem uygulaması olmayanlar için göster
            if (editMode && !AppUninstaller.isSystemApp(context, app.packageName)) {
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
        }

        Text(
            text = app.name,
            style = bodySmall,
            color = onSurfaceVariant,
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

