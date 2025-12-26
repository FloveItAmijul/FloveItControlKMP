package com.floveit.floveitcontrol.platformSpecific

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.floveit.floveitcontrol.filetransfer.PickedFile

@Composable
actual fun LaunchVideoPicker(onPicked: (PickedFile) -> Unit): () -> Unit {

    val context = LocalContext.current
    val cr = context.contentResolver

    var afterPick: ((Uri) -> Unit)? = null

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        afterPick?.invoke(uri)
    }

    return remember { {
        afterPick = fun(uri: Uri){
            val name = resolveDisplayName(cr, uri) ?: "video.mp4"
            val mime = cr.getType(uri) ?: "video/*"
            val input = cr.openInputStream(uri) ?: return
            val bytes = input.use { it.readBytes() }
            onPicked(PickedFile(displayName = name, mime = mime, bytes = bytes))
        }
        launcher.launch("video/*")
    } }
}