


// File: shared/src/iosMain/kotlin/com/floveit/floveitcontrol/platformSpecific/Settings.kt
package com.floveit.floveitcontrol.platformSpecific

import com.russhwolf.settings.*
import platform.Foundation.NSUserDefaults


actual fun provideSettings(): Settings {
    return NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
}
