// commonMain
package com.floveit.floveitcontrol.platformSpecific

import androidx.compose.runtime.Composable

@Composable
expect fun CameraView(onQRCodeScanned: (String) -> Unit = {})
