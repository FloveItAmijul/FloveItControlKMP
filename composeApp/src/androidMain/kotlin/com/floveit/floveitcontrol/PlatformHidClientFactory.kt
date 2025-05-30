package com.floveit.floveitcontrol



import android.app.Application
import com.floveit.floveitcontrol.HidClient
import com.floveit.floveitcontrol.provideHidClient

object AppLocator {
    lateinit var app: Application
}

actual fun provideHidClient(): HidClient =
    AndroidHidClient(AppLocator.app)
