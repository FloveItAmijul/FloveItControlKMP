package com.floveit.floveitcontrol.platformSpecific


import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.floveit.floveitcontrol.filetransfer.PickedFile

@Composable
actual fun launchFilePicker(onPicked: (PickedFile) -> Unit): () -> Unit {

    val context = LocalContext.current
    val cr = context.contentResolver
    val mimeTypes = arrayOf("image/*", "application/pdf", "text/plain", "*/*")

    var afterPick: ((Uri) -> Unit)? = null

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        afterPick?.invoke(uri)
    }

    return remember {
        {
            // Use an anonymous function so 'return' returns from THIS function safely
            afterPick = fun(uri: Uri) {
                val name = resolveDisplayName(cr, uri) ?: "file.bin"
                val mime = cr.getType(uri) ?: guessMimeFromName(name)
                val input = cr.openInputStream(uri) ?: return        // <- clean early-exit
                val bytes = input.use { it.readBytes() }
                onPicked(PickedFile(displayName = name, mime = mime, bytes = bytes))
            }
            launcher.launch(mimeTypes)
        }
    }
}

private fun resolveDisplayName(cr: ContentResolver, uri: Uri): String? {
    var cursor: Cursor? = null
    return try {
        cursor = cr.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        if (cursor?.moveToFirst() == true) cursor.getString(0) else null
    } finally { cursor?.close() }
}

private fun guessMimeFromName(name: String): String =
    when (name.substringAfterLast('.', "").lowercase()) {
        "png" -> "image/png"
        "jpg", "jpeg" -> "image/jpeg"
        "gif" -> "image/gif"
        "pdf" -> "application/pdf"
        "txt" -> "text/plain"
        else -> "application/octet-stream"
    }

