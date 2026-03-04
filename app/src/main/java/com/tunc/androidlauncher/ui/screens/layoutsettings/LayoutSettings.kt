package com.tunc.androidlauncher.ui.screens.layoutsettings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tunc.androidlauncher.data.IconSize
import com.tunc.androidlauncher.data.LayoutManager
import kotlin.math.roundToInt

@Composable
fun LayoutSettings(
    innerPadding: PaddingValues,
    onBackClick: () -> Unit = {},
    onNavigateToCustomization: () -> Unit = {},
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    onBackgroundColor: Color = MaterialTheme.colorScheme.onBackground,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    surfaceColor: Color = MaterialTheme.colorScheme.surface,
    titleLargeStyle: TextStyle = MaterialTheme.typography.titleLarge,
    titleMediumStyle: TextStyle = MaterialTheme.typography.titleMedium,
    bodySmallStyle: TextStyle = MaterialTheme.typography.bodySmall
) {
    val context = LocalContext.current
    val layoutManager = remember { LayoutManager(context) }
    val selectedIconSize by layoutManager.iconSizeFlow.collectAsStateWithLifecycle()

    val iconSizeOptions = listOf(
        IconSize.SMALL,
        IconSize.MEDIUM,
        IconSize.LARGE
    )

    var offsetX by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(innerPadding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX > 100) {
                                onBackClick()
                            }
                            offsetX = 0f
                        },
                        onDragCancel = {
                            offsetX = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            val newOffset = offsetX + dragAmount
                            offsetX = if (newOffset > 0) newOffset else 0f
                        }
                    )
                }
                .padding(horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = onBackgroundColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LAYOUT",
                    style = titleLargeStyle.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    ),
                    color = onBackgroundColor
                )
            }

            Text(
                text = "ICON SIZE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                ),
                color = onBackgroundColor.copy(alpha = 0.3f),
                modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onNavigateToCustomization),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = primaryColor.copy(alpha = 0.15f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Customize Apps",
                                    style = titleMediumStyle.copy(fontWeight = FontWeight.Medium),
                                    color = primaryColor
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Change app icons and names",
                                    style = bodySmallStyle,
                                    color = onBackgroundColor.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }

                items(iconSizeOptions) { iconSize ->
                    IconSizeOptionItem(
                        iconSize = iconSize,
                        isSelected = selectedIconSize == iconSize,
                        onClick = {
                            layoutManager.setIconSize(iconSize)
                        },
                        primaryColor = primaryColor,
                        surfaceColor = surfaceColor,
                        onBackgroundColor = onBackgroundColor,
                        titleMediumStyle = titleMediumStyle,
                        bodySmallStyle = bodySmallStyle
                    )
                }
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
private fun IconSizeOptionItem(
    iconSize: IconSize,
    isSelected: Boolean,
    onClick: () -> Unit,
    primaryColor: Color,
    surfaceColor: Color,
    onBackgroundColor: Color,
    titleMediumStyle: TextStyle,
    bodySmallStyle: TextStyle
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) primaryColor.copy(alpha = 0.15f) else surfaceColor.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = iconSize.displayName,
                    style = titleMediumStyle.copy(fontWeight = FontWeight.Medium),
                    color = if (isSelected) primaryColor else onBackgroundColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "App Drawer: ${iconSize.appDrawerSize}dp | Home: ${iconSize.homeScreenSize}dp | Dock: ${iconSize.bottomBarSize}dp",
                    style = bodySmallStyle,
                    color = onBackgroundColor.copy(alpha = 0.6f)
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = primaryColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
