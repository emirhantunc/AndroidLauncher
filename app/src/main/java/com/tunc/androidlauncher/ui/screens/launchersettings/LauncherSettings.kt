package com.tunc.androidlauncher.ui.screens.launchersettings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tunc.androidlauncher.R
import java.util.Locale
import kotlin.math.roundToInt


@Composable
fun LauncherSettings(
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues,
    onNavigateToAppLock: () -> Unit = {},
    onNavigateToTheme: () -> Unit = {},
    onNavigateToHiddenApps: () -> Unit = {},
    onNavigateToLayout: () -> Unit = {},
    onNavigateToLanguage: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onSettingClicked: (String) -> Unit = {},
    backGroundColor: Color = MaterialTheme.colorScheme.background,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    titleLargeStyle: TextStyle = MaterialTheme.typography.titleLarge,
    titleMediumStyle: TextStyle = MaterialTheme.typography.titleMedium,
) {
    var searchQuery by remember { mutableStateOf("") }
    val settingsList = SettingsDataSource.menuData

    val settingsWithTitles = remember(settingsList) {
        settingsList.flatMap { section ->
            section.settings.map { setting ->
                setting to section
            }
        }
    }

    val settingTitles = settingsWithTitles.associate { (setting, _) ->
        setting.id to stringResource(setting.titleResId)
    }

    val filteredSettingsList = remember(searchQuery, settingsList, settingTitles) {
        if (searchQuery.isEmpty()) {
            settingsList
        } else {
            val query = searchQuery.lowercase(Locale.getDefault())
            settingsList.mapNotNull { section ->
                val filteredSettings = section.settings.filter { setting ->
                    settingTitles[setting.id]
                        ?.lowercase(Locale.getDefault())
                        ?.contains(query) == true
                }
                if (filteredSettings.isNotEmpty()) {
                    section.copy(settings = filteredSettings)
                } else {
                    null
                }
            }
        }
    }

    val settingClickHandler: (String) -> Unit = { settingId ->
        onSettingClicked(settingId)
        when (settingId) {
            "app_lock" -> onNavigateToAppLock()
            "theme" -> onNavigateToTheme()
            "hidden_apps" -> onNavigateToHiddenApps()
            "layout" -> onNavigateToLayout()
            "language" -> onNavigateToLanguage()
        }
    }

    var offsetX by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backGroundColor)
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
            Column(modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = titleColor
                        )
                    }
                    Text(
                        text = stringResource(R.string.settings).uppercase(),
                        style = titleLargeStyle.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        ),
                        color = titleColor
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                SettingsSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onClear = { searchQuery = "" },
                    titleColor = titleColor
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                filteredSettingsList.forEach { section ->

                    item(key = "header_${section.id}") {
                        Column(modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)) {
                            Text(
                                text = stringResource(section.titleResId).uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 3.sp
                                ),
                                color = titleColor.copy(alpha = 0.3f),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            HorizontalDivider(
                                color = titleColor.copy(alpha = 0.1f),
                                thickness = 1.dp
                            )
                        }
                    }

                    items(
                        items = section.settings,
                        key = { setting -> setting.id }
                    ) { setting ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    settingClickHandler(setting.id)
                                }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringResource(setting.titleResId),
                                    style = titleMediumStyle.copy(fontWeight = FontWeight.Normal),
                                    color = titleColor
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = titleColor.copy(alpha = 0.2f)
                            )
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(20.dp)) }
            }
        }
    }
}

@Composable
private fun SettingsSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    titleColor: Color,
    surface: Color = MaterialTheme.colorScheme.surface,
    onSurfaceVariant: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(surface)
            .border(1.dp, titleColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = titleColor
        ),
        cursorBrush = SolidColor(titleColor),
        singleLine = true,
        decorationBox = { innerTextField ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.padding(horizontal = 8.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(R.string.search_settings),
                            style = MaterialTheme.typography.bodyMedium,
                            color = onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    innerTextField()
                }

                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    )
}

