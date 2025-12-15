package com.floveit.floveitcontrol.viewmodel

import androidx.lifecycle.*
import com.floveit.floveitcontrol.filetransfer.PickedFile
import com.floveit.floveitcontrol.settings.SettingsRepository
import com.floveit.floveitcontrol.settings.mirrors.MirrorDevice
import com.floveit.floveitcontrol.lightControl.LightRepository
import com.floveit.floveitcontrol.platformSpecific.getAppInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch

class FLoveItControlViewModel(
    private val lightRepository: LightRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel(){

    val isConnected : StateFlow<Boolean> = lightRepository.isConnected
    val login : StateFlow<Boolean> = lightRepository.login
    val ledState : StateFlow<Boolean> = lightRepository.ledState
    val ledBrightness : StateFlow<Float> = lightRepository.ledBrightness
    val ledColorTemp : StateFlow<Float> = lightRepository.ledColorTemp
    val boostMode : StateFlow<Boolean> = lightRepository.boostMode
    val makeupMode : StateFlow<Boolean> = lightRepository.makeupMode
    val nightMode : StateFlow<Boolean> = lightRepository.nightMode
    val favouriteMode : StateFlow<Boolean> = lightRepository.favouriteMode
    val connectedMirrors: StateFlow<List<MirrorDevice>> = lightRepository.connectedMirrors
    val isLoginSuccess: StateFlow<Boolean> = lightRepository.isLoginSuccess
    val lastConnectedMirror: StateFlow<MirrorDevice?> = lightRepository.lastConnectedMirror
    val startConnecting: StateFlow<Boolean> = lightRepository.startConnecting
    val currentScreen: StateFlow<Int> = lightRepository.currentScreen
    // Timer and Mouse Settings
    val timer: StateFlow<String> = settingsRepository.timer
    val timerValue: StateFlow<Long> = settingsRepository.timerValue
    private val _trackpadSensitivity = MutableStateFlow(settingsRepository.getTrackpadSensitivity())
    val trackpadSensitivity: StateFlow<Float> get() = _trackpadSensitivity.asStateFlow()
    private val _scrollBar = MutableStateFlow(settingsRepository.getScrollbar())
    val scrollBar: StateFlow<Boolean> get() = _scrollBar.asStateFlow()
    private val _scrollSPosition = MutableStateFlow(settingsRepository.getScrollbarPosition())
    val scrollPosition: StateFlow<String> get() = _scrollSPosition.asStateFlow()
    private val _scrollSensitivity = MutableStateFlow(settingsRepository.getScrollSensitivity())
    val scrollSensitivity: StateFlow<Float> get() = _scrollSensitivity.asStateFlow()
    private val _scrollDirection = MutableStateFlow(settingsRepository.getScrollDirection())
    val scrollDirection: StateFlow<Boolean> get() = _scrollDirection.asStateFlow()




    companion object {
        const val SERVICE_NAME = "FLoveIt"
    }


    init {
        viewModelScope.launch {
            lightRepository
                .observeServerMessages()          // this returns a Flow<String>
                .catch { e ->
                    // 1) Log the error
                    println("⚠️ observeServerMessages failed: ${e.message}")
                    // 2) Maybe retry after a delay:
                    delay(1000L)
                    // re-emit by re-collecting:
                    emitAll(lightRepository.observeServerMessages())
                }
                .collect { message ->
                    // handle each server message here
                    println("FLoveItViewModel observeServerMessages: $message")
                }
        }
        settingsRepository.setTimer(0)
    }

    fun tabNavigation(currentScreen: Int) {
        lightRepository.currentScreen(currentScreen)
    }

    fun startDiscoveryMirror(device: MirrorDevice) {
        viewModelScope.launch {
            lightRepository.startDiscoveryMirror(SERVICE_NAME, device.id)
        }
    }

    fun disconnectMirror() {
        viewModelScope.launch {
            lightRepository.disconnectMirror()
        }
    }

    fun startLastMirrorDiscovery() {
        viewModelScope.launch {
            lightRepository.startLastMirrorDiscovery()
        }
    }

    fun updateLastMirror(device: MirrorDevice) {
        viewModelScope.launch {
            lightRepository.updateLastMirror(device)
        }
    }


    fun addMirror(device: MirrorDevice) {
        viewModelScope.launch {
            lightRepository.addMirrorDevice(device)
        }
    }

    fun removeMirror(device: MirrorDevice) {
        viewModelScope.launch {
            lightRepository.removeMirrorDevice(device)
        }
    }


    fun handleScanData(data: String) {
        viewModelScope.launch {
            lightRepository.handleScanData(data)
        }
    }

    fun sendData(data: String , onResult: (Boolean) -> Unit)  {
        viewModelScope.launch {
            val send = lightRepository.sendData(data)
            onResult(send)
        }
    }

    fun sendAuthenticate(data: String, onResult: (Boolean) -> Unit){
        viewModelScope.launch {
            val sendAuth =  lightRepository.sendAuthenticate(data)
            onResult(sendAuth)
        }
    }

    // update ledState
    fun updateLedState(ledState: Boolean) {
        viewModelScope.launch {
            lightRepository.updateLedState(ledState)
        }

    }

    // Update Brightness
    fun updateLedBrightness(ledBrightness: Float) {
        viewModelScope.launch {
            lightRepository.updateLedBrightness(ledBrightness)
        }
    }

    // update color temperature
    fun updateLedColorTemp(ledColorTemp: Float) {
        viewModelScope.launch {
            lightRepository.updateLedColorTemp(ledColorTemp)
        }
    }

    // update Mode
    fun toggleBoostMode() {
        viewModelScope.launch {
            lightRepository.toggleBoostMode()
        }
    }
    fun toggleMakeupMode(){
        viewModelScope.launch {
            lightRepository.toggleMakeupMode()
        }
    }
    fun toggleNightMode(){
        viewModelScope.launch {
            lightRepository.toggleNightMode()
        }
    }
    fun toggleFavouriteMode(){
        viewModelScope.launch {
            lightRepository.toggleFavouriteMode()
        }
    }

    fun updateAuthStatus(auth: Boolean) {
        lightRepository.updateAuthStatus(auth)
    }

    fun updateConnectedStatus(isConnecting: Boolean){
        lightRepository.updateConnectedStatus(isConnecting)
    }

    // setTimer

    // Next (wraps to first after last)
    fun nextTimer(){
        settingsRepository.nextTimer()
        sendData("Time${timerValue.value}"){}
    }
    // Previous (wraps to last if at first)
    fun previousTimer() {
        settingsRepository.previousTimer()
        sendData("Time${timerValue.value}"){}
    }


    fun setTrackpadSensitivity(value: Float) {
        _trackpadSensitivity.value = value
        settingsRepository.setTrackpadSensitivity(value)
    }

    fun setScrollbar(value: Boolean) {
        _scrollBar.value = value
        settingsRepository.setScrollbar(value)
    }

    fun setScrollbarPosition(value: String) {
        _scrollSPosition.value = value
        settingsRepository.setScrollbarPosition(value)
    }

    fun setScrollSensitivity(value: Float) {
        _scrollSensitivity.value = value
        settingsRepository.setScrollSensitivity(value)
    }

    fun setScrollDirection(value: Boolean) {
        _scrollDirection.value = value
        settingsRepository.setScrollDirection(value)
    }

    fun getAppVersion() : String {
        return getAppInfo().getAppInfo()
    }

    fun sendPickedFile(file: PickedFile, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            runCatching {
                lightRepository.sendPickedFile(file)
            }.onSuccess {
                onResult(true)
            }.onFailure { e ->
                e.printStackTrace() // or Log.e("FileSender", "send failed", e)
                onResult(false)
            }
        }
    }



    fun logout(){
        viewModelScope.launch {
            lightRepository.logout()
        }

    }

}












