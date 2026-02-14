package com.tunc.androidlauncher.ui.screens.themesettings.models

import com.tunc.androidlauncher.data.ThemeMode

data class ThemeOption(
    val mode: ThemeMode,
    val titleResId: Int,
    val descriptionResId: Int
)
