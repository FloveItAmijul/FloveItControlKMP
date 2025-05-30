package com.floveit.floveitcontrol.wifihid.helperfunction

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.ContentScale
import floveitcontrol.composeapp.generated.resources.*
import kotlinx.coroutines.*
import org.jetbrains.compose.resources.painterResource

@Composable
fun TouchPadTwo(
    modifier: Modifier = Modifier,
    onSingleDrag: (Offset) -> Unit,
    onTwoFingerDrag: (Offset) -> Unit,
    onTwoFingerTap: () -> Unit,
    onSingleTap: () -> Unit,
    onDoubleTap: () -> Unit = {},
    onSinglePressDrag: (Offset, Boolean) -> Unit = { _, _ -> },
    onSingleLongPress: () -> Unit = {},
    onFingerRelease: (Int, Offset) -> Unit = { _, _ -> }
) {

    val scope = rememberCoroutineScope()

    // We store a reference to the "pending single tap" job so we can cancel if a second tap arrives
    var pendingSingleTapJob by remember { mutableStateOf<Job?>(null) }

    // The typical double-tap window
    val doubleTapTimeout = 300L


    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource( Res.drawable.mouse2),
            contentDescription = "Mouse Pad",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
                .pointerInput(Unit) {
                    while (true) {
                        val firstDown = awaitPointerEventScope { awaitFirstDown() }
                        val firstDownTime = firstDown.uptimeMillis
                        val initialPositions = mutableMapOf(firstDown.id to firstDown.position)
                        val lastPositions = mutableMapOf(firstDown.id to firstDown.position)

                        var dragDetected = false
                        var isLongPress = false
                        var multiTouch = false
                        val threshold = 10f

                        // Detect long press after 300ms
                        val longPressJob = scope.launch {
                            delay(300)
                            if (!dragDetected && firstDown.pressed) {
                                isLongPress = true

                            }
                        }

                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()

                                // Track finger release
                                event.changes.forEach { pointer ->
                                    if (pointer.changedToUp()) {
                                        onFingerRelease(pointer.id.value.toInt(), pointer.position)
                                    }
                                }

                                // Register new pointers
                                event.changes.forEach { pointer ->
                                    if (pointer.id !in initialPositions && pointer.pressed) {
                                        initialPositions[pointer.id] = pointer.position
                                        lastPositions[pointer.id] = pointer.position
                                        if (pointer.uptimeMillis - firstDownTime < 10L) {
                                            multiTouch = true
                                        }
                                    }
                                }

                                var totalDelta = Offset.Zero

                                event.changes.forEach { pointer ->
                                    val lastPos = lastPositions[pointer.id] ?: pointer.position
                                    val delta = pointer.position - lastPos
                                    lastPositions[pointer.id] = pointer.position

                                    // If movement > threshold => drag
                                    val initPos = initialPositions[pointer.id] ?: pointer.position
                                    if (!dragDetected && (pointer.position - initPos).getDistance() > threshold) {
                                        dragDetected = true
                                        longPressJob.cancel()
                                    }

                                    totalDelta += delta
                                }

                                // If a drag is detected, call relevant callbacks
                                val activePointers = event.changes.filter { it.pressed }
                                if (dragDetected) {
                                    if (activePointers.size >= 2 || multiTouch) {
                                        val avgDelta = totalDelta /
                                                (if (activePointers.size >= 2) activePointers.size.toFloat() else 1f)
                                        onTwoFingerDrag(avgDelta)
                                    } else if (activePointers.size == 1) {
                                        if (isLongPress) {
                                            onSinglePressDrag(totalDelta * 3f, true)
                                        } else {
                                            onSingleDrag(totalDelta)
                                        }
                                    }
                                }

                                // Break if all pointers lifted
                                if (event.changes.all { !it.pressed }) break
                            }
                        }

                        // Cancel long press if still active
                        if (longPressJob.isActive) {
                            longPressJob.cancel()
                        }

                        // If no drag => treat as a tap
                        if (!dragDetected) {
                            if (initialPositions.size == 1) {
                                if (isLongPress) {
                                    onSingleLongPress()
                                } else {
                                    // Double Tap Changes:
                                    // 1) Cancel any pending single-tap job => we might do a double tap
                                    // 2) If we had no pending job, create one to call onSingleTap() after a delay
                                    //    That gives time to see if a second tap will arrive
                                    if (pendingSingleTapJob?.isActive == true) {
                                        // => A second tap arrived quickly => it's a double tap
                                        pendingSingleTapJob?.cancel()
                                        pendingSingleTapJob = null
                                        onDoubleTap()
                                    } else {
                                        // => This is the first tap -> schedule single tap
                                        pendingSingleTapJob = scope.launch {
                                            delay(doubleTapTimeout)
                                            onSingleTap()
                                            pendingSingleTapJob = null
                                        }
                                    }
                                }
                            } else if (initialPositions.size >= 2) {
                                onTwoFingerTap()
                            }
                        }
                    }
                },
        )
    }
}

