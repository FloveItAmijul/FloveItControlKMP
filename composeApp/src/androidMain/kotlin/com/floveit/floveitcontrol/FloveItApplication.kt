package com.floveit.floveitcontrol

import android.app.Application


class FloveItApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppLocator.app = this
    }
}
