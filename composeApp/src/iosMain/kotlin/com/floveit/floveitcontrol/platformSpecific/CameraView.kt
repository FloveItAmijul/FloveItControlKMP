@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.floveit.floveitcontrol.platformSpecific

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import platform.AVFoundation.*
import platform.Foundation.NSLog
import platform.UIKit.UIView
import platform.darwin.*
import platform.darwin.NSObject
@Composable
actual fun CameraView(onQRCodeScanned: (String) -> Unit) {
    var useFrontCamera by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        NSLog("🔴 CameraView: Composable entered")
        onDispose {
            NSLog("🔴 CameraView: Disposing, stopping session")
            cameraSession?.stopRunning()
            cameraSession = null
        }
    }

    Box(Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        UIKitView(
            factory = {
                NSLog("✅ UIKitView factory started")

                val session = AVCaptureSession()
                session.sessionPreset = AVCaptureSessionPresetPhoto

                val device = AVCaptureDevice.devicesWithMediaType(AVMediaTypeVideo)
                    ?.filterIsInstance<AVCaptureDevice>()
                    ?.firstOrNull { device ->
                        if (useFrontCamera) {
                            device.position == AVCaptureDevicePositionFront
                        } else {
                            device.position == AVCaptureDevicePositionBack
                        }
                    } ?: run {
                    NSLog("❌ No camera device found")
                    return@UIKitView UIView()
                }

                val input = requireNotNull(
                    AVCaptureDeviceInput.deviceInputWithDevice(device, null)
                ) { "❌ Failed to create AVCaptureDeviceInput" }

                session.addInput(input as AVCaptureInput)

                // ✅ ADD METADATA OUTPUT FOR QR CODES
                val metadataOutput = AVCaptureMetadataOutput()
                if (session.canAddOutput(metadataOutput)) {
                    session.addOutput(metadataOutput)
                    metadataOutput.setMetadataObjectsDelegate(
                        QRMetadataDelegate(onQRCodeScanned),
                        dispatch_get_main_queue()
                    )
                    metadataOutput.metadataObjectTypes = listOf(AVMetadataObjectTypeQRCode)
                    NSLog("✅ QR Metadata Output added")
                }

                val previewLayer = AVCaptureVideoPreviewLayer(session = session).apply {
                    videoGravity = AVLayerVideoGravityResizeAspectFill
                }

                val view = UIView()
                view.layer.addSublayer(previewLayer)

                dispatch_async(dispatch_get_main_queue()) {
                    previewLayer.frame = view.bounds
                }

                dispatch_async(
                    dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0UL)
                ) {
                    session.startRunning()
                }

                cameraSession = session
                view
            },
            modifier = Modifier.size(width = 300.dp, height = 400.dp)
                // or use .aspectRatio(3f/4f) instead of .size(...)
                .clip(RoundedCornerShape(8.dp))    // optional rounding
                .border(1.dp, Color.White, RoundedCornerShape(8.dp))
        )
    }

}

// ✅ GLOBAL HOLDER
private var cameraSession: AVCaptureSession? = null

class QRMetadataDelegate(
    private val onQRCodeScanned: (String) -> Unit
) : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {

    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputMetadataObjects: List<*>,
        fromConnection: AVCaptureConnection
    ) {
        val first = didOutputMetadataObjects.firstOrNull() as? AVMetadataMachineReadableCodeObject
        val value = first?.stringValue
        if (value != null) {
            NSLog("✅ QR Code Detected: $value")
            onQRCodeScanned(value)
        }
    }
}
