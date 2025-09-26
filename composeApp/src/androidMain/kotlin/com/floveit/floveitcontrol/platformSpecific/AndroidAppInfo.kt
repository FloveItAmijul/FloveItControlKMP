package com.floveit.floveitcontrol.platformSpecific

import android.content.Context
import android.content.pm.PackageManager

class AndroidAppInfo(private val context: Context): AppInfo {
    override fun getAppInfo(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "Unknown"
        } catch (e: PackageManager.NameNotFoundException) {
            "Unknown"
        }
    }
}

actual fun getAppInfo(): AppInfo = AndroidAppInfo(appContext)