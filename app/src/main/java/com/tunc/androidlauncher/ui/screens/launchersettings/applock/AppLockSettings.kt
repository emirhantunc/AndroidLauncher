package com.tunc.androidlauncher.ui.screens.launchersettings.applock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tunc.androidlauncher.R
import com.tunc.androidlauncher.core.models.AppInfo
import com.tunc.androidlauncher.data.AppManager
import com.tunc.androidlauncher.ui.screens.launchersettings.applock.components.AppLockItem
import com.tunc.androidlauncher.ui.screens.launchersettings.applock.components.PinInputSection
import com.tunc.androidlauncher.ui.screens.launchersettings.applock.models.AppLockSettings
import com.tunc.androidlauncher.ui.screens.launchersettings.applock.viewmodels.AppLockViewModel

enum class AppLockScreen {
    MAIN, SET_PIN, CONFIRM_PIN, CHANGE_PIN
}

@Composable
fun AppLockSettings(
    innerPadding: PaddingValues,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: AppLockViewModel = viewModel(),
    backgroundColor: Color = MaterialTheme.colorScheme.background
) {
    val context = LocalContext.current
    val appManager = remember { AppManager.getInstance(context) }
    val apps by appManager.allApps.collectAsStateWithLifecycle()
    var currentScreen by remember { mutableStateOf(AppLockScreen.MAIN) }
    var tempPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }

    val appLockSettings by viewModel.appLockSettings.collectAsState()

    LaunchedEffect(Unit) {
        appManager.loadApps()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(innerPadding)
    ) {
        when (currentScreen) {
            AppLockScreen.MAIN -> {
                MainAppLockScreen(
                    apps = apps,
                    appLockSettings = appLockSettings,
                    viewModel = viewModel,
                    onBackClick = onBackClick,
                    onSetPinClick = {
                        currentScreen = if (viewModel.isPinSet()) {
                            AppLockScreen.CHANGE_PIN
                        } else {
                            AppLockScreen.SET_PIN
                        }
                    },
                )
            }

            AppLockScreen.SET_PIN -> {
                PinInputSection(
                    pin = tempPin,
                    onPinChange = {
                        tempPin = it
                        if (it.length == 4) {
                            currentScreen = AppLockScreen.CONFIRM_PIN
                        }
                    },
                    title = stringResource(R.string.app_lock_enter_pin),
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            AppLockScreen.CONFIRM_PIN -> {
                PinInputSection(
                    pin = confirmPin,
                    onPinChange = {
                        confirmPin = it
                        if (it.length == 4) {
                            if (it == tempPin) {
                                viewModel.setPin(tempPin)
                                tempPin = ""
                                confirmPin = ""
                                currentScreen = AppLockScreen.MAIN
                            } else {
                                confirmPin = ""
                                tempPin = ""
                                currentScreen = AppLockScreen.SET_PIN
                            }
                        }
                    },
                    title = stringResource(R.string.app_lock_confirm_pin),
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            AppLockScreen.CHANGE_PIN -> {
                PinInputSection(
                    pin = tempPin,
                    onPinChange = {
                        tempPin = it
                        if (it.length == 4) {
                            if (viewModel.verifyPin(it)) {
                                tempPin = ""
                                currentScreen = AppLockScreen.SET_PIN
                            } else {
                                tempPin = ""
                            }
                        }
                    },
                    title = stringResource(R.string.app_lock_enter_current_pin),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun MainAppLockScreen(
    apps: List<AppInfo>,
    appLockSettings: AppLockSettings,
    viewModel: AppLockViewModel,
    onBackClick: () -> Unit,
    onSetPinClick: () -> Unit,
    onBackgroundColor: Color = MaterialTheme.colorScheme.onBackground,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    surfaceColor: Color = MaterialTheme.colorScheme.surface,
    onSurfaceColor : Color = MaterialTheme.colorScheme.onSurface,
    titleLargeStyle: TextStyle = MaterialTheme.typography.titleLarge,
    titleMediumStyle: TextStyle = MaterialTheme.typography.titleMedium,
    bodySmallStyle: TextStyle = MaterialTheme.typography.bodySmall,
    labelSmallStyle: TextStyle = MaterialTheme.typography.labelSmall,
    bodyMediumStyle : TextStyle = MaterialTheme.typography.bodyMedium,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
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
                text = stringResource(R.string.app_lock_title).uppercase(),
                style = titleLargeStyle.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                ),
                color = onBackgroundColor
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = surfaceColor.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (appLockSettings.isPinSet) {
                                stringResource(R.string.app_lock_pin_set)
                            } else {
                                stringResource(R.string.app_lock_pin_not_set)
                            },
                            style = titleMediumStyle.copy(fontWeight = FontWeight.Medium),
                            color = onBackgroundColor
                        )
                        Text(
                            text = stringResource(R.string.app_lock_subtitle),
                            style = bodySmallStyle,
                            color = onBackgroundColor.copy(alpha = 0.6f)
                        )
                    }
                    Button(
                        onClick = onSetPinClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (appLockSettings.isPinSet) {
                                stringResource(R.string.app_lock_change_pin)
                            } else {
                                stringResource(R.string.app_lock_set_pin)
                            },
                            style = bodyMediumStyle,
                            color = onSurfaceColor
                        )
                    }
                }
            }
        }

        Text(
            text = stringResource(R.string.app_lock_select_apps).uppercase(),
            style = labelSmallStyle.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            ),
            color = onBackgroundColor.copy(alpha = 0.3f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(apps, key = { it.packageName }) { app ->
                AppLockItem(
                    app = app,
                    isLocked = viewModel.isAppLocked(app.packageName),
                    onToggleLock = {
                        if (appLockSettings.isPinSet) {
                            viewModel.toggleAppLock(app.packageName)
                        }
                    },
                )
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}
