package com.tunc.androidlauncher.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.net.toUri

object AppUninstaller {
    private const val TAG = "AppUninstaller"

    /**
     * Core sistem uygulamalarının package name'leri
     * Bu uygulamalar silinemez
     */
    private val CORE_SYSTEM_APPS = setOf(
        // Android sistem uygulamaları
        "com.android.settings",          // Ayarlar
        "com.android.camera",            // Kamera (AOSP)
        "com.android.camera2",           // Kamera2 (AOSP)
        "com.google.android.camera",     // Google Kamera
        "com.android.gallery",           // Galeri (AOSP)
        "com.android.gallery3d",         // Galeri 3D (AOSP)
        "com.google.android.apps.photos", // Google Photos
        "com.android.contacts",          // Rehber (AOSP)
        "com.google.android.contacts",   // Google Contacts
        "com.android.dialer",            // Telefon (AOSP)
        "com.google.android.dialer",     // Google Telefon
        "com.android.mms",               // Mesajlar (AOSP)
        "com.google.android.apps.messaging", // Google Messages
        "com.android.systemui",          // Sistem UI
        "com.android.launcher",          // Launcher (AOSP)
        "com.google.android.apps.nexuslauncher", // Pixel Launcher
        "com.android.vending",           // Play Store
        "com.google.android.gms",        // Google Play Services
        "com.google.android.gsf",        // Google Services Framework

        // Samsung sistem uygulamaları
        "com.sec.android.app.camera",    // Samsung Kamera
        "com.sec.android.gallery3d",     // Samsung Galeri
        "com.samsung.android.contacts",  // Samsung Contacts
        "com.samsung.android.dialer",    // Samsung Telefon
        "com.samsung.android.messaging", // Samsung Messages

        // Xiaomi sistem uygulamaları
        "com.miui.gallery",              // MIUI Galeri
        "com.android.incallui",          // MIUI Telefon

        // Huawei sistem uygulamaları
        "com.huawei.camera",             // Huawei Kamera
        "com.huawei.photos",             // Huawei Galeri

        // Oppo sistem uygulamaları
        "com.oppo.camera",               // Oppo Kamera
        "com.coloros.gallery3d",         // Oppo Galeri

        // Vivo sistem uygulamaları
        "com.vivo.camera"                // Vivo Kamera
    )

    /**
     * Uygulamanın silinip silinemeyeceğini kontrol eder
     *
     * @param context Android context
     * @param packageName Kontrol edilecek package name
     * @return true ise uygulama silinemez, false ise silinebilir
     */
    fun isSystemApp(context: Context, packageName: String): Boolean {
        // Core uygulamalar listesinde var mı?
        if (CORE_SYSTEM_APPS.contains(packageName)) {
            Log.d(TAG, "Package $packageName is in core system apps list")
            return true
        }

        return try {
            val packageManager = context.packageManager
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)

            // Sistem uygulaması mı? (system partition'da yüklü)
            val isSystemApp = (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

            // Güncellenen sistem uygulaması mı?
            val isUpdatedSystemApp = (applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

            // Eğer sistem uygulaması ama güncellenmişse, kullanıcı güncellemeleri kaldırabilir
            // Ama tamamen silemez. Biz her halükarda silmeyi engelleyelim.
            val cannotBeUninstalled = isSystemApp || isUpdatedSystemApp

            if (cannotBeUninstalled) {
                Log.d(TAG, "Package $packageName is a system app (system: $isSystemApp, updated: $isUpdatedSystemApp)")
            }

            cannotBeUninstalled
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Package $packageName not found", e)
            true // Güvenli taraf: bulunamazsa silinemez say
        }
    }

    /**
     * Belirtilen package name'e sahip uygulamayı kaldırmak için sistem dialog'unu açar.
     * Sistem uygulamaları için işlem yapmaz.
     *
     * @param context Android context
     * @param packageName Kaldırılacak uygulamanın package name'i
     */
    fun uninstallApp(context: Context, packageName: String) {
        // Sistem uygulaması kontrolü
        if (isSystemApp(context, packageName)) {
            Log.w(TAG, "Cannot uninstall system app: $packageName")
            return
        }

        try {
            Log.d(TAG, "Attempting to uninstall package: $packageName")
            Log.d(TAG, "Android SDK: ${Build.VERSION.SDK_INT}")

            // Android 10+ (API 29+) için farklı yaklaşım
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                uninstallAppModern(context, packageName)
            } else {
                uninstallAppLegacy(context, packageName)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error starting uninstall for package: $packageName", e)
            e.printStackTrace()
        }
    }

    /**
     * Android 10+ için modern silme yöntemi
     */
    private fun uninstallAppModern(context: Context, packageName: String) {
        try {
            val packageInstaller = context.packageManager.packageInstaller
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = "package:$packageName".toUri()
                putExtra(Intent.EXTRA_RETURN_RESULT, true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val activityContext = context.findActivity() ?: context
            activityContext.startActivity(intent)

            Log.d(TAG, "Modern uninstall intent started for: $packageName")
        } catch (e: Exception) {
            Log.e(TAG, "Modern uninstall failed, trying legacy method", e)
            uninstallAppLegacy(context, packageName)
        }
    }

    /**
     * Eski Android sürümleri için silme yöntemi
     */
    private fun uninstallAppLegacy(context: Context, packageName: String) {
        val packageUri = "package:$packageName".toUri()
        Log.d(TAG, "Package URI created: $packageUri")

        val intent = Intent(Intent.ACTION_DELETE, packageUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val activityContext = context.findActivity() ?: context
        activityContext.startActivity(intent)

        Log.d(TAG, "Legacy uninstall intent started for: $packageName")
    }

    /**
     * Context'ten Activity'yi bul
     */
    private fun Context.findActivity(): Activity? {
        var context = this
        while (context is ContextWrapper) {
            if (context is Activity) return context
            context = context.baseContext
        }
        return null
    }
}
