package com.floveit.floveitcontrol.platformSpecific

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.floveit.floveitcontrol.filetransfer.PickedFile
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.getBytes
import platform.Photos.PHPhotoLibrary
import platform.PhotosUI.*
import platform.UniformTypeIdentifiers.*
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue


@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun LaunchPhotoPicker(onPicked: (PickedFile) -> Unit): () -> Unit {
    val latestOnPicked by rememberUpdatedState(onPicked)

    val delegate = remember {
        object : NSObject(), PHPickerViewControllerDelegateProtocol {
            override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
                picker.dismissViewControllerAnimated(true, completion = null)

                val result = didFinishPicking.firstOrNull() as? PHPickerResult ?: return
                val provider = result.itemProvider

                // Prefer data representation
                val utType = UTTypeImage.identifier
                if (!provider.hasItemConformingToTypeIdentifier(utType)) return

                provider.loadDataRepresentationForTypeIdentifier(utType) { data, error ->
                    if (data == null || error != null) return@loadDataRepresentationForTypeIdentifier

                    val name = "photo.jpg" // iOS doesn't always give original name here
                    val bytes = ByteArray(data.length.toInt())
                    bytes.usePinned { pinned ->
                        data.getBytes(pinned.addressOf(0), data.length)
                    }
                    latestOnPicked(PickedFile(displayName = name, mime =  "image/jpeg", bytes = bytes))
                }
            }
        }
    }

    return remember {
        {
            dispatch_async(dispatch_get_main_queue()) {
                val config = PHPickerConfiguration(photoLibrary = PHPhotoLibrary.sharedPhotoLibrary()).apply {
                    selectionLimit = 1
                    filter = PHPickerFilter.imagesFilter()
                }

                val picker = PHPickerViewController(configuration = config)
                picker.delegate = delegate
                topViewController()?.presentViewController(picker, animated = true, completion = null)
            }
        }
    }
}
