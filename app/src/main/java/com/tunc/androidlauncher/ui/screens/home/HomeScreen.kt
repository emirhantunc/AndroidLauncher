package com.tunc.androidlauncher.ui.screens.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tunc.androidlauncher.core.findApp
import com.tunc.androidlauncher.core.getInstalledApps
import com.tunc.androidlauncher.core.models.AppInfo
import com.tunc.androidlauncher.ui.screens.home.components.BottomBar
import com.tunc.androidlauncher.ui.screens.home.components.HomeGrid
import com.tunc.androidlauncher.ui.screens.home.components.HomeSearchBar
import com.tunc.androidlauncher.ui.screens.home.components.LockScreenClock
import com.tunc.androidlauncher.ui.screens.home.viewmodels.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.collections.isNotEmpty


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    innerPadding: PaddingValues,
    backGround: Color = MaterialTheme.colorScheme.background,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadApps(context)
    }

    val gridApps by viewModel.gridApps.collectAsStateWithLifecycle()
    val dockApps = gridApps

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backGround)
            .padding(16.dp)
            .padding(innerPadding)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HomeSearchBar()

            Spacer(modifier = Modifier.height(26.dp))

            LockScreenClock()

            Spacer(modifier = Modifier.height(14.dp))

            if (gridApps.isNotEmpty()) {
                HomeGrid(
                    apps = gridApps,
                    context = context,
                    )
            }

            Spacer(modifier = Modifier.weight(1f))

            BottomBar(
                apps = dockApps,
                context = context,
            )
        }
    }
}







