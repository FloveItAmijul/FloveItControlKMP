@file:OptIn(
    ExperimentalForeignApi::class,
    BetaInteropApi::class
)
package com.floveit.floveitcontrol

import kotlinx.cinterop.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import platform.Foundation.*
import platform.UIKit.UIDevice
import platform.darwin.*

actual fun provideHidClient(): HidClient = IosHidClient()

class IosHidClient : HidClient {
    companion object {

        private const val SERVICE_TYPE = "_hidserver._tcp."
    }

    //–– public state
    private val _isConnected   = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean>   = _isConnected
    private val _serverMessage = MutableStateFlow("")
    override val serverMessage: StateFlow<String>  = _serverMessage
    private val _deviceName    = MutableStateFlow("")
    override val deviceName: StateFlow<String>     = _deviceName
    private val _deviceID      = MutableStateFlow("")
    override val deviceID: StateFlow<String>       = _deviceID
    private val _findingMirror = MutableStateFlow(false)
    override val findingMirror: StateFlow<Boolean> = _findingMirror

    // ⬇️ New endpoint flows
    private val _currentHost = MutableStateFlow<String?>(null)
    override val currentHost: StateFlow<String?> = _currentHost

    private val _currentPort = MutableStateFlow(40035) // fixed, or set dynamically
    override val currentPort: StateFlow<Int> = _currentPort

    //–– retry scope
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    //–– Bonjour browser + per‐service state
    private var netBrowser: NSNetServiceBrowser? = null
    private val services      = mutableMapOf<String, NSNetService>()
    private val inputStreams  = mutableMapOf<String, NSInputStream>()
    private val outputStreams = mutableMapOf<String, NSOutputStream>()
    private val delegates     = mutableMapOf<String, NSObject>()
    private var browserDel: NSObject? = null

    init {
        getDeviceInfo()
    }

