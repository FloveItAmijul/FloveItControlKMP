package com.floveit.floveitcontrol

import kotlinx.coroutines.flow.StateFlow

/**
 * Common HID API:
 *  - `isConnected` drives your UI
 *  - `discover()` kicks off NSD/Bonjour + socket connect
 *  - `send()` writes HID data
 *  - `disconnect()` tears down
 */
interface HidClient {
    val isConnected: StateFlow<Boolean>
    val serverMessage: StateFlow<String>
    val deviceName: StateFlow<String>
    val deviceID: StateFlow<String>
    suspend fun discover()
    suspend fun send(data: String): Boolean
    fun disconnect()

}

interface WindowController {
    fun enableImmersiveMode()
}


/**
 * Factory: platform modules must supply one.
 */
expect fun provideHidClient(): HidClient
