package com.floveit.floveitcontrol.wifihid.helperfunction

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun AmijulSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    min: Float = 0f,
    max: Float = 100f,
    steps: Int = 25, // ✅ number of steps
    fillColor: Color = Color.White,
    backgroundColor: Color = Color(0xFF70536A),
    icon: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val range = (max - min).coerceAtLeast(1f)

// Track the last step that triggered haptic
    var lastStepIndex by remember { mutableStateOf(-1) }


    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            //.clip(RoundedCornerShape(35))
            .background(Color.Transparent)
    ) {
        val heightPx = with(LocalDensity.current) { maxHeight.toPx() }

        val targetValue = ((value - min) / range).coerceIn(0f, 1f)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val touchY = change.position.y
                        val percent = (1f - (touchY / heightPx)).coerceIn(0f, 1f)
                        val rawValue = min + percent * range

                        val steppedValue = if (steps > 0) {
                            val stepSize = range / steps
                            val index = ((rawValue / stepSize).roundToInt()).coerceIn(0, steps)
                            val snapped = min + index * stepSize

                            if (index != lastStepIndex) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                lastStepIndex = index
                            }

                            snapped
                        } else rawValue

                        onValueChange(steppedValue)

                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(targetValue)
                    .align(Alignment.BottomCenter)
                    .clip(RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp))
                    .background(fillColor)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            ) {
                icon()
            }
        }
    }
}

