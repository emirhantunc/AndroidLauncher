package com.tunc.androidlauncher.ui.screens.home.components

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.tunc.androidlauncher.core.models.AppInfo
import com.tunc.androidlauncher.data.AppLockManager
import com.tunc.androidlauncher.data.RecentAppsManager
import com.tunc.androidlauncher.ui.components.NotificationBadge
import com.tunc.androidlauncher.ui.screens.launchersettings.applock.components.PinVerificationDialog
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BottomBar(
    apps: List<AppInfo?>,
    context: Context,
    appLockManager: AppLockManager? = null,
    iconSize: Int = 30,
    onAppsReordered: (List<AppInfo>) -> Unit = {},
    onAppRemovedFromBar: ((AppInfo) -> Unit)? = null,
    gridBounds: androidx.compose.ui.geometry.Rect? = null,
    onBoundsChanged: ((androidx.compose.ui.geometry.Rect) -> Unit)? = null,
    onDragOverlayStart: ((AppInfo, Offset, Int) -> Unit)? = null,
    onDragOverlayMove: ((Offset) -> Unit)? = null,
    onDragOverlayEnd: (() -> Unit)? = null
) {
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    val itemPositions = remember { mutableStateMapOf<Int, Offset>() }
    var hoveredIndex by remember { mutableStateOf<Int?>(null) }
    var startExactPos by remember { mutableStateOf(Offset.Zero) }

    // Apps listesi değiştiğinde eski state'leri temizle
    LaunchedEffect(apps.map { it?.packageName }) {
        draggedIndex = null
        dragOffset = Offset.Zero
        hoveredIndex = null
        itemPositions.clear()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.4f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(32.dp))
            .padding(vertical = 16.dp, horizontal = 24.dp)
            .onGloballyPositioned { coordinates ->
                val pos = coordinates.positionInRoot()
                val size = coordinates.size
                onBoundsChanged?.invoke(
                    androidx.compose.ui.geometry.Rect(
                        pos.x.toFloat(),
                        pos.y.toFloat(),
                        pos.x.toFloat() + size.width.toFloat(),
                        pos.y.toFloat() + size.height.toFloat()
                    )
                )
            },
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        apps.forEachIndexed { index, app ->
            key(app?.packageName ?: "empty_$index") {
                if (app != null) {
                    BottomIcon(
                        app = app,
                        context = context,
                        appLockManager = appLockManager,
                        iconSize = iconSize,
                        index = index,
                        isDragging = draggedIndex == index,
                        dragOffset = if (draggedIndex == index) dragOffset else Offset.Zero,
                        isHovered = hoveredIndex == index,
                        onPositionChanged = { position ->
                            itemPositions[index] = position
                        },
                        onDragStart = { exactPos ->
                            draggedIndex = index
                            startExactPos = exactPos
                            // Overlay başlat
                            if (app != null) {
                                onDragOverlayStart?.invoke(app, exactPos, iconSize)
                            }
                        },
                        onDrag = { offset ->
                            dragOffset += offset

                            // Overlay pozisyonunu güncelle
                            val exactDraggedPos = startExactPos + dragOffset
                            onDragOverlayMove?.invoke(exactDraggedPos)

                            // Hangi item üzerinde olduğumuzu kontrol et
                            val draggedPosition = itemPositions[index]?.plus(dragOffset)
                            draggedPosition?.let { pos ->
                                itemPositions.entries.forEachIndexed { idx, entry ->
                                    if (idx != index) {
                                        val itemPos = entry.value
                                        val itemWidth = iconSize * 2 // Approximate width
                                        if (pos.x > itemPos.x - itemWidth && pos.x < itemPos.x + itemWidth) {
                                            hoveredIndex = entry.key
                                        }
                                    }
                                }
                            }
                        },
                        onDragEnd = {
                            // Grid alanına düşürüldü mü kontrol et
                            val draggedPosition = itemPositions[index]?.plus(dragOffset)
                            val droppedOnGrid = draggedPosition != null && gridBounds?.contains(draggedPosition) == true
                            val draggedApp = apps.getOrNull(index)

                            if (droppedOnGrid && draggedApp != null && onAppRemovedFromBar != null) {
                                // Bottom bar'dan kaldır
                                onAppRemovedFromBar(draggedApp)
                            } else {
                                // Yeniden sıralama
                                hoveredIndex?.let { targetIndex ->
                                    if (targetIndex != index) {
                                        val mutableApps = apps.filterNotNull().toMutableList()
                                        val app = mutableApps[index]
                                        mutableApps.removeAt(index)
                                        mutableApps.add(targetIndex, app)
                                        onAppsReordered(mutableApps)
                                    }
                                }
                            }

                            draggedIndex = null
                            dragOffset = Offset.Zero
                            hoveredIndex = null
                            onDragOverlayEnd?.invoke()
                        }
                    )
                } else {
                    Spacer(modifier = Modifier.size(iconSize.dp))
                }
            }
        }
    }
}

@Composable
private fun BottomIcon(
    app: AppInfo,
    context: Context,
    appLockManager: AppLockManager?,
    iconSize: Int = 30,
    index: Int = 0,
    isDragging: Boolean = false,
    dragOffset: Offset = Offset.Zero,
    isHovered: Boolean = false,
    onPositionChanged: (Offset) -> Unit = {},
    onDragStart: (Offset) -> Unit = {},
    onDrag: (Offset) -> Unit = {},
    onDragEnd: () -> Unit = {}
) {
    var showPinDialog by remember { mutableStateOf(false) }
    val isLocked = appLockManager?.isAppLocked(app.packageName) == true && appLockManager.isPinSet()
    val recentAppsManager = remember { RecentAppsManager(context) }
    var exactIconPos by remember { mutableStateOf(Offset.Zero) }

    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .onGloballyPositioned { coordinates ->
                onPositionChanged(coordinates.positionInRoot())
            }
            .offset {
                IntOffset(
                    dragOffset.x.roundToInt(),
                    dragOffset.y.roundToInt()
                )
            }
            .graphicsLayer {
                scaleX = if (isDragging) 1.2f else if (isHovered) 0.9f else 1f
                scaleY = if (isDragging) 1.2f else if (isHovered) 0.9f else 1f
                alpha = if (isDragging) 0f else 1f
            }
    ) {
        Box(
            modifier = Modifier
                .size(iconSize.dp)
                .clip(RoundedCornerShape(14.dp))
                .onGloballyPositioned { coordinates ->
                    if (!isDragging) {
                        exactIconPos = coordinates.positionInRoot()
                    }
                }
                .pointerInput(app.packageName) {
                    detectDragGestures(
                        onDragStart = {
                            currentOnDragStart(exactIconPos)
                        },
                        onDrag = { change, offset ->
                            change.consume()
                            currentOnDrag(offset)
                        },
                        onDragEnd = {
                            currentOnDragEnd()
                        }
                    )
                }
                .clickable {
                    if (!isDragging) {
                        if (isLocked) {
                            showPinDialog = true
                        } else {
                            recentAppsManager.addRecentApp(app.packageName)
                            val launchIntent =
                                context.packageManager.getLaunchIntentForPackage(app.packageName)
                            launchIntent?.let { context.startActivity(it) }
                        }
                    }
                }
        ) {
            app.icon?.let { icon ->
                AsyncImage(
                    model = icon,
                    contentDescription = app.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }

        if (app.notificationCount > 0 && !isDragging) {
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
                recentAppsManager.addRecentApp(app.packageName)
                val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                launchIntent?.let { context.startActivity(it) }
            },
            verifyPin = { pin -> appLockManager.verifyPin(pin) }
        )
    }
}