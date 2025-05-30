package com.floveit.floveitcontrol.platformSpecific

import androidx.compose.runtime.*

@Composable
actual fun SetTransparentSystemBars() {
    // No-op for iOS (not yet supported)
}

actual fun isAndroid(): Boolean = false
