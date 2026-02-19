package com.tunc.androidlauncher.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_customizations")
data class AppCustomization(
    @PrimaryKey
    val packageName: String,
    val customIconUri: String? = null,
    val customName: String? = null
)