    override suspend fun discover(deviceName: String): Unit = withContext(Dispatchers.Main) {
        _findingMirror.value = true
        // 1) tear down anything we had
        services.keys.toList().forEach(::disconnectService)
        netBrowser?.run { delegate = null; stop() }

        // 2) new browser delegate (only findService, no didRemove)
        browserDel = object : NSObject(), NSNetServiceBrowserDelegateProtocol {
            override fun netServiceBrowser(
                browser: NSNetServiceBrowser,
                didFindService: NSNetService,
                moreComing: Boolean
            ) {
                val name = didFindService.name
                if (name.contains(deviceName) && !services.containsKey(name)) {
                    services[name] = didFindService

                    didFindService.delegate = object : NSObject(), NSNetServiceDelegateProtocol {
                        override fun netServiceDidResolveAddress(sender: NSNetService) {
                            sender.hostName?.let { host ->
                                _currentHost.value = host
                                _currentPort.value = sender.port.toInt()
                                scope.launch { connect(host, sender.port.toInt(), name) }
                            }
                        }
                        override fun netService(sender: NSNetService, didNotResolve: Map<Any?, *>) {
                            // on failure, retry entire discovery
                            scope.launch {
                                delay(500)
                                discover(deviceName)
                            }
                        }
                    }
                    didFindService.resolveWithTimeout(5.0)
                }
            }
            override fun netServiceBrowserWillSearch(browser: NSNetServiceBrowser) {}
            override fun netServiceBrowserDidStopSearch(browser: NSNetServiceBrowser) {
                _findingMirror.value = false
            }
            override fun netServiceBrowser(
                browser: NSNetServiceBrowser,
                didNotSearch: Map<Any?, *>
            ) {
                _findingMirror.value = false
            }
        }

        // 3) start browsing
        netBrowser = NSNetServiceBrowser().apply {
            delegate = browserDel as NSNetServiceBrowserDelegateProtocol
            scheduleInRunLoop(NSRunLoop.currentRunLoop(), NSRunLoopCommonModes)
            searchForServicesOfType(SERVICE_TYPE, inDomain = "local.")
        }
    }

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    private fun connect(host: String, port: Int, name: String) = memScoped {
        // open streams to a new host:port
        val inPtr  = alloc<ObjCObjectVar<NSInputStream?>>()
        val outPtr = alloc<ObjCObjectVar<NSOutputStream?>>()
        NSStream.getStreamsToHostWithName(host, port.toLong(), inPtr.ptr, outPtr.ptr)

        val inStream  = inPtr.value ?: return
        val outStream = outPtr.value ?: return
        val rl        = NSRunLoop.mainRunLoop

        // single Obj-C delegate for both streams
        val del = object : NSObject(), NSStreamDelegateProtocol {
            override fun stream(stream: NSStream, handleEvent: NSStreamEvent) {
                when (handleEvent) {
                    NSStreamEventHasBytesAvailable ->
                        handleIncoming(name, stream as NSInputStream)
                    NSStreamEventErrorOccurred,
                    NSStreamEventEndEncountered  ->
                        disconnectService(name)
                    else -> {}
                }
            }
        }

        // schedule & open
        inStream.delegate  = del
        inStream.scheduleInRunLoop(rl, NSRunLoopCommonModes)
        outStream.delegate = del
        outStream.scheduleInRunLoop(rl, NSRunLoopCommonModes)
        try {
            inStream.open()
            outStream.open()
        } catch (_: Throwable) {
            disconnectService(name)
            return
        }

        // stash
        inputStreams[name]  = inStream
        outputStreams[name] = outStream
        delegates[name]     = del
        _isConnected.value  = services.isNotEmpty()
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun handleIncoming(name: String, stream: NSInputStream) {
        val buf = ByteArray(1024)
        buf.usePinned { pinned ->
            val n = stream.read(pinned.addressOf(0).reinterpret(), buf.size.convert())
            if (n <= 0) {
                disconnectService(name)
                return
            }
            val txt = buf.copyOf(n.toInt()).decodeToString()
            txt.split("\n")
                .map(String::trim)
                .filter(String::isNotEmpty)
                .forEach { line ->
                    CoroutineScope(Dispatchers.Main.immediate).launch {
                        _serverMessage.value = line
                    }
                }
        }
    }

    //    override suspend fun send(data: String): Boolean = withContext(Dispatchers.Default) {
//        if (outputStreams.isEmpty()) return@withContext false
//        val payload = (data + "\n").encodeToByteArray()
//        payload.usePinned { pinned ->
//            val ptr = pinned.addressOf(0).reinterpret<UByteVar>()
//            outputStreams.forEach { (name, out) ->
//                try {
//                    out.write(ptr, payload.size.convert())
//                } catch (_: Throwable) {
//                    disconnectService(name)
//                }
//            }
//        }
//        true
//    }
    override suspend fun send(data: String): Boolean = withContext(Dispatchers.Default) {
        // If there are no open output streams, bail out immediately
        if (outputStreams.isEmpty()) return@withContext false

        val payload = (data + "\n").encodeToByteArray()
        payload.usePinned { pinned ->
            val ptr = pinned.addressOf(0).reinterpret<UByteVar>()
            // Iterate a copy of the keys so we can remove while iterating
            val iterator = outputStreams.entries.iterator()
            while (iterator.hasNext()) {
                val (name, out) = iterator.next()
                try {
                    out.write(ptr, payload.size.convert())
                } catch (_: Throwable) {
                    // Tear down this service if write fails
                    disconnectService(name)
                    iterator.remove()
                }
            }
        }

        // Return true if at least one stream remains (i.e. we “sent” to something)
        return@withContext outputStreams.isNotEmpty()
    }


    override fun disconnect() {
        // cancel retries
        scope.coroutineContext.cancelChildren()
        // tear down each server
        services.keys.toList().forEach(::disconnectService)
        // stop browsing
        netBrowser?.run { stop(); delegate = null }
        netBrowser = null
        _isConnected.value = false
        _findingMirror.value = false

    }

    override fun disconnectMirror(key: String) {
        disconnectService(key)
    }


    override fun lookupKeyForServiceName(serviceName: String): String? {
        return if (services.containsKey(serviceName)) serviceName else null
    }

    private fun disconnectService(name: String) {
        // 1) stop resolving if in-flight
        services.remove(name)?.apply {
            delegate = null
            stop()
        }
        // 2) tear down streams
        inputStreams.remove(name)?.apply {
            removeFromRunLoop(NSRunLoop.currentRunLoop(), NSRunLoopCommonModes)
            delegate = null
            close()
        }
        outputStreams.remove(name)?.apply {
            removeFromRunLoop(NSRunLoop.currentRunLoop(), NSRunLoopCommonModes)
            delegate = null
            close()
        }
        delegates.remove(name)
        // 3) update flag
        _isConnected.value = services.isNotEmpty()
    }

    private fun getDeviceInfo() {
        val defaults = NSUserDefaults.standardUserDefaults()
        val key      = "com.floveit.uniqueDeviceID"
        val existing = defaults.stringForKey(key)
        val uuid     = existing?.takeIf(String::isNotEmpty)
            ?: NSUUID().UUIDString.also {
                defaults.setObject(it, key)
                defaults.synchronize()
            }
        _deviceName.value = UIDevice.currentDevice.name
        _deviceID.value   = uuid
    }
}








