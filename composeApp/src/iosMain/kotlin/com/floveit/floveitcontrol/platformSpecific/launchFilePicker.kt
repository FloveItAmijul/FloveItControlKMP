package com.floveit.floveitcontrol.platformSpecific

import com.floveit.floveitcontrol.filetransfer.PickedFile
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.getBytes
import platform.UIKit.*
import platform.UniformTypeIdentifiers.UTTypeItem
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue



@Composable
actual fun launchFilePicker(onPicked: (PickedFile) -> Unit): () -> Unit {
    // No Compose-managed resources needed; just return a lambda that presents a UIDocumentPicker
    return remember {
        {
            presentPicker(onPicked)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun presentPicker(onPicked: (PickedFile) -> Unit) {
    val picker = UIDocumentPickerViewController(forOpeningContentTypes = listOf(UTTypeItem))
    picker.allowsMultipleSelection = false
    picker.delegate = object : NSObject(), UIDocumentPickerDelegateProtocol {
        override fun documentPicker(controller: UIDocumentPickerViewController, didPickDocumentsAtURLs: List<*>) {
            val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL ?: return
            val allowed = url.startAccessingSecurityScopedResource()
            try {
                val data = NSData.dataWithContentsOfURL(url) ?: return
                val len = data.length.toInt()
                val bytes = ByteArray(len)
                bytes.usePinned { pinned -> data.getBytes(pinned.addressOf(0), data.length) }
                val name = (url.lastPathComponent ?: "file.bin").toString()
                val mime = guessMimeFromName(name)
                onPicked(PickedFile(displayName = name, mime = mime, bytes = bytes))
            } finally {
                if (allowed) url.stopAccessingSecurityScopedResource()
            }
        }
        override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {}
    }

    dispatch_async(dispatch_get_main_queue()) {
        topViewController()?.presentViewController(picker, animated = true, completion = null)
    }
}

private fun topViewController(): UIViewController? {
    val scenes = UIApplication.sharedApplication.connectedScenes
        .filterIsInstance<UIWindowScene>()

    val anyWindow: Any? =
        scenes.firstOrNull { it.activationState == UISceneActivationStateForegroundActive }
            ?.windows?.firstOrNull()
            ?: scenes.firstOrNull()?.windows?.firstOrNull()

    val window = anyWindow as? UIWindow ?: return null
    var top: UIViewController? = window.rootViewController

    while (true) {
        val presented = top?.presentedViewController ?: break
        top = presented
    }
    return top
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
