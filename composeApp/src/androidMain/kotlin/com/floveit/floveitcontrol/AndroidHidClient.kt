package com.floveit.floveitcontrol

import android.annotation.SuppressLint
import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.util.Log
import android.provider.Settings
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.Socket
import java.net.SocketException
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AndroidHidClient(private val context: Context) : HidClient {
    companion object {
        private const val TAG = "HidClient"
        private const val SERVICE_TYPE = "_hidserver._tcp."
        private const val SERVICE_NAME = "FloveIt"
    }

    // hold all available server
    private val _availableServers = MutableStateFlow<List<String>>(emptyList())
    val availableServers: StateFlow<List<String>> get() = _availableServers.asStateFlow()
    // 2) The one the user has picked
    private val _selectedServer = MutableStateFlow<String?>(null)
    val selectedServer: StateFlow<String?> get() = _selectedServer

   // private var readJob: Job? = null
    private val readJobs = ConcurrentHashMap<String , Job>()
    private val connections = ConcurrentHashMap<String , Socket>()
    private val nsd = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private var discoveryListener: NsdManager.DiscoveryListener? = null

    //private var socket: Socket? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _deviceName = MutableStateFlow("")
    override val deviceName: StateFlow<String> get() = _deviceName.asStateFlow()
    private val _deviceID = MutableStateFlow("")
    override val deviceID: StateFlow<String> get() = _deviceID.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> get() = _isConnected.asStateFlow()
    private val _serverMessage = MutableStateFlow("")
    override val serverMessage: StateFlow<String> get() = _serverMessage.asStateFlow()

    init { getDeviceInfo() }


    fun selectServer(key: String){
        if(connections.containsKey(key)){
            _selectedServer.value = key
        }
    }
    override suspend fun discover(): Unit = withContext(Dispatchers.IO) {
        // ─── 1) Stop any existing discovery ─────────────────────────────────
        discoveryListener?.let { old ->
            Log.d(TAG, "Stopping previous discovery (if any)")
            try {
                nsd.stopServiceDiscovery(old)
            } catch (iae: IllegalArgumentException) {
                Log.w(TAG, "stopServiceDiscovery: listener not registered, ignoring", iae)
            }
            discoveryListener = null
        }

        // ─── 2) Build a fresh discovery listener ─────────────────────────────
        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onStartDiscoveryFailed(type: String?, code: Int) {
                Log.e(TAG, "StartDiscoveryFailed: $code")
            }
            override fun onStopDiscoveryFailed(type: String?, code: Int) {
                Log.e(TAG, "StopDiscoveryFailed: $code")
            }
            override fun onDiscoveryStarted(type: String?) {
                Log.d(TAG, "Discovery started")
            }
            override fun onDiscoveryStopped(type: String?) {
                Log.d(TAG, "Discovery stopped")
            }

            override fun onServiceLost(info: NsdServiceInfo?) {
                info ?: return
                val host = info.host ?: run {
                    Log.w(TAG, "Service lost before resolution: ${info.serviceName}")
                    return
                }
                val key = "${host.hostAddress}:${info.port}"
                connections[key]?.close()
                readJobs[key]?.cancel()
                connections.remove(key)
                readJobs.remove(key)
                if (connections.isEmpty()) _isConnected.value = false
                Log.w(TAG, "Service $key lost – cleaned up")
            }

            override fun onServiceFound(info: NsdServiceInfo?) {
                val svc = info ?: return
                val name = svc.serviceName
                Log.d(TAG, "Service found: $name")
                if (!name.contains(SERVICE_NAME)) return

                Log.d(TAG, "Resolving $name…")
                nsd.resolveService(svc, object : NsdManager.ResolveListener {
                    override fun onResolveFailed(si: NsdServiceInfo?, errorCode: Int) {
                        Log.e(TAG, "Resolve failed on ${si?.serviceName}: $errorCode — retrying this service")
                        // retry only this service after a small delay
                        Handler(Looper.getMainLooper()).postDelayed({
                            si?.let { nsd.resolveService(it, this) }
                        }, 500)
                    }
                    override fun onServiceResolved(si: NsdServiceInfo) {
                        val host = si.host?.hostAddress ?: return
                        val key = "$host:${si.port}"
                        if (connections.containsKey(key)) {
                            Log.d(TAG, "$key already connected, skipping")
                            return
                        }
                        Log.d(TAG, "Resolved $key → connecting")
                        scope.launch { connect(si, key) }
                    }
                })
            }
        }

        // ─── 3) Kick off discovery, with one retry if NSD complains ──────────
        try {
            nsd.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener!!)
        } catch (iae: IllegalArgumentException) {
            Log.w(TAG, "discoverServices threw: $iae — retrying once")
            // make sure we’ve cleaned up the bad listener
            try { nsd.stopServiceDiscovery(discoveryListener!!) } catch (_: Exception) {}
            nsd.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener!!)
        }
    }



    private fun connect(si: NsdServiceInfo , key: String) {
        // tear down previous socket/read‐job
       // readJob?.cancel()
       // socket?.close()

        try {
          //  socket = Socket(si.host, si.port).apply { keepAlive = true }
         //  _isConnected.value = true


            val sock = Socket(si.host , si.port).apply { keepAlive = true }
            connections[key] = sock
            _isConnected.value = connections.isNotEmpty()
            _availableServers.value = connections.keys.toList()

            // launch dedicated read-loop
            val job = scope.launch {
                val reader = BufferedReader(InputStreamReader(sock.getInputStream()))
                while(!sock.isClosed){
                    val line = try {
                        reader.readLine()
                    } catch (se: SocketException){
                        Log.d(TAG, "[$key] read loop ended ${se.message}")
                        break
                    }

                    if(line == null) break
                    Log.d(TAG, "[$key] Received: $line")
                    withContext(Dispatchers.Main) {
                        _serverMessage.value = line
                    }
                }
            }
            readJobs[key] = job

        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to server", e)
            _isConnected.value = false
        }
    }

    override suspend fun send(data: String): Boolean = withContext(Dispatchers.IO) {
        if(connections.isEmpty()) return@withContext false
        connections.values.forEach { sock ->
            try {
                sock.getOutputStream().writer().apply {
                    write("$data\n"); flush()
                }
                Log.d(TAG, "Sent to ${sock.inetAddress.hostAddress}: $data")
            } catch (e: IOException){
                Log.e(TAG, "Send failed on ${sock.inetAddress.hostAddress}", e)
            }
        }
        true
    }

    override fun disconnect() {
        readJobs.values.forEach { it.cancel() }
        connections.values.forEach {
            try {
                it.close()
            } catch (e: Exception){
                Log.e(TAG, "Disconnect failed", e)
            }
        }

        readJobs.clear()
        connections.clear()
        _isConnected.value = connections.isNotEmpty()
        discoveryListener?.let { old ->
            Log.d(TAG, "Stopping discovery (disconnect)")
            try {
                               nsd.stopServiceDiscovery(old)
            } catch (iae: IllegalArgumentException) {
                Log.w(TAG, "stopServiceDiscovery: listener not registered, ignoring")
            }
            discoveryListener = null
        }

    }

    @SuppressLint("HardwareIds")
    private fun getDeviceInfo() {
        try {
            val androidID = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            val man = Build.MANUFACTURER.replaceFirstChar { it.titlecase(Locale.ROOT) }
            val model = Build.MODEL
            _deviceName.value = if (model.startsWith(man, true)) model else "$man $model"
            _deviceID.value = androidID
            Log.d(TAG, "DeviceName=${_deviceName.value}, ID=${_deviceID.value}")
        } catch (e: IOException) {
            Log.e(TAG, "getDeviceInfo failed", e)
        }
    }
}
