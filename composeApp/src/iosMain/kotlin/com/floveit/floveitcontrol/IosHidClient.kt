package com.floveit.floveitcontrol

import kotlinx.cinterop.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.*
import platform.Foundation.*
import platform.darwin.*


actual fun provideHidClient(): HidClient = IosHidClient()
class IosHidClient : HidClient {
    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private var netBrowser: NSNetServiceBrowser? = null
    private var netService: NSNetService? = null

    private var inputStream: NSInputStream? = null
    private var outputStream: NSOutputStream? = null

    // Keep strong references to delegates
    private var browserDelegate: NSObject? = null
    private var serviceDelegate: NSObject? = null
    private var inputStreamDelegate: NSObject? = null
    private var outputStreamDelegate: NSObject? = null

    override suspend fun discover() = coroutineScope {
        // 1) Bonjour must run on the main thread
        withContext(Dispatchers.Main) {
            // Stop any existing browser
            netBrowser?.stop()

            browserDelegate = object : NSObject(), NSNetServiceBrowserDelegateProtocol {
                override fun netServiceBrowser(
                    browser: NSNetServiceBrowser,
                    didFindService: NSNetService,
                    moreComing: Boolean
                ) {
                    println("Found service: ${didFindService.name}")
                    if (didFindService.name.contains("Amijul")) {
                        // Stop browsing once we find our service
                        browser.stop()

                        netService = didFindService.apply {
                            serviceDelegate = object : NSObject(), NSNetServiceDelegateProtocol {
                                override fun netServiceDidResolveAddress(sender: NSNetService) {
                                    val hostStr = sender.hostName ?: return
                                    val port = sender.port.toInt()
                                    println("Resolved service at $hostStr:$port")

                                    // Launch connection off the main thread
                                    CoroutineScope(Dispatchers.Default).launch {
                                        connect(hostStr, port)
                                    }
                                }

                                override fun netService(sender: NSNetService, didNotResolve: Map<Any?, *>) {
                                    println("Failed to resolve: $didNotResolve")
                                }
                            }
                            delegate = serviceDelegate as NSNetServiceDelegateProtocol
                            resolveWithTimeout(5.0)
                        }
                    }
                }

                override fun netServiceBrowserDidStopSearch(browser: NSNetServiceBrowser) {
                    println("Browser stopped")
                }

                override fun netServiceBrowser(browser: NSNetServiceBrowser, didNotSearch: Map<Any?, *>) {
                    println("Browser error: $didNotSearch")
                }
            }

            netBrowser = NSNetServiceBrowser().apply {
                delegate = browserDelegate as NSNetServiceBrowserDelegateProtocol
                scheduleInRunLoop(NSRunLoop.currentRunLoop(), NSRunLoopCommonModes)
                searchForServicesOfType("_hidserver._tcp.", inDomain = "")
            }
            println("Started Bonjour discovery")
        }
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    private fun connect(host: String, port: Int) = memScoped {
        println("Connecting to $host:$port")

        val inputVar = alloc<ObjCObjectVar<NSInputStream?>>()
        val outputVar = alloc<ObjCObjectVar<NSOutputStream?>>()

        NSStream.getStreamsToHostWithName(host, port.toLong(), inputVar.ptr, outputVar.ptr)
        val runLoop = NSRunLoop.currentRunLoop()

        // Set up input stream with delegate
        inputStreamDelegate = object : NSObject(), NSStreamDelegateProtocol {
            override fun stream(stream: NSStream, handleEvent: NSStreamEvent) {
                when (handleEvent) {
                    NSStreamEventOpenCompleted -> println("Input stream opened")
                    NSStreamEventHasBytesAvailable -> handleIncomingData(stream as NSInputStream)
                    NSStreamEventErrorOccurred -> {
                        println("Input stream error: ${stream.streamError}")
                        disconnect()
                    }
                    NSStreamEventEndEncountered -> {
                        println("Input stream closed")
                        disconnect()
                    }
                    else -> {}
                }
            }
        }

        // Set up output stream with delegate
        outputStreamDelegate = object : NSObject(), NSStreamDelegateProtocol {
            override fun stream(stream: NSStream, handleEvent: NSStreamEvent) {
                when (handleEvent) {
                    NSStreamEventOpenCompleted -> println("Output stream opened")
                    NSStreamEventHasSpaceAvailable -> {} // Ready to send data
                    NSStreamEventErrorOccurred -> {
                        println("Output stream error: ${stream.streamError}")
                        disconnect()
                    }
                    NSStreamEventEndEncountered -> {
                        println("Output stream closed")
                        disconnect()
                    }
                    else -> {}
                }
            }
        }

        inputStream = inputVar.value?.apply {
            delegate = inputStreamDelegate as NSStreamDelegateProtocol
            scheduleInRunLoop(runLoop, NSRunLoopCommonModes)
            open()
        }

        outputStream = outputVar.value?.apply {
            delegate = outputStreamDelegate as NSStreamDelegateProtocol
            scheduleInRunLoop(runLoop, NSRunLoopCommonModes)
            open()
        }

        if (inputStream != null && outputStream != null) {
            _isConnected.value = true
            println("Streams connected")
        } else {
            println("Failed to create streams")
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun handleIncomingData(stream: NSInputStream) {
        // Process incoming data here if needed
        val bufferSize = 1024
        val buffer = ByteArray(bufferSize)

        buffer.usePinned { pinned ->
            val bytesRead = stream.read(pinned.addressOf(0).reinterpret(), bufferSize.convert())
            if (bytesRead > 0) {
                val data = buffer.copyOf(bytesRead.toInt()).decodeToString()
                println("Received data: $data")
                // Process received data here
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override suspend fun send(data: String) {
        withContext(Dispatchers.Default) {
            val out = outputStream ?: return@withContext
            if (!_isConnected.value) return@withContext

            val bytes = (data + "\n").encodeToByteArray()
            bytes.usePinned { pinned ->
                val ptr = pinned.addressOf(0).reinterpret<UByteVar>()
                val bytesWritten = out.write(ptr, bytes.size.convert())
                println("Sent $bytesWritten bytes")
            }
        }
    }

    override fun disconnect() {
        println("Disconnecting streams")
        inputStream?.apply {
            removeFromRunLoop(NSRunLoop.currentRunLoop(), NSRunLoopCommonModes)
            close()
        }
        outputStream?.apply {
            removeFromRunLoop(NSRunLoop.currentRunLoop(), NSRunLoopCommonModes)
            close()
        }
        netBrowser?.stop()
        netService?.stop()

        inputStream = null
        outputStream = null
        netBrowser = null
        netService = null

        _isConnected.value = false
    }
}

//class IosHidClient : HidClient {
//    private val _isConnected = MutableStateFlow(false)
//    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
//
//    private var netBrowser: NSNetServiceBrowser? = null
//    private var netService: NSNetService? = null
//
//    private var inputStream: NSInputStream? = null
//    private var outputStream: NSOutputStream? = null
//
//    override suspend fun discover() = coroutineScope {
//        // 1) Bonjour must run on the main thread
//        withContext(Dispatchers.Main) {
//            netBrowser = NSNetServiceBrowser().apply {
//                delegate = object : NSObject(), NSNetServiceBrowserDelegateProtocol {
//                    override fun netServiceBrowser(
//                        browser: NSNetServiceBrowser,
//                        didFindService: NSNetService,
//                        moreComing: Boolean
//                    ) {
//                        if (didFindService.name.contains("Amijul")) {
//                            netService = didFindService.apply {
//                                delegate = object : NSObject(), NSNetServiceDelegateProtocol {
//                                    override fun netServiceDidResolveAddress(sender: NSNetService) {
//                                        // 2) Now that we have host+port, connect off the main thread
//                                        val hostStr = sender.hostName ?: return
//                                        val port = sender.port.toInt()
//                                        // Launch a coroutine on Default dispatcher
//                                        CoroutineScope(Dispatchers.Default).launch {
//                                            connect(hostStr, port)
//                                        }
//                                    }
//                                }
//                                // kick off address resolution
//                                resolveWithTimeout(5.0)
//                            }
//                        }
//                    }
//                }
//                // schedule in the run loop
//                scheduleInRunLoop(
//                    NSRunLoop.currentRunLoop(),
//                    NSRunLoopCommonModes
//                )
//                // start searching for your TCP service
//                searchForServicesOfType("_hidserver._tcp.", inDomain = "")
//            }
//        }
//    }
//
//    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
//    private fun connect(host: String, port: Int) = memScoped {
//        // 1) Allocate Objective-C object vars
//        val inputVar  = alloc<ObjCObjectVar<NSInputStream?>>()
//        val outputVar = alloc<ObjCObjectVar<NSOutputStream?>>()
//
//        // 2) Foundation will fill those pointers
//        NSStream.getStreamsToHostWithName(host, port.toLong(), inputVar.ptr, outputVar.ptr)
//
//        // 3) Grab the shared run loop once
//        val runLoop = NSRunLoop.currentRunLoop()
//
//        // 4) Open & schedule your streams
//        inputStream = inputVar.value?.apply {
//            scheduleInRunLoop(runLoop, NSRunLoopCommonModes)
//            open()
//        }
//        outputStream = outputVar.value?.apply {
//            scheduleInRunLoop(runLoop, NSRunLoopCommonModes)
//            open()
//        }
//
//        // 5) Mark connected
//        _isConnected.value = true
//    }
//
//    @OptIn(ExperimentalForeignApi::class)
//    override suspend fun send(data: String) {
//        withContext(Dispatchers.Default) {
//            outputStream?.let { out ->
//                val bytes = (data + "\n").encodeToByteArray()
//                bytes.usePinned { pinned ->
//                    // pinned.addressOf(0) is CPointer<ByteVar>
//                    // we reinterpret it to CPointer<UByteVar> so the signature matches
//                    val ptr = pinned.addressOf(0).reinterpret<UByteVar>()
//                    out.write(ptr, bytes.size.convert())
//                }
//            }
//        }
//    }
//
//    override fun disconnect() {
//        inputStream?.close()
//        outputStream?.close()
//        _isConnected.value = false
//    }
//}
