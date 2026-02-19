package com.tunc.androidlauncher.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_folders")
data class AppFolder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)
