package com.tunc.androidlauncher.ui.screens.launchersettings.models

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector


data class SettingModel(
    val id: String,
    @StringRes val titleResId: Int
)