package com.floveit.floveitcontrol.platformSpecific

import platform.Foundation.NSBundle

class IOSAppInfo: AppInfo {
    override fun getAppInfo(): String {
        return NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String ?: "Unknown"
    }
}

actual fun getAppInfo(): AppInfo = IOSAppInfo()