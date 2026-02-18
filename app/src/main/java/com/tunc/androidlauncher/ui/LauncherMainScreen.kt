package com.tunc.androidlauncher.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.tunc.androidlauncher.ui.screens.appdrawer.AppDrawer
import com.tunc.androidlauncher.ui.screens.home.HomeScreen
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LauncherMainScreen(
    innerPadding: PaddingValues,
    onNavigateToSettings: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }

    val hiddenOffset = screenHeightPx

    var offsetY by remember { mutableStateOf(hiddenOffset) }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = {},
                        onDragEnd = {},
                        onVerticalDrag = { change, dragAmount ->
                            if (offsetY == hiddenOffset && dragAmount < -20) {
                                change.consume()
                                offsetY = 0f
                            }
                        }
                    )
                }
        ) {
            HomeScreen(innerPadding = innerPadding)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, offsetY.roundToInt()) }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            if (offsetY < screenHeightPx * 0.6f) {
                                offsetY = 0f
                            } else {
                                offsetY = hiddenOffset
                            }
                        },
                        onVerticalDrag = { _, dragAmount ->
                            val newOffset = (offsetY + dragAmount).coerceIn(0f, hiddenOffset)
                            offsetY = newOffset
                        }
                    )
                }
        ) {
            AppDrawer(
                innerPadding = innerPadding,
                onSettingsClick = onNavigateToSettings
            )
        }
    }
}
