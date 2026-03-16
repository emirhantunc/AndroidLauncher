package com.tunc.androidlauncher.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppFolderDao {
    @Query("SELECT * FROM app_folders ORDER BY sortIndex ASC")
    fun getAllFolders(): Flow<List<AppFolder>>

    @Query("SELECT * FROM app_folders ORDER BY sortIndex ASC")
    suspend fun getAllFoldersSync(): List<AppFolder>

    @Query("UPDATE app_folders SET sortIndex = :sortIndex WHERE id = :folderId")
    suspend fun updateFolderSortIndex(folderId: Long, sortIndex: Int)

    @Query("SELECT * FROM app_folders WHERE id = :folderId")
    suspend fun getFolderById(folderId: Long): AppFolder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: AppFolder): Long

    @Update
    suspend fun updateFolder(folder: AppFolder)

    @Delete
    suspend fun deleteFolder(folder: AppFolder)

    @Query("SELECT * FROM folder_apps WHERE folderId = :folderId")
    fun getAppsInFolder(folderId: Long): Flow<List<FolderApp>>

    @Query("SELECT * FROM folder_apps WHERE folderId = :folderId")
    suspend fun getAppsInFolderSync(folderId: Long): List<FolderApp>

    @Query("SELECT * FROM folder_apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getFolderByPackageName(packageName: String): FolderApp?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolderApp(folderApp: FolderApp)

    @Delete
    suspend fun deleteFolderApp(folderApp: FolderApp)

    @Query("DELETE FROM folder_apps WHERE folderId = :folderId AND packageName = :packageName")
    suspend fun removeAppFromFolder(folderId: Long, packageName: String)

    @Query("DELETE FROM folder_apps WHERE packageName = :packageName")
    suspend fun removeAppFromAllFolders(packageName: String)

    @Transaction
    @Query("SELECT * FROM app_folders")
    fun getFoldersWithApps(): Flow<List<FolderWithApps>>
}

data class FolderWithApps(
    @Embedded val folder: AppFolder,
    @Relation(
        parentColumn = "id",
        entityColumn = "folderId"
    )
    val apps: List<FolderApp>
)
