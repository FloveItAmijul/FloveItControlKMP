package com.floveit.floveitcontrol.database

import com.floveit.floveitcontrol.platformSpecific.provideSettings
import com.russhwolf.settings.Settings

class SettingsRepository {

    private val settings: Settings = provideSettings()

    companion object {
        private const val AUTH_KEY = "loggedIn"
    }

    fun setAuth(auth: Boolean) {
        settings.putBoolean(AUTH_KEY, auth)
    }

    fun getAuth(): Boolean {
        return settings.getBoolean(AUTH_KEY, defaultValue = false)
    }
}
