package com.floveit.floveitcontrol.platformSpecific

import androidx.compose.runtime.Composable

@Composable
expect fun SetTransparentSystemBars()
expect fun isAndroid(): Boolean
