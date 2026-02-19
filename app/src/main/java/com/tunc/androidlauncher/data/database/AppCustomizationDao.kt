package com.tunc.androidlauncher.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppCustomizationDao {

    @Query("SELECT * FROM app_customizations WHERE packageName = :packageName")
    fun getCustomization(packageName: String): Flow<AppCustomization?>

    @Query("SELECT * FROM app_customizations")
    fun getAllCustomizations(): Flow<List<AppCustomization>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomization(customization: AppCustomization)

    @Update
    suspend fun updateCustomization(customization: AppCustomization)

    @Query("DELETE FROM app_customizations WHERE packageName = :packageName")
    suspend fun deleteCustomization(packageName: String)

    @Query("UPDATE app_customizations SET customIconUri = :iconUri WHERE packageName = :packageName")
    suspend fun updateIcon(packageName: String, iconUri: String?)

    @Query("UPDATE app_customizations SET customName = :name WHERE packageName = :packageName")
    suspend fun updateName(packageName: String, name: String?)
}
