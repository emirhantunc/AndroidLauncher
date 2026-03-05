package com.tunc.androidlauncher.ui.screens.appdrawer.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.tunc.androidlauncher.core.models.AppInfo
import com.tunc.androidlauncher.data.AppLockManager
import com.tunc.androidlauncher.data.RecentAppsManager
import com.tunc.androidlauncher.ui.components.NotificationBadge
import com.tunc.androidlauncher.ui.screens.launchersettings.applock.components.PinVerificationDialog
import com.tunc.androidlauncher.utils.AppUninstaller

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DraggableAppItem(
    app: AppInfo,
    appLockManager: AppLockManager? = null,
    iconSize: Int = 40,
    bgColor: Color = MaterialTheme.colorScheme.background,
    labelSmall: TextStyle = MaterialTheme.typography.labelSmall,
    onBackGround: Color = MaterialTheme.colorScheme.onBackground,
    onSurface: Color = MaterialTheme.colorScheme.onSurface,
    editMode: Boolean = false,
    onEditModeChange: (Boolean) -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onDragStart: () -> Unit = {},
    onDrag: (Offset) -> Unit = {},
    onDragEnd: (Offset) -> Unit = {},
    onDragCancel: () -> Unit = {}
) {
    val context = LocalContext.current
    var showPinDialog by remember { mutableStateOf(false) }
    val isLocked = appLockManager?.isAppLocked(app.packageName) == true && appLockManager.isPinSet()

    var isDragging by remember { mutableStateOf(false) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }
    var itemPosition by remember { mutableStateOf(Offset.Zero) }
    var itemSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .onGloballyPositioned { coordinates ->
                itemPosition = coordinates.positionInRoot()
                itemSize = coordinates.size
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .graphicsLayer {
                    if (isDragging) {
                        translationX = dragOffset.x
                        translationY = dragOffset.y
                        alpha = 0.7f
                    }
                }
                .zIndex(if (isDragging) 1f else 0f)
                .combinedClickable(
                    onClick = {
                        if (isDragging) {
                            // Drag sırasında tıklama yapmaz
                        } else if (editMode) {
                            // Edit modunda tıklama yapmaz
                        } else if (isLocked) {
                            showPinDialog = true
                        } else {
                            launchApp(context, app.packageName)
                        }
                    },
                    onLongClick = {
                        // Long press ile drag başlatma ve edit mode'u kaldırdık
                        // Çünkü detectDragGesturesAfterLongPress zaten var
                    }
                )
                .pointerInput(Unit) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = {
                            isDragging = true
                            onDragStart()
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragOffset += dragAmount
                            onDrag(itemPosition + dragOffset + Offset(
                                itemSize.width / 2f,
                                itemSize.height / 2f
                            ))
                        },
                        onDragEnd = {
                            val finalPosition = itemPosition + dragOffset + Offset(
                                itemSize.width / 2f,
                                itemSize.height / 2f
                            )
                            onDragEnd(finalPosition)
                            isDragging = false
                            dragOffset = Offset.Zero
                        },
                        onDragCancel = {
                            isDragging = false
                            dragOffset = Offset.Zero
                            onDragCancel()
                        }
                    )
                }
        ) {
            // App icon ve silme butonu
            Box(contentAlignment = Alignment.TopStart) {
                Box(
                    modifier = Modifier
                        .size((iconSize + 20).dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(bgColor)
                        .then(if (isDragging) Modifier.shadow(8.dp, RoundedCornerShape(18.dp)) else Modifier),
                    contentAlignment = Alignment.Center
                ) {
                    app.icon?.let { icon ->
                        AsyncImage(
                            model = icon,
                            contentDescription = app.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(18.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } ?: run {
                        Text(app.label.take(1), color = onSurface, fontWeight = FontWeight.Bold)
                    }

                    if (app.notificationCount > 0 && !isDragging && !editMode) {
                        NotificationBadge(
                            count = app.notificationCount,
                            modifier = Modifier.align(Alignment.TopEnd)
                        )
                    }
                }

                // Silme butonu - Icon'un sol üst köşesinde
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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = app.name,
                style = labelSmall,
                color = onBackGround,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(80.dp)
            )
        }
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

private fun launchApp(context: android.content.Context, packageName: String) {
    val recentAppsManager = RecentAppsManager(context)
    recentAppsManager.addRecentApp(packageName)
    val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
    launchIntent?.let { context.startActivity(it) }
}
