package com.tunc.androidlauncher.ui.screens.appdrawer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.tunc.androidlauncher.core.models.AppInfo
import com.tunc.androidlauncher.data.AppLockManager
import com.tunc.androidlauncher.data.AppManager
import com.tunc.androidlauncher.data.FolderManager
import com.tunc.androidlauncher.data.HiddenAppsManager
import com.tunc.androidlauncher.data.LayoutManager
import com.tunc.androidlauncher.data.RecentAppsManager
import com.tunc.androidlauncher.ui.components.AddToFolderDialog
import com.tunc.androidlauncher.ui.components.FolderDialog
import com.tunc.androidlauncher.ui.components.RenameFolderDialog
import com.tunc.androidlauncher.ui.screens.appdrawer.components.AlphabetSidebar
import com.tunc.androidlauncher.ui.screens.appdrawer.components.DraggableAppItem
import com.tunc.androidlauncher.ui.screens.appdrawer.components.FolderItem
import com.tunc.androidlauncher.ui.screens.appdrawer.components.RecentAppsSection
import com.tunc.androidlauncher.ui.screens.appdrawer.components.SearchBarSection
import com.tunc.androidlauncher.ui.screens.appdrawer.components.SectionHeader
import com.tunc.androidlauncher.ui.screens.appdrawer.components.SettingsIcon


@Composable
fun AppDrawer(
    innerPadding: PaddingValues,
    onSettingsClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val appManager = remember { AppManager.getInstance(context) }
    val appLockManager = remember { AppLockManager(context) }
    val recentAppsManager = remember { RecentAppsManager(context) }
    val hiddenAppsManager = remember { HiddenAppsManager(context) }
    val layoutManager = remember { LayoutManager(context) }
    val folderManager = remember { FolderManager(context) }
    var appList by remember { mutableStateOf<Map<String, List<AppInfo>>>(emptyMap()) }
    var foldersByLetter by remember {
        mutableStateOf<Map<String, List<com.tunc.androidlauncher.data.database.FolderWithApps>>>(
            emptyMap()
        )
    }
    val allApps by appManager.allApps.collectAsStateWithLifecycle()
    val recentPackages by recentAppsManager.recentAppsFlow.collectAsStateWithLifecycle()
    val hiddenPackages by hiddenAppsManager.hiddenAppsFlow.collectAsStateWithLifecycle()
    val iconSize by layoutManager.iconSizeFlow.collectAsStateWithLifecycle()
    val foldersWithApps by folderManager.getFoldersWithApps()
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val letterIndexMap = remember { mutableStateMapOf<String, Int>() }
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    var selectedFolder by remember { mutableStateOf<Long?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var folderToRename by remember { mutableStateOf<Pair<Long, String>?>(null) }
    var selectedApp by remember { mutableStateOf<AppInfo?>(null) }
    var showAddToFolderDialog by remember { mutableStateOf(false) }

    // Edit mode state
    var editMode by remember { mutableStateOf(false) }

    // Selected app for context menu
    var selectedAppForContext by remember { mutableStateOf<AppInfo?>(null) }

    var draggedApp by remember { mutableStateOf<AppInfo?>(null) }
    var dragPosition = Offset.Zero
    val appPositions = remember { mutableStateMapOf<String, Pair<Offset, IntSize>>() }
    val folderPositions = remember { mutableStateMapOf<Long, Pair<Offset, IntSize>>() }
    var hoveredApp by remember { mutableStateOf<AppInfo?>(null) }
    var hoveredFolderId by remember { mutableStateOf<Long?>(null) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var appsToFolder by remember { mutableStateOf<Pair<AppInfo, AppInfo>?>(null) }
    var searchQuery by remember { mutableStateOf("") } // Arama sorgusu

    val recentApps = remember(recentPackages, allApps, hiddenPackages) {
        recentPackages.mapNotNull { packageName ->
            if (!hiddenPackages.contains(packageName)) {
                allApps.find { it.packageName == packageName }
            } else {
                null
            }
        }
    }

    LaunchedEffect(Unit) {
        appManager.loadApps()
    }

    LaunchedEffect(allApps, hiddenPackages, foldersWithApps, searchQuery) { // searchQuery eklendi
        if (allApps.isNotEmpty()) {
            val appsInFolders =
                foldersWithApps.flatMap { it.apps.map { app -> app.packageName } }.toSet()

            // Tüm gizli olmayan ve klasörde olmayan uygulamaları al
            val visibleApps = allApps.filter {
                !hiddenPackages.contains(it.packageName) && !appsInFolders.contains(it.packageName)
            }

            // Arama filtresi
            val filteredApps = if (searchQuery.isBlank()) {
                visibleApps
            } else {
                visibleApps.filter {
                    it.name.startsWith(searchQuery, ignoreCase = true)
                }
            }

            val grouped = filteredApps.sortedBy { it.name.lowercase() }
                .groupBy { it.name.firstOrNull()?.uppercase() ?: "#" }
            appList = grouped

            // Klasörleri de filtrele
            val filteredFolders = if (searchQuery.isBlank()) {
                foldersWithApps
            } else {
                foldersWithApps.filter {
                    it.folder.name.startsWith(searchQuery, ignoreCase = true)
                }
            }

            val foldersGrouped = filteredFolders
                .sortedBy { it.folder.name.lowercase() }
                .groupBy { it.folder.name.firstOrNull()?.uppercase() ?: "#" }
            foldersByLetter = foldersGrouped
        }
    }

    LaunchedEffect(appList, foldersByLetter) {
        if (appList.isNotEmpty() || foldersByLetter.isNotEmpty()) {
            var currentIndex = 0
            if (recentApps.isNotEmpty()) {
                currentIndex = 1
            }
            letterIndexMap.clear()

            val allLetters = (appList.keys + foldersByLetter.keys).toSet().sorted()
            allLetters.forEach { letter ->
                letterIndexMap[letter] = currentIndex
                val foldersCount = foldersByLetter[letter]?.size ?: 0
                val appsCount = appList[letter]?.size ?: 0
                currentIndex += 1 + foldersCount + appsCount
            }
        }
    }

    LaunchedEffect(gridState.isScrollInProgress) {
        if (gridState.isScrollInProgress && editMode) {
            editMode = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(innerPadding)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SearchBarSection(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )

                SettingsIcon(onClick = onSettingsClick)
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                state = gridState,
                contentPadding = PaddingValues(
                    bottom = 20.dp, start = 16.dp, end = 28.dp
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                if (recentApps.isNotEmpty()) {
                    item(span = { GridItemSpan(4) }) {
                        RecentAppsSection(
                            recentApps = recentApps,
                            appLockManager = appLockManager,
                            iconSize = iconSize.appDrawerSize
                        )
                    }
                }

                val allLetters = (appList.keys + foldersByLetter.keys).toSet().sorted()

                allLetters.forEach { letter ->
                    item(span = { GridItemSpan(4) }) {
                        SectionHeader(letter = letter)
                    }

                    val foldersInLetter = foldersByLetter[letter] ?: emptyList()
                    foldersInLetter.forEach { folderWithApps ->
                        val folderApps = folderWithApps.apps.mapNotNull { folderApp ->
                            allApps.find { it.packageName == folderApp.packageName }
                        }
                        item {
                            val isHovered = hoveredFolderId == folderWithApps.folder.id
                            Box(
                                modifier = Modifier
                                    .onGloballyPositioned { coordinates ->
                                        folderPositions[folderWithApps.folder.id] = Pair(
                                            coordinates.positionInRoot(),
                                            coordinates.size
                                        )
                                    }
                                    .graphicsLayer {
                                        if (isHovered) {
                                            scaleX = 1.1f
                                            scaleY = 1.1f
                                        }
                                    }
                            ) {
                                FolderItem(
                                    folderName = folderWithApps.folder.name,
                                    apps = folderApps,
                                    onClick = { selectedFolder = folderWithApps.folder.id },
                                    iconSize = iconSize.appDrawerSize
                                )
                            }
                        }
                    }

                    val appsInLetter = appList[letter] ?: emptyList()
                    items(appsInLetter, key = { app -> app.packageName }) { app ->
                        Box(
                            modifier = Modifier.onGloballyPositioned { coordinates ->
                                appPositions[app.packageName] = Pair(
                                    coordinates.positionInRoot(),
                                    coordinates.size
                                )
                            }
                        ) {
                            DraggableAppItem(
                                app = app,
                                appLockManager = appLockManager,
                                iconSize = iconSize.appDrawerSize,
                                editMode = editMode,
                                onEditModeChange = { editMode = it },
                                onDeleteClick = {
                                    com.tunc.androidlauncher.utils.AppUninstaller.uninstallApp(
                                        context,
                                        app.packageName
                                    )
                                    editMode = false
                                },
                                isSelected = selectedAppForContext == app,
                                isHovered = hoveredApp == app,
                                onSelect = {
                                    selectedAppForContext = app
                                },
                                onDeselect = {
                                    selectedAppForContext = null
                                },
                                onDragStart = { position ->
                                    draggedApp = app
                                    dragPosition = position
                                    hoveredApp = null
                                },
                                onDrag = { currentPos ->
                                    dragPosition = currentPos

                                    var foundHoverApp: AppInfo? = null
                                    var foundHoverFolder: Long? = null

                                    // App positions check
                                    appPositions.entries.forEach { (packageName, positionSize) ->
                                        if (packageName != app.packageName) {
                                            val (appPos, appSize) = positionSize
                                            val isOverApp = currentPos.x >= appPos.x &&
                                                    currentPos.x <= appPos.x + appSize.width &&
                                                    currentPos.y >= appPos.y &&
                                                    currentPos.y <= appPos.y + appSize.height

                                            if (isOverApp) {
                                                foundHoverApp = allApps.find { it.packageName == packageName }
                                            }
                                        }
                                    }

                                    // Folder positions check
                                    folderPositions.entries.forEach { (folderId, positionSize) ->
                                        val (folderPos, folderSize) = positionSize
                                        val isOverFolder = currentPos.x >= folderPos.x &&
                                                currentPos.x <= folderPos.x + folderSize.width &&
                                                currentPos.y >= folderPos.y &&
                                                currentPos.y <= folderPos.y + folderSize.height

                                        if (isOverFolder) {
                                            foundHoverFolder = folderId
                                        }
                                    }

                                    hoveredApp = foundHoverApp
                                    hoveredFolderId = foundHoverFolder
                                },
                                onDragEnd = {
                                    val targetFolderId = hoveredFolderId
                                    val currentDraggedApp = draggedApp

                                    if (targetFolderId != null && currentDraggedApp != null) {
                                        // Add to existing folder
                                        coroutineScope.launch {
                                            folderManager.addAppToFolder(targetFolderId, currentDraggedApp.packageName)
                                        }
                                    } else if (hoveredApp != null && currentDraggedApp != null) {
                                        // Create new folder
                                        appsToFolder = Pair(currentDraggedApp, hoveredApp!!)
                                        showCreateFolderDialog = true
                                    }

                                    draggedApp = null
                                    hoveredApp = null
                                    hoveredFolderId = null
                                    dragPosition = Offset.Zero
                                }
                            )
                        }
                    }
                }
            }
        }
        AlphabetSidebar(
            letters = (appList.keys + foldersByLetter.keys).toSet().sorted(),
            modifier = Modifier.align(Alignment.CenterEnd),
            onLetterClick = { letter ->
                letterIndexMap[letter]?.let { index ->
                    coroutineScope.launch {
                        gridState.scrollToItem(index, scrollOffset = 0)
                    }
                }
            })
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
                    recentAppsManager.addRecentApp(app.packageName)
                    val launchIntent =
                        context.packageManager.getLaunchIntentForPackage(app.packageName)
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
        val suggestedCategory =
            folderManager.getCategoryForPackage(appsToFolder!!.first.packageName)

        com.tunc.androidlauncher.ui.components.CreateFolderDialog(
            onDismiss = {
                showCreateFolderDialog = false
                appsToFolder = null
            },
            onConfirm = { folderName ->
                coroutineScope.launch {
                    val finalName = if (folderName.isNotBlank()) folderName else suggestedCategory
                    val folderId = folderManager.createFolder(finalName)
                    folderManager.addAppToFolder(folderId, appsToFolder!!.first.packageName)
                    folderManager.addAppToFolder(folderId, appsToFolder!!.second.packageName)
                    showCreateFolderDialog = false
                    appsToFolder = null
                }
            },
            suggestedCategory = suggestedCategory
        )
    }

    if (showAddToFolderDialog && selectedApp != null) {
        val allFolders by folderManager.getAllFolders()
            .collectAsStateWithLifecycle(initialValue = emptyList())

        AddToFolderDialog(
            appName = selectedApp!!.name,
            appPackageName = selectedApp!!.packageName,
            folders = allFolders,
            onDismiss = {
                showAddToFolderDialog = false
                selectedApp = null
            },
            onCreateNewFolder = { folderName ->
                coroutineScope.launch {
                    selectedApp?.let { app ->
                        val finalName =
                            if (folderName.isNotBlank()) folderName else folderManager.getCategoryForPackage(
                                app.packageName
                            )
                        val folderId = folderManager.createFolder(finalName)
                        folderManager.addAppToFolder(folderId, app.packageName)
                    }
                    showAddToFolderDialog = false
                    selectedApp = null
                }
            },
            onSelectFolder = { folderId ->
                coroutineScope.launch {
                    selectedApp?.let {
                        folderManager.addAppToFolder(folderId, it.packageName)
                    }
                    showAddToFolderDialog = false
                    selectedApp = null
                }
            }
        )
    }
}