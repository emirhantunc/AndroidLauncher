package com.tunc.androidlauncher.ui.screens.home.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tunc.androidlauncher.core.models.AppInfo
import com.tunc.androidlauncher.ui.screens.home.HomeApps
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class HomeViewModel : ViewModel() {
    private val _gridApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val gridApps = _gridApps.asStateFlow()


    fun loadApps(context: Context) {
        viewModelScope.launch {

            HomeApps.loadAppsIfNeeded(context)
            _gridApps.value = HomeApps.cachedGridApps

        }
    }
}