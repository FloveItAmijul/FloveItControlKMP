package com.floveit.floveitcontrol.platformSpecific

interface AppInfo {
    fun getAppInfo() : String
}

expect fun getAppInfo() : AppInfo