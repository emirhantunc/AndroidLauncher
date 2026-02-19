package com.tunc.androidlauncher.data

import android.content.Context
import com.tunc.androidlauncher.data.database.AppDatabase
import com.tunc.androidlauncher.data.database.AppFolder
import com.tunc.androidlauncher.data.database.FolderApp
import com.tunc.androidlauncher.data.database.FolderWithApps
import kotlinx.coroutines.flow.Flow

class FolderManager(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val dao = database.appFolderDao()

    fun getAllFolders(): Flow<List<AppFolder>> {
        return dao.getAllFolders()
    }

    fun getFoldersWithApps(): Flow<List<FolderWithApps>> {
        return dao.getFoldersWithApps()
    }

    fun getAppsInFolder(folderId: Long): Flow<List<FolderApp>> {
        return dao.getAppsInFolder(folderId)
    }

    suspend fun createFolder(name: String): Long {
        return dao.insertFolder(AppFolder(name = name))
    }

    suspend fun updateFolderName(folderId: Long, newName: String) {
        val folder = dao.getFolderById(folderId)
        folder?.let {
            dao.updateFolder(it.copy(name = newName))
        }
    }

    suspend fun deleteFolder(folderId: Long) {
        val folder = dao.getFolderById(folderId)
        folder?.let {
            dao.deleteFolder(it)
        }
    }

    suspend fun addAppToFolder(folderId: Long, packageName: String) {
        dao.removeAppFromAllFolders(packageName)
        dao.insertFolderApp(FolderApp(folderId = folderId, packageName = packageName))
    }

    suspend fun removeAppFromFolder(folderId: Long, packageName: String) {
        dao.removeAppFromFolder(folderId, packageName)
    }

    suspend fun isAppInFolder(packageName: String): Boolean {
        return dao.getFolderByPackageName(packageName) != null
    }

    suspend fun getFolderByPackageName(packageName: String): FolderApp? {
        return dao.getFolderByPackageName(packageName)
    }

    fun getCategoryForPackage(packageName: String): String {
        return when {
            packageName.contains("instagram", ignoreCase = true) ||
            packageName.contains("facebook", ignoreCase = true) ||
            packageName.contains("twitter", ignoreCase = true) ||
            packageName.contains("whatsapp", ignoreCase = true) ||
            packageName.contains("telegram", ignoreCase = true) ||
            packageName.contains("snapchat", ignoreCase = true) ||
            packageName.contains("tiktok", ignoreCase = true) ||
            packageName.contains("linkedin", ignoreCase = true) -> "Sosyal"

            packageName.contains("youtube", ignoreCase = true) ||
            packageName.contains("netflix", ignoreCase = true) ||
            packageName.contains("spotify", ignoreCase = true) ||
            packageName.contains("music", ignoreCase = true) ||
            packageName.contains("video", ignoreCase = true) ||
            packageName.contains("media", ignoreCase = true) -> "Eğlence"

            packageName.contains("game", ignoreCase = true) ||
            packageName.contains("play", ignoreCase = true) -> "Oyun"

            packageName.contains("camera", ignoreCase = true) ||
            packageName.contains("photo", ignoreCase = true) ||
            packageName.contains("gallery", ignoreCase = true) -> "Fotoğraf"

            packageName.contains("mail", ignoreCase = true) ||
            packageName.contains("gmail", ignoreCase = true) ||
            packageName.contains("outlook", ignoreCase = true) -> "Mail"

            packageName.contains("chrome", ignoreCase = true) ||
            packageName.contains("browser", ignoreCase = true) ||
            packageName.contains("firefox", ignoreCase = true) -> "Web"

            packageName.contains("maps", ignoreCase = true) ||
            packageName.contains("navigation", ignoreCase = true) -> "Haritalar"

            packageName.contains("shop", ignoreCase = true) ||
            packageName.contains("store", ignoreCase = true) ||
            packageName.contains("amazon", ignoreCase = true) -> "Alışveriş"

            packageName.contains("news", ignoreCase = true) ||
            packageName.contains("haber", ignoreCase = true) -> "Haberler"

            packageName.contains("bank", ignoreCase = true) ||
            packageName.contains("finance", ignoreCase = true) -> "Finans"

            else -> "Genel"
        }
    }
}
