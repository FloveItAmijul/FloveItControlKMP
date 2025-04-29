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
    suspend fun discover()
    suspend fun send(data: String)
    fun disconnect()
}

/**
 * Factory: platform modules must supply one.
 */
expect fun provideHidClient(): HidClient
