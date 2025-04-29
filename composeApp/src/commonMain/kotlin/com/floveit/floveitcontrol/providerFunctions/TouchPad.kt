package com.floveit.floveitcontrol.providerFunctions

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import floveitcontrol.composeapp.generated.resources.Res
import floveitcontrol.composeapp.generated.resources.mouse_pad
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import kotlin.math.abs

@Composable
fun TouchPad(
    modifier: Modifier = Modifier,
    onPress: () -> Unit,
    onRelease: () -> Unit,
    onLongPress: () -> Unit,
    onDrag: (dx: Float, dy: Float) -> Unit,
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
) {
    // thresholds
    val longPressTimeout = 1500L
    val touchSlopPx = with(LocalDensity.current) { 10.dp.toPx() }
    val swipeVelocityThreshold = 1000f                            // px per second
    val scope = rememberCoroutineScope()

    Box(modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    // 1) Wait for first down
                    val down = awaitFirstDown()
                    var isDragging = false
                    var longPressed = false

                    // VelocityTracker for fling detection
                    val velocityTracker = VelocityTracker()

                    // Schedule long-press
                    val longPressJob = scope.launch {
                        delay(longPressTimeout)
                        if (!isDragging) {
                            longPressed = true
                            onLongPress()
                        }
                    }

                    // 2) Track moves & up
                    while (true) {
                        val event = awaitPointerEvent()
                        // only track our pointer
                        val change = event.changes.first { it.id == down.id }
                        val (dx, dy) = change.positionChange()

                        // a) detect drag start
                        if (!isDragging && (abs(dx) > touchSlopPx || abs(dy) > touchSlopPx)) {
                            isDragging = true
                            longPressJob.cancel()
                        }

                        // b) if dragging, report incremental drag & record velocity
//                        if (isDragging) {
//                            velocityTracker.addPosition(change.uptimeMillis, change.position)
//                            change.consumePositionChange()
//                            onDrag(dx, dy)
//                        }

                        if (isDragging) {
                            velocityTracker.addPosition(change.uptimeMillis, change.position)
                            change.consume()
                            onDrag(dx, dy)
                        }

                        // c) finger lifted?
                        if (!change.pressed) {
                            longPressJob.cancel()

                            when {
                                isDragging -> {
                                    // compute velocity
                                    val v = velocityTracker.calculateVelocity()
                                    val vx = v.x
                                    val vy = v.y

                                    // only if fling speed exceeds threshold do we swipe
                                    if (abs(vx) > swipeVelocityThreshold || abs(vy) > swipeVelocityThreshold) {
                                        if (abs(vx) > abs(vy)) {
                                            if (vx > 0) onSwipeRight() else onSwipeLeft()
                                        } else {
                                            if (vy > 0) onSwipeDown() else  onSwipeUp()
                                        }
                                    }
                                }
                                longPressed -> {
                                    // long-press already fired
                                }
                                else -> {
                                    // plain tap
                                    onPress()
                                }
                            }
                            onRelease()
                            break
                        }
                    }
                }
            }
        }
    ) {
        Image(
            painter = painterResource(Res.drawable.mouse_pad),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = modifier.matchParentSize()
        )
    }
}

