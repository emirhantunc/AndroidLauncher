package com.tunc.androidlauncher.data

import android.content.Context
import com.tunc.androidlauncher.R
import com.tunc.androidlauncher.data.database.AppDatabase
import com.tunc.androidlauncher.data.database.AppFolder
import com.tunc.androidlauncher.data.database.FolderApp
import com.tunc.androidlauncher.data.database.FolderWithApps
import kotlinx.coroutines.flow.Flow

class FolderManager(private val context: Context) {
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

    suspend fun createFolder(name: String, sortIndex: Int = 0): Long {
        return dao.insertFolder(AppFolder(name = name, sortIndex = sortIndex))
    }

    suspend fun updateFolderName(folderId: Long, newName: String) {
        val folder = dao.getFolderById(folderId)
        folder?.let {
            dao.updateFolder(it.copy(name = newName))
        }
    }

    suspend fun updateFolderSortIndex(folderId: Long, sortIndex: Int) {
        dao.updateFolderSortIndex(folderId, sortIndex)
    }

    suspend fun getAllFoldersSync(): List<AppFolder> {
        return dao.getAllFoldersSync()
    }

    suspend fun getFolderByIdSync(folderId: Long): AppFolder? {
        return dao.getFolderById(folderId)
    }

    suspend fun deleteFolder(folderId: Long) {
        val folder = dao.getFolderById(folderId)
        folder?.let {
            dao.deleteFolder(it)
        }
    }

    suspend fun addAppToFolder(folderId: Long, packageName: String) {
        // Önce uygulamanın mevcut klasörünü bul
        val currentFolder = dao.getFolderByPackageName(packageName)

        // Eğer uygulama başka bir klasördeyse ve farklı bir klasöre taşınıyorsa
        if (currentFolder != null && currentFolder.folderId != folderId) {
            // Eski klasörden manuel olarak çıkar (otomatik silme olmadan)
            dao.removeAppFromFolder(currentFolder.folderId, packageName)

            // Eski klasörde uygulama kalmadıysa klasörü sil
            val appsInOldFolder = dao.getAppsInFolderSync(currentFolder.folderId)
            if (appsInOldFolder.isEmpty()) {
                val oldFolder = dao.getFolderById(currentFolder.folderId)
                oldFolder?.let { dao.deleteFolder(it) }
            }
        }

        // Yeni klasöre ekle
        dao.insertFolderApp(FolderApp(folderId = folderId, packageName = packageName))
    }

    suspend fun removeAppFromFolder(folderId: Long, packageName: String) {
        dao.removeAppFromFolder(folderId, packageName)

        // Klasörde uygulama kalmadıysa klasörü sil
        val folder = dao.getFolderById(folderId)
        folder?.let {
            val appsInFolder = dao.getAppsInFolderSync(folderId)
            if (appsInFolder.isEmpty()) {
                dao.deleteFolder(it)
            }
        }
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
            packageName.contains("linkedin", ignoreCase = true) -> context.getString(R.string.folder_category_social)

            packageName.contains("youtube", ignoreCase = true) ||
            packageName.contains("netflix", ignoreCase = true) ||
            packageName.contains("spotify", ignoreCase = true) ||
            packageName.contains("music", ignoreCase = true) ||
            packageName.contains("video", ignoreCase = true) ||
            packageName.contains("media", ignoreCase = true) -> context.getString(R.string.folder_category_entertainment)

            packageName.contains("game", ignoreCase = true) ||
            packageName.contains("play", ignoreCase = true) -> context.getString(R.string.folder_category_games)

            packageName.contains("camera", ignoreCase = true) ||
            packageName.contains("photo", ignoreCase = true) ||
            packageName.contains("gallery", ignoreCase = true) -> context.getString(R.string.folder_category_photo)

            packageName.contains("mail", ignoreCase = true) ||
            packageName.contains("gmail", ignoreCase = true) ||
            packageName.contains("outlook", ignoreCase = true) -> context.getString(R.string.folder_category_mail)

            packageName.contains("chrome", ignoreCase = true) ||
            packageName.contains("browser", ignoreCase = true) ||
            packageName.contains("firefox", ignoreCase = true) -> context.getString(R.string.folder_category_web)

            packageName.contains("maps", ignoreCase = true) ||
            packageName.contains("navigation", ignoreCase = true) -> context.getString(R.string.folder_category_maps)

            packageName.contains("shop", ignoreCase = true) ||
            packageName.contains("store", ignoreCase = true) ||
            packageName.contains("amazon", ignoreCase = true) -> context.getString(R.string.folder_category_shopping)

            packageName.contains("news", ignoreCase = true) ||
            packageName.contains("haber", ignoreCase = true) -> context.getString(R.string.folder_category_news)

            packageName.contains("bank", ignoreCase = true) ||
            packageName.contains("finance", ignoreCase = true) -> context.getString(R.string.folder_category_finance)

            else -> context.getString(R.string.folder_category_general)
        }
    }
}
