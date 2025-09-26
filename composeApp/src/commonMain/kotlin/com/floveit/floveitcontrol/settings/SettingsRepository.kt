package com.floveit.floveitcontrol.settings

import com.floveit.floveitcontrol.platformSpecific.provideSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SettingsRepository {

    private val settings: Settings = provideSettings()

    companion object {
        private const val TRACKPAD_SENSITIVITY_KEY = "trackpadSensitivity"
        private const val SCROLLBAR_KEY = "scrollBar"
        private const val SCROLLBAR_POSITION_KEY = "scrollBarPosition"
        private const val SCROLL_SENSITIVITY_KEY = "scrollSensitivity"
        private const val SCROLL_DIRECTION_KEY = "scrollDirection"
    }

    private val _timerIndex = MutableStateFlow(0)
    private val _timerValue = MutableStateFlow(0L)
    val timerValue: StateFlow<Long> get() = _timerValue
    private val _timer = MutableStateFlow("")
    val timer: StateFlow<String> get() = _timer


    val timers = listOf("15s", "30s", "1 min", "2 min", "5 min", "10 min", "30 min", "Never")
    val timerValues = listOf(15000L, 30000L, 60000L, 120000L, 300000L, 600000L, 1800000L, 0L)


    // Set timer explicitly
    fun setTimer(index: Int) {
        val validIndex = index.coerceIn(timers.indices)
        _timerIndex.value = validIndex
        _timer.value = timers[validIndex]
        _timerValue.value = timerValues[validIndex]
    }

    // Next (wraps to first after last)
    fun nextTimer(){
        val nextIndex = (_timerIndex.value + 1) % timers.size
        setTimer(nextIndex)
    }
    // Previous (wraps to last if at first)
    fun previousTimer() {
        val prevIndex = if(_timerIndex.value == 0) timers.lastIndex else _timerIndex.value - 1
        setTimer(prevIndex)
    }


    fun setTrackpadSensitivity(value: Float) {
        settings.putFloat(TRACKPAD_SENSITIVITY_KEY, value)
    }

    fun getTrackpadSensitivity(): Float {
        return settings.getFloat(TRACKPAD_SENSITIVITY_KEY, defaultValue = 0F)
    }

    fun setScrollbar(value: Boolean) {
        settings.putBoolean(SCROLLBAR_KEY, value)
    }

    fun getScrollbar(): Boolean {
        return settings.getBoolean(SCROLLBAR_KEY, defaultValue = true)
    }

    fun setScrollbarPosition(value: String) {
        settings.putString(SCROLLBAR_POSITION_KEY, value)
    }

    fun getScrollbarPosition(): String {
        return settings.getString(SCROLLBAR_POSITION_KEY, defaultValue = "Right")
    }

    fun setScrollSensitivity(value: Float) {
        settings.putFloat(SCROLL_SENSITIVITY_KEY, value)
    }

    fun getScrollSensitivity(): Float {
        return settings.getFloat(SCROLL_SENSITIVITY_KEY, defaultValue = 0f)
    }

    fun setScrollDirection(value: Boolean) {
        settings.putBoolean(SCROLL_DIRECTION_KEY, value)
    }

    fun getScrollDirection(): Boolean {
        return settings.getBoolean(SCROLL_DIRECTION_KEY, defaultValue = false)
    }


}