package com.floveit.floveitcontrol.platformSpecific

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.floveit.floveitcontrol.filetransfer.PickedFile
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
actual fun LaunchFilePicker(onPicked: (PickedFile) -> Unit): () -> Unit {
    // always call the latest lambda even after recomposition
    val latestOnPicked by rememberUpdatedState(onPicked)

    // IMPORTANT: keep delegate alive
    val delegate = remember {
        object : NSObject(), UIDocumentPickerDelegateProtocol {

            @OptIn(ExperimentalForeignApi::class)
            override fun documentPicker(
                controller: UIDocumentPickerViewController,
                didPickDocumentsAtURLs: List<*>
            ) {
                val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL ?: return

                val allowed = url.startAccessingSecurityScopedResource()
                try {
                    val data: NSData = NSData.dataWithContentsOfURL(url) ?: return
                    val len = data.length.toInt()
                    val bytes = ByteArray(len)
                    bytes.usePinned { pinned ->
                        data.getBytes(pinned.addressOf(0), data.length)
                    }

                    val name = url.lastPathComponent ?: "file.bin"
                    val mime = guessMimeFromName(name)

                    latestOnPicked(PickedFile(displayName = name, mime = mime, bytes = bytes))
                } finally {
                    if (allowed) url.stopAccessingSecurityScopedResource()
                }
            }

            override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
                // optional: you can notify UI if you want
            }
        }
    }

    return remember {
        {
            dispatch_async(dispatch_get_main_queue()) {
                val picker = UIDocumentPickerViewController(
                    forOpeningContentTypes = listOf(UTTypeItem)
                )
                picker.allowsMultipleSelection = false
                picker.delegate = delegate
                picker.modalPresentationStyle = UIModalPresentationFormSheet

                topViewController()?.presentViewController(
                    picker,
                    animated = true,
                    completion = null
                )
            }
        }
    }
}

fun topViewController(): UIViewController? {
    val scenes = UIApplication.sharedApplication.connectedScenes
        .filterIsInstance<UIWindowScene>()

    // pick a foreground scene first, else any scene
    val scene = scenes.firstOrNull {
        it.activationState == UISceneActivationStateForegroundActive
    } ?: scenes.firstOrNull()

    // ensure this is UIWindow? (not Any?)
    val window: UIWindow? = scene?.windows?.firstOrNull() as? UIWindow
        ?: UIApplication.sharedApplication.keyWindow

    var top: UIViewController? = window?.rootViewController
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
