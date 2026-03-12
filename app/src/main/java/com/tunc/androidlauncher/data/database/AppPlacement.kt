package com.tunc.androidlauncher.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Uygulamaların home screen ve bottom bar'daki konumlarını tutar.
 *
 * sortIndex kuralları:
 * - 0, 1, 2, 3 → Bottom Bar slotları (max 4)
 * - 4, 5, 6, ... → Home Grid uygulamaları (sıralı)
 *
 * Bir uygulama ya bottom bar'dadır ya da grid'de. İkisinde birden olamaz.
 */
@Entity(tableName = "app_placements")
data class AppPlacement(
    @PrimaryKey
    val packageName: String,
    val sortIndex: Int
)
