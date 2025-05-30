package com.floveit.floveitcontrol.platformSpecific

import android.content.Context
import com.russhwolf.settings.*

lateinit var appContext: Context

fun initSettings(context: Context) {
    appContext = context.applicationContext
}

actual fun provideSettings(): Settings {
    val sharedPreferences = appContext.getSharedPreferences("floveit_settings", Context.MODE_PRIVATE)
    return SharedPreferencesSettings(sharedPreferences)
}
