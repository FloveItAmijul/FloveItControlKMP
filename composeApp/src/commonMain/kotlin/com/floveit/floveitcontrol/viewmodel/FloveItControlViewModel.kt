package com.floveit.floveitcontrol.viewmodel

import androidx.lifecycle.*
import com.floveit.floveitcontrol.settings.mirrors.MirrorDevice
import com.floveit.floveitcontrol.lightControl.LightRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.launch

class FLoveItControlViewModel(private val lightRepository: LightRepository) : ViewModel(){

    val findingMirror : StateFlow<Boolean> = lightRepository.findingMirror
    val isConnected : StateFlow<Boolean> = lightRepository.isConnected
    val login : StateFlow<Boolean> = lightRepository.login
    val isLogin : StateFlow<Boolean> = lightRepository.isLogin
    val ledState : StateFlow<Boolean> = lightRepository.ledState
    val ledBrightness : StateFlow<Float> = lightRepository.ledBrightness
    val ledColorTemp : StateFlow<Float> = lightRepository.ledColorTemp
    val boostMode : StateFlow<Boolean> = lightRepository.boostMode
    val makeupMode : StateFlow<Boolean> = lightRepository.makeupMode
    val nightMode : StateFlow<Boolean> = lightRepository.nightMode
    val favouriteMode : StateFlow<Boolean> = lightRepository.favouriteMode
    val connectedMirrors: StateFlow<List<MirrorDevice>> = lightRepository.connectedMirrors
    val isLoginSuccess: StateFlow<Boolean> = lightRepository.isLoginSuccess



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
                    println("FloveItViewModel observeServerMessages: $message")
                }
        }
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

    fun logout(){
        viewModelScope.launch {
            lightRepository.logout()
        }

    }

}












