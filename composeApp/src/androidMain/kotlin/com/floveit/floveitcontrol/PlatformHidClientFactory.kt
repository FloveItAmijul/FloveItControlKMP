package com.floveit.floveitcontrol



import android.app.Application
import com.floveit.floveitcontrol.HidClient
import com.floveit.floveitcontrol.provideHidClient

/**
 * We’ll stash the Application in a global so that `provideHidClient()` can pull it out.
 * (Or use a proper DI container – this is the simplest hack.)
 */
object AppLocator {
    lateinit var app: Application
}

actual fun provideHidClient(): HidClient =
    AndroidHidClient(AppLocator.app)
