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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.tunc.androidlauncher.core.getInstalledApps
import com.tunc.androidlauncher.core.models.AppInfo
import com.tunc.androidlauncher.ui.screens.appdrawer.components.AlphabetSidebar
import com.tunc.androidlauncher.ui.screens.appdrawer.components.AppItem
import com.tunc.androidlauncher.ui.screens.appdrawer.components.SearchBarSection
import com.tunc.androidlauncher.ui.screens.appdrawer.components.SectionHeader
import com.tunc.androidlauncher.ui.screens.appdrawer.components.SettingsIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.collections.sortedBy


@Composable
fun AppDrawer(
    innerPadding: PaddingValues
) {
    val context = LocalContext.current
    var appList by remember { mutableStateOf<Map<String, List<AppInfo>>>(emptyMap()) }
    val letterIndexMap = remember { mutableStateMapOf<String, Int>() }
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val apps = getInstalledApps(context)
            val grouped = apps.sortedBy { it.label.lowercase() }
                .groupBy { it.label.first().uppercase() }

            appList = grouped
        }
    }

    LaunchedEffect(appList) {
        if (appList.isNotEmpty()) {
            var currentIndex = 0
            letterIndexMap.clear()
            appList.forEach { (letter, appsInGroup) ->
                letterIndexMap[letter] = currentIndex
                currentIndex += 1 + appsInGroup.size
            }
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
                    primary = MaterialTheme.colorScheme.primary,
                    surface = MaterialTheme.colorScheme.surface,
                    onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant,
                    onSurface = MaterialTheme.colorScheme.onSurface
                )
                SettingsIcon(primary = MaterialTheme.colorScheme.primary)
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                state = gridState,
                contentPadding = PaddingValues(
                    bottom = 20.dp,
                    start = 16.dp,
                    end = 28.dp
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                appList.forEach { (letter, appsInGroup) ->
                    item(span = { GridItemSpan(4) }) {
                        SectionHeader(
                            letter,
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.surface
                        )
                    }
                    items(appsInGroup) { app ->
                        AppItem(app, MaterialTheme.colorScheme.surface)
                    }
                }
            }
        }
        AlphabetSidebar(
            letters = appList.keys.toList(),
            primaryColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.CenterEnd),
            onLetterClick = { letter ->
                letterIndexMap[letter]?.let { index ->
                    coroutineScope.launch {
                        gridState.scrollToItem(index)
                    }
                }
            }
        )
    }
}