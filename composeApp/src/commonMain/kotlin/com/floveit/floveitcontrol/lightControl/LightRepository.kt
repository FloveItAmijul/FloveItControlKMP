package com.floveit.floveitcontrol.lightControl

import com.floveit.floveitcontrol.HidClient
import com.floveit.floveitcontrol.settings.mirrors.MirrorDevice
import com.floveit.floveitcontrol.platformSpecific.provideSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json

class LightRepository(
    private val hidClient: HidClient,
    private val settings: Settings = provideSettings()
) {
    companion object {
        private const val AUTH = "isLoggedIn"
        private const val LAST_MIRROR = "lastMirror"
        private const val CONNECTED_MIRRORS = "connectedMirrors"
        const val SERVICE_NAME = "FLoveIt"
    }

    private val json = Json { encodeDefaults = true }
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _connectedMirrors = MutableStateFlow<List<MirrorDevice>>(emptyList())
    val connectedMirrors: StateFlow<List<MirrorDevice>> get() = _connectedMirrors.asStateFlow()

    // I want to store the last connected server also in the data store like login
    private val _lastConnectedMirror = MutableStateFlow<MirrorDevice?>(null)
    val lastConnectedMirror: StateFlow<MirrorDevice?> get() = _lastConnectedMirror.asStateFlow()

    val findingMirror: StateFlow<Boolean> = hidClient.findingMirror
    val isConnected: StateFlow<Boolean> = hidClient.isConnected
    val serverMessage: StateFlow<String> = hidClient.serverMessage
    val deviceName: StateFlow<String> = hidClient.deviceName
    val deviceID: StateFlow<String> = hidClient.deviceID

    // Login Info
    private val _login = MutableStateFlow(settings.getBoolean(AUTH, false))
    val login: StateFlow<Boolean> = _login.asStateFlow()

    private val _isLoginSuccess = MutableStateFlow(false)
    val isLoginSuccess: StateFlow<Boolean> get() = _isLoginSuccess.asStateFlow()

    private val _isLogin = MutableStateFlow(false)
    val isLogin: StateFlow<Boolean> get() = _isLogin.asStateFlow()


    // Led State
    private val _ledState = MutableStateFlow(false)
    val ledState: StateFlow<Boolean> = _ledState.asStateFlow()

    // Led Brightness
    private val _ledBrightness = MutableStateFlow(0f)
    val ledBrightness: StateFlow<Float> = _ledBrightness.asStateFlow()

    // Led Color Temperature
    private val _ledColorTemp = MutableStateFlow(0f)
    val ledColorTemp: StateFlow<Float> = _ledColorTemp.asStateFlow()

    // led Mode
    private val _boostMode = MutableStateFlow(false)
    val boostMode: StateFlow<Boolean> = _boostMode.asStateFlow()
    private val _makeupMode = MutableStateFlow(false)
    val makeupMode: StateFlow<Boolean> = _makeupMode.asStateFlow()
    private val _nightMode = MutableStateFlow(false)
    val nightMode: StateFlow<Boolean> = _nightMode.asStateFlow()
    private val _favouriteMode = MutableStateFlow(false)
    val favouriteMode: StateFlow<Boolean> = _favouriteMode.asStateFlow()


    init {
        // …then load once everything (including `json`) is initialized
        _connectedMirrors.value = loadMirrorDevice()

        _lastConnectedMirror.value = loadLastMirror()
    }


    /** Reads the last mirror from Settings, or null if none was saved. */
    private fun loadLastMirror(): MirrorDevice? {
        val raw = settings.getString(LAST_MIRROR, "")
        return try {
            if (raw.isNotBlank()) json.decodeFromString<MirrorDevice>(raw) else null
        } catch (e: Exception) {
            println(e.message)
            null
        }
    }


    suspend fun startDiscoveryMirror(serverName: String , serverID: String) {
        withContext(Dispatchers.IO) {
            hidClient.discover("$serverName-$serverID")
            println("Start discovery")
        }
    }

    suspend fun disconnectMirror(){
        withContext(Dispatchers.IO) {
            hidClient.disconnect()
            println("Disconnect mirror")
        }
    }

    suspend fun disconnectMirror(mirror: MirrorDevice) {
        withContext(Dispatchers.IO) {
            // Reconstruct exactly the NsdService name you used when discovering:
            val serviceName = "${SERVICE_NAME}-${mirror.id}"
            // Ask the client if it knows a “key” for that name:
            hidClient.lookupKeyForServiceName(serviceName)
                ?.let { key -> hidClient.disconnectMirror(key) }
        }
    }

    suspend fun addMirrorDevice(mirrorDevice: MirrorDevice) = withContext(Dispatchers.IO) {
        val updated = (_connectedMirrors.value + mirrorDevice)
            .distinctBy { it.id }    // avoid dupes
        _connectedMirrors.value = updated
        saveMirrorList(updated)
    }

    suspend fun removeMirrorDevice(mirror: MirrorDevice) {
        withContext(Dispatchers.IO) {
            // 1) If this mirror is currently connected, disconnect it:
            disconnectMirror(mirror)

            // 2) Remove from our in-memory + on-disk list:
            val updated = _connectedMirrors.value.filterNot { it.id == mirror.id }
            _connectedMirrors.value = updated
            saveMirrorList(updated)

            // 3) If this was the “lastConnected,” clear that:
            _lastConnectedMirror.value?.let { last ->
                if (last.id == mirror.id) {
                    _lastConnectedMirror.value = null
                    settings.putString(LAST_MIRROR, "")
                }
            }
        }
    }


    /** Kick off discovery for the last mirror, if any. */
    suspend fun startLastMirrorDiscovery() = withContext(Dispatchers.IO) {
        _lastConnectedMirror.value?.let { startDiscoveryMirror(SERVICE_NAME, it.id) }
        println("Start last mirror discovery")
    }


    fun updateLastMirror(device: MirrorDevice) {
        _lastConnectedMirror.value = device
        settings.putString(LAST_MIRROR, json.encodeToString(device))
        println("Update last mirror $device")
    }

    private fun loadMirrorDevice(): List<MirrorDevice> =
        settings.getString(CONNECTED_MIRRORS, "[]")
            .let { json.decodeFromString<List<MirrorDevice>>(it) }


    private fun saveMirrorList(list: List<MirrorDevice>) {
        settings.putString(CONNECTED_MIRRORS,
            json.encodeToString<List<MirrorDevice>>(list)
        )
    }





    // Handle Scan Data
    suspend fun handleScanData(data: String) {
        withContext(Dispatchers.IO) {
            println("🔍 Scanned data: $data")
            if(data.startsWith("FLoveIt")){
                val getData = data.removePrefix("FLoveIt")
                val id = getData.substringAfter("id:").substringBefore(",")
                val name = getData.substringAfter("name:")
                val newMirrorDevice = MirrorDevice(id = id, name = name)
                addMirrorDevice(newMirrorDevice)
                updateLastMirror(newMirrorDevice)
                println("Device name: $name")

                while (!isConnected.value){
                    _isLogin.value = true
                    startDiscoveryMirror(serverName = SERVICE_NAME , serverID = id)
                    delay(5000L)
                    println("Searching...")
                }

                // If connected
                _isLogin.value = false
                if(sendAuthenticate("authenticated")){
                    println("Successfully login")

                }

            }
        }
    }

    // observe Server Message
    fun observeServerMessages(): Flow<String> =
        hidClient.serverMessage
            .onEach { message ->
                // exactly your old side-effects:
                println("New server message = $message")
                if (message.startsWith("Server")) {
                    handleInitialData(message)
                } else {
                    handleServerAuthData(message)
                }
            }
    // handle initial data
    private fun handleInitialData(message : String) {
        val parsedData = try {
            message.split("&").associate {
                val (key, value) = it.split("=")
                key to value
            }
        } catch (e: Exception) {
            println("Error parsing message: ${e.message}")
            return // Exit if parsing fails
        }

        // Update client-side state
        parsedData["ServerBrightness"]?.toFloatOrNull()?.let { _ledBrightness.value = it }
        parsedData["WarmCool"]?.toFloatOrNull()?.let { _ledColorTemp.value = it }
        //parsedData["LoggedIn"]?.split(",")?.let { _user.value = it }
        parsedData["LedState"]?.toBoolean()?.let { _ledState.value = it }

//        // Check if the Android ID is in the user list
//        val isAuthenticated = _user.value.any { user ->
//            println("All User: $user")
//            if (user == _deviceName.value) {
//                updateAuth(true)
//                println("user: $user && ID: ${_deviceName.value}")
//                true // Stop iteration once a match is found
//            } else {
//                false
//            }
//        }
//
//        // Handle database update based on authentication status
//        if (!isAuthenticated) {
//            try {
//                updateAuth(false)
//            } catch (e: IOException) {
//                Log.e("Database", "IOException: ${e.message}")
//            }
//        }

    }
    // handle server message
    private fun handleServerAuthData(message : String) {
        when (message) {
            "unauthenticated${deviceName.value}-${deviceID.value}" -> {
                _login.value = false
                updateAuthStatus(false)
                settings.putBoolean(key = AUTH , value = false)
                println("❌ Login failed")
            }
            "authenticated${deviceName.value}-${deviceID.value}" -> {
                _login.value = true
                updateAuthStatus(true)
                settings.putBoolean(key = AUTH , value = true)
                println("✅ Login successful")
            }
        }
    }

    fun updateAuthStatus(auth: Boolean) {
        _isLoginSuccess.value = auth
    }
    // send data
    suspend fun sendData(data: String): Boolean {
        val sendData = "${deviceName.value}-${deviceID.value},$data"
        return withContext(Dispatchers.IO) {
            hidClient.send(sendData)   // ✅ return the Boolean result
        }
    }
    // send Authentication
    suspend fun sendAuthenticate(data: String) : Boolean{
        val sendData = "$data${deviceName.value}-${deviceID.value}"
        println(sendData)
       return withContext(Dispatchers.IO) {
            hidClient.send(sendData)
        }
    }
    // update ledState and store in database
    suspend fun updateLedState(ledState: Boolean) {
        withContext(Dispatchers.IO) {
            val sendLedState = sendData(if (ledState) "ON" else "OFF")
            if (sendLedState) {
                _ledState.value = ledState
                println("✅ LED state updated: $ledState")
            }else {
                println("❌ Failed to update LED state: $ledState")
            }

        }
    }
    // update ledBrightness and store in database
    suspend fun updateLedBrightness(ledBrightness: Float) {
        withContext(Dispatchers.IO) {
            val sendLedBrightness = sendData("Brightness$ledBrightness")
            if (sendLedBrightness) {
                _ledBrightness.value = ledBrightness
                _ledState.value = true
                println("✅ LED brightness updated: $ledBrightness")
            } else {
                println("❌ Failed to update LED brightness: $ledBrightness")
            }
        }
    }
// update ledColorTemp and store in database
    suspend fun updateLedColorTemp(ledColorTemp: Float) {
        withContext(Dispatchers.IO) {
            val sendLedColorTemp = sendData("WarmCool$ledColorTemp")
            if (sendLedColorTemp) {
                _ledColorTemp.value = ledColorTemp
                _ledState.value = true
                println("✅ LED color temperature updated: $ledColorTemp")
            } else {
                println("❌ Failed to update LED color temperature: $ledColorTemp")
            }
        }
    }
    // update Mode and store in database
    suspend fun toggleBoostMode() {
       withContext(Dispatchers.IO) {
            val command = if (_boostMode.value) "BoostOFF" else "BoostON"
            if (sendData(command)) {
                _boostMode.value = !_boostMode.value
                if(_boostMode.value){
                    _ledState.value = true
                    _makeupMode.value = false
                    _nightMode.value = false
                    _favouriteMode.value = false
                }
            }
        }
    }
    suspend fun toggleMakeupMode(){
        withContext(Dispatchers.IO) {
            val command = if (_makeupMode.value) "MakeupOFF" else "MakeupON"
            if (sendData(command)) {
                _makeupMode.value = !_makeupMode.value
                if(_makeupMode.value){
                    _ledState.value = true
                    _boostMode.value = false
                    _nightMode.value = false
                    _favouriteMode.value = false
                }
            }
        }
    }
    suspend fun toggleNightMode(){
        withContext(Dispatchers.IO) {
            val command = if (_nightMode.value) "NightOFF" else "NightON"
            if (sendData(command)) {
                _nightMode.value = !_nightMode.value
                if(_nightMode.value){
                    _ledState.value = true
                    _boostMode.value = false
                    _makeupMode.value = false
                    _favouriteMode.value = false
                }
            }
        }
    }
    suspend fun toggleFavouriteMode(){
        withContext(Dispatchers.IO) {
            val command = if (_favouriteMode.value) "FavouriteOFF" else "FavouriteON"
            if (sendData(command)) {
                _favouriteMode.value = !_favouriteMode.value
                if(_favouriteMode.value){
                    _ledState.value = true
                    _boostMode.value = false
                    _makeupMode.value = false
                    _nightMode.value = false
                }
            }
        }
    }
    // Logout from device control
    suspend fun logout(){
        withContext(Dispatchers.IO) {
//            if(sendAuthenticate("unauthenticated")){
//                _login.value = false
//                settings.putBoolean(key = AUTH , value = false)
//                println("✅ Logout successful")
//            }
            _login.value = false
            settings.putBoolean(key = AUTH , value = false)
        }

    }

}
















