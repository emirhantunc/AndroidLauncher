package com.tunc.androidlauncher.ui.screens.launchersettings.models

import androidx.annotation.StringRes


data class SettingsTitleModel(
    val id : String,
    @StringRes val titleResId: Int,
    val settings: List<SettingModel>
)