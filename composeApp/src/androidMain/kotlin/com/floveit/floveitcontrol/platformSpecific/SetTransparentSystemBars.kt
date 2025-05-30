// androidMain/kotlin/com/floveit/floveitcontrol/platformSpecific/SystemBarUtil.kt
package com.floveit.floveitcontrol.platformSpecific

import android.app.Activity
import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

@Composable
actual fun SetTransparentSystemBars() {
    val view = LocalView.current
    val window = (view.context as? Activity)?.window

    SideEffect {
        window?.let {
            WindowCompat.setDecorFitsSystemWindows(it, false)
            it.statusBarColor = Color.TRANSPARENT
            WindowInsetsControllerCompat(it, view).isAppearanceLightStatusBars = false
        }
    }
}

// androidMain
actual fun isAndroid(): Boolean = true

