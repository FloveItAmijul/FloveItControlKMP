package com.floveit.floveitcontrol.platformSpecific

import androidx.compose.runtime.Composable
import com.floveit.floveitcontrol.filetransfer.PickedFile


@Composable
expect fun launchFilePicker(onPicked: (PickedFile) -> Unit): () -> Unit