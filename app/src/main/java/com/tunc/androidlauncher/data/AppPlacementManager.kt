package com.tunc.androidlauncher.data

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.tunc.androidlauncher.core.models.AppInfo
import com.tunc.androidlauncher.data.database.AppDatabase
import com.tunc.androidlauncher.data.database.AppPlacement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Uygulama yerleşimlerini yöneten singleton manager.
 *
 * Index kuralları:
 * - 0, 1, 2, 3 → Bottom Bar (max 4 slot)
 * - 4, 5, 6, ... → Home Grid uygulamaları
 *
 * Bir uygulama aynı anda sadece bir yerde bulunabilir.
 */
@SuppressLint("StaticFieldLeak")
class AppPlacementManager private constructor(context: Context) {

    companion object {
        private const val TAG = "AppPlacementManager"
        const val BOTTOM_BAR_MAX_INDEX = 3   // 0..3 = 4 slot
        const val GRID_START_INDEX = 4       // Grid ilk index

        @Volatile
        private var INSTANCE: AppPlacementManager? = null

        fun getInstance(context: Context): AppPlacementManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppPlacementManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    private val database = AppDatabase.getDatabase(context)
    private val dao = database.appPlacementDao()

    // Tüm placement'ları dinleyen flow
    val allPlacementsFlow: Flow<List<AppPlacement>> = dao.getAllPlacements()

    // Bottom bar package name'leri (index 0-3, sıralı)
    val bottomBarFlow: Flow<List<AppPlacement>> = dao.getBottomBarPlacements()

    // Grid package name'leri (index >= 4, sıralı)
    val gridFlow: Flow<List<AppPlacement>> = dao.getGridPlacements()

    // İç cache
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized = _isInitialized.asStateFlow()

    /**
     * İlk kurulum: Eğer placement tablosu boşsa, mevcut uygulamaları yerleştir.
     * Bottom bar'a ilk 4 rastgele uygulama, geri kalanı grid'e isim sırasına göre.
     */
    suspend fun initializeIfNeeded(allApps: List<AppInfo>) {
        val existing = dao.getAllPlacementsSync()
        if (existing.isNotEmpty()) {
            // Yeni eklenen uygulamaları kontrol et ve grid sonuna ekle
            syncWithInstalledApps(allApps, existing)
            _isInitialized.value = true
            return
        }

        if (allApps.isEmpty()) return

        Log.d(TAG, "Initializing placements for ${allApps.size} apps")

        val sorted = allApps.sortedBy { it.name.lowercase() }

        // İlk 4 uygulamayı bottom bar'a ata
        val bottomBarApps = sorted.take(4)
        val gridApps = sorted.drop(4)

        val placements = mutableListOf<AppPlacement>()

        bottomBarApps.forEachIndexed { index, app ->
            placements.add(AppPlacement(packageName = app.packageName, sortIndex = index))
        }

        gridApps.forEachIndexed { index, app ->
            placements.add(AppPlacement(packageName = app.packageName, sortIndex = GRID_START_INDEX + index))
        }

        dao.replaceAll(placements)
        _isInitialized.value = true
        Log.d(TAG, "Initialized ${placements.size} placements (${bottomBarApps.size} bottom bar, ${gridApps.size} grid)")
    }

    /**
     * Yeni yüklenen/kaldırılan uygulamaları placement ile senkronize et.
     */
    private suspend fun syncWithInstalledApps(allApps: List<AppInfo>, existing: List<AppPlacement>) {
        val existingPackages = existing.map { it.packageName }.toSet()
        val installedPackages = allApps.map { it.packageName }.toSet()

        // Yeni yüklenen uygulamaları grid sonuna ekle
        val newApps = installedPackages - existingPackages
        if (newApps.isNotEmpty()) {
            val maxIndex = dao.getMaxIndex() ?: (GRID_START_INDEX - 1)
            var nextIndex = maxIndex + 1
            newApps.forEach { packageName ->
                dao.insertPlacement(AppPlacement(packageName = packageName, sortIndex = nextIndex++))
            }
            Log.d(TAG, "Added ${newApps.size} new apps to grid")
        }

        // Kaldırılmış uygulamaları temizle
        val removedApps = existingPackages - installedPackages
        if (removedApps.isNotEmpty()) {
            removedApps.forEach { packageName ->
                dao.deletePlacement(packageName)
            }
            // Index'leri yeniden düzenle
            reindexAll()
            Log.d(TAG, "Removed ${removedApps.size} uninstalled apps")
        }
    }

    /**
     * Uygulamayı bottom bar'a taşı. Grid'den çıkarır.
     * @param position bottom bar'daki slot (0-3)
     */
    suspend fun moveToBottomBar(packageName: String, position: Int) {
        val targetIndex = position.coerceIn(0, BOTTOM_BAR_MAX_INDEX)

        // Eğer hedef slot'ta başka bir uygulama varsa, onu grid başına taşı
        val currentAtTarget = dao.getAllPlacementsSync().find { it.sortIndex == targetIndex }
        if (currentAtTarget != null && currentAtTarget.packageName != packageName) {
            // Mevcut uygulamayı grid başına taşı
            moveToGrid(currentAtTarget.packageName, GRID_START_INDEX)
        }

        // Önce mevcut placement'ı sil
        dao.deletePlacement(packageName)

        // Bottom bar'a yerleştir
        dao.insertPlacement(AppPlacement(packageName = packageName, sortIndex = targetIndex))

        Log.d(TAG, "Moved $packageName to bottom bar position $targetIndex")
    }

    /**
     * Uygulamayı grid'e taşı. Bottom bar'dan çıkarır.
     * @param desiredIndex istenen grid index'i (>= GRID_START_INDEX)
     */
    suspend fun moveToGrid(packageName: String, desiredIndex: Int = -1) {
        // Önce mevcut placement'ı sil
        dao.deletePlacement(packageName)

        val index = if (desiredIndex < GRID_START_INDEX) {
            // Grid sonuna ekle
            (dao.getMaxIndex() ?: (GRID_START_INDEX - 1)) + 1
        } else {
            desiredIndex
        }

        dao.insertPlacement(AppPlacement(packageName = packageName, sortIndex = index))
        Log.d(TAG, "Moved $packageName to grid index $index")
    }

    /**
     * Bottom bar uygulamalarını yeniden sırala.
     */
    suspend fun reorderBottomBar(packageNames: List<String>) {
        val placements = packageNames.take(4).mapIndexed { index, pkg ->
            AppPlacement(packageName = pkg, sortIndex = index)
        }
        // Sadece bottom bar slot'larını güncelle
        placements.forEach { dao.insertPlacement(it) }
        Log.d(TAG, "Reordered bottom bar: $packageNames")
    }

    /**
     * Bottom bar'dan uygulamayı çıkar, grid sonuna taşı.
     */
    suspend fun removeFromBottomBar(packageName: String) {
        moveToGrid(packageName)
        reindexAll()
        Log.d(TAG, "Removed $packageName from bottom bar to grid")
    }

    /**
     * Grid'den uygulamayı çıkar, uygun bottom bar slot'una taşı.
     * Boş slot yoksa son slot'taki uygulamayla yer değiştirir.
     */
    suspend fun moveFromGridToBottomBar(packageName: String) {
        val allPlacements = dao.getAllPlacementsSync()
        val bottomBarPlacements = allPlacements.filter { it.sortIndex in 0..BOTTOM_BAR_MAX_INDEX }

        if (bottomBarPlacements.size < 4) {
            // Boş slot var, oraya ekle
            val usedSlots = bottomBarPlacements.map { it.sortIndex }.toSet()
            val emptySlot = (0..BOTTOM_BAR_MAX_INDEX).first { it !in usedSlots }
            moveToBottomBar(packageName, emptySlot)
        } else {
            // Dolu, son slot'taki ile yer değiştir
            val lastSlotApp = bottomBarPlacements.maxByOrNull { it.sortIndex }
            if (lastSlotApp != null) {
                val slotIndex = lastSlotApp.sortIndex
                moveToGrid(lastSlotApp.packageName)
                moveToBottomBar(packageName, slotIndex)
            }
        }
    }

    /**
     * İki uygulamanın yerini değiştir (hem bottom bar içi hem grid içi hem çapraz)
     */
    suspend fun swapApps(packageName1: String, packageName2: String) {
        val placement1 = dao.getPlacement(packageName1)
        val placement2 = dao.getPlacement(packageName2)

        if (placement1 != null && placement2 != null) {
            dao.insertPlacement(AppPlacement(packageName = packageName1, sortIndex = placement2.sortIndex))
            dao.insertPlacement(AppPlacement(packageName = packageName2, sortIndex = placement1.sortIndex))
            Log.d(TAG, "Swapped $packageName1 (${placement1.sortIndex}) <-> $packageName2 (${placement2.sortIndex})")
        }
    }

    /**
     * Uygulama kaldırıldığında placement'ı sil ve reindex yap.
     */
    suspend fun onAppUninstalled(packageName: String) {
        dao.deletePlacement(packageName)
        reindexAll()
        Log.d(TAG, "Removed uninstalled app: $packageName")
    }

    /**
     * Tüm index'leri yeniden düzenle (gap'leri kapat).
     * Bottom bar: 0-3 (sırasıyla), Grid: 4+ (sırasıyla)
     */
    private suspend fun reindexAll() {
        val all = dao.getAllPlacementsSync().sortedBy { it.sortIndex }
        val bottomBar = all.filter { it.sortIndex in 0..BOTTOM_BAR_MAX_INDEX }
        val grid = all.filter { it.sortIndex >= GRID_START_INDEX }

        val reindexed = mutableListOf<AppPlacement>()

        bottomBar.forEachIndexed { index, placement ->
            reindexed.add(placement.copy(sortIndex = index))
        }

        grid.forEachIndexed { index, placement ->
            reindexed.add(placement.copy(sortIndex = GRID_START_INDEX + index))
        }

        dao.replaceAll(reindexed)
    }
}
