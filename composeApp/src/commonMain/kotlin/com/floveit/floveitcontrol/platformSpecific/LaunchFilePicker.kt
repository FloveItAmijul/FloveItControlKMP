package com.floveit.floveitcontrol.platformSpecific

import androidx.compose.runtime.Composable
import com.floveit.floveitcontrol.filetransfer.PickedFile


@Composable
expect fun LaunchPhotoPicker(onPicked: (PickedFile) -> Unit): () -> Unit
@Composable
expect fun LaunchVideoPicker(onPicked: (PickedFile) -> Unit): () -> Unit
@Composable
expect fun LaunchFilePicker(onPicked: (PickedFile) -> Unit): () -> Unit