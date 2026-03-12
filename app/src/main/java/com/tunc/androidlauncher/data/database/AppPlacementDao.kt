package com.tunc.androidlauncher.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface AppPlacementDao {

    @Query("SELECT * FROM app_placements ORDER BY sortIndex ASC")
    fun getAllPlacements(): Flow<List<AppPlacement>>

    @Query("SELECT * FROM app_placements ORDER BY sortIndex ASC")
    suspend fun getAllPlacementsSync(): List<AppPlacement>

    /** Bottom bar uygulamaları (index 0-3) */
    @Query("SELECT * FROM app_placements WHERE sortIndex BETWEEN 0 AND 3 ORDER BY sortIndex ASC")
    fun getBottomBarPlacements(): Flow<List<AppPlacement>>

    /** Grid uygulamaları (index >= 4) */
    @Query("SELECT * FROM app_placements WHERE sortIndex >= 4 ORDER BY sortIndex ASC")
    fun getGridPlacements(): Flow<List<AppPlacement>>

    @Query("SELECT * FROM app_placements WHERE packageName = :packageName LIMIT 1")
    suspend fun getPlacement(packageName: String): AppPlacement?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlacement(placement: AppPlacement)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(placements: List<AppPlacement>)

    @Query("DELETE FROM app_placements WHERE packageName = :packageName")
    suspend fun deletePlacement(packageName: String)

    @Query("DELETE FROM app_placements")
    suspend fun deleteAll()

    @Query("SELECT MAX(sortIndex) FROM app_placements")
    suspend fun getMaxIndex(): Int?

    @Query("SELECT COUNT(*) FROM app_placements WHERE sortIndex BETWEEN 0 AND 3")
    suspend fun getBottomBarCount(): Int

    @Transaction
    suspend fun replaceAll(placements: List<AppPlacement>) {
        deleteAll()
        insertAll(placements)
    }
}
