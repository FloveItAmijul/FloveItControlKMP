package com.floveit.floveitcontrol


import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket
import java.net.SocketException

class AndroidHidClient(private val context: Context) : HidClient {
    companion object {
        private const val TAG = "HidClient"
        private const val SERVICE_TYPE = "_hidserver._tcp."
        private const val SERVICE_NAME = "Amijul"
    }

    // A Job to drive the read loop
    private var readJob: Job? = null
    private val nsd = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var socket: Socket? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected

    override suspend fun discover() = withContext(Dispatchers.IO) {
        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onStartDiscoveryFailed(type: String?, code: Int)
                { Log.e(TAG, "Start fail $code") }
            override fun onStopDiscoveryFailed(type: String?, code: Int)
                { Log.e(TAG, "Stop fail $code") }
            override fun onDiscoveryStarted(type: String?)  { Log.d(TAG, "Discovery started") }
            override fun onDiscoveryStopped(type: String?)  { Log.d(TAG, "Discovery stopped") }
            override fun onServiceLost(info: NsdServiceInfo?) { _isConnected.value = false }
            override fun onServiceFound(info: NsdServiceInfo?) {
                if (info?.serviceName?.contains(SERVICE_NAME) == true) {
                    nsd.resolveService(info, object : NsdManager.ResolveListener {
                        override fun onResolveFailed(si: NsdServiceInfo?, e: Int)
                            { Log.e(TAG, "Resolve fail $e") }
                        override fun onServiceResolved(si: NsdServiceInfo?) {
                            si?.let { scope.launch { connect(it) } }
                        }
                    })
                }
            }
        }
        nsd.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener!!)
    }

    private suspend fun connect(si: NsdServiceInfo) {
        // If we were already connected, close and cancel old loop
        readJob?.cancel()
        socket?.close()

        try {
            // 1) Establish socket
            socket = Socket(si.host, si.port).apply { keepAlive = true }
            _isConnected.value = true

            // 2) Kick off the read-loop in its own Job
            val reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
            readJob = scope.launch {
                while (!socket!!.isClosed) {
                    val line = try {
                        reader.readLine()
                    } catch (se: SocketException) {
                        // expected when socket.close() is called
                        Log.d(TAG, "Read loop ended (socket closed)")
                        break
                    }
                    if (line == null) break
                    Log.d(TAG, "Received: $line")
                }
            }

        } catch (e: Exception) {
            // Only log real connection errors
            Log.e(TAG, "Error connecting to server", e)
            _isConnected.value = false
        }
    }

    override suspend fun send(data: String)  {
        withContext(Dispatchers.IO) {
            socket
                ?.takeIf { !it.isClosed }
                ?.getOutputStream()
                ?.let { out ->
                    OutputStreamWriter(out).apply {
                        write("$data\n"); flush()
                    }
                } ?: Log.e(TAG, "Not connected")
        }
    }

    override fun disconnect() {
        // 1) Cancel the read-loop so it won’t try to read on a closed socket
        readJob?.cancel()

        // 2) Close the socket
        try { socket?.close() } catch (_: Exception) {}

        // 3) Update UI state
        _isConnected.value = false
    }

}
