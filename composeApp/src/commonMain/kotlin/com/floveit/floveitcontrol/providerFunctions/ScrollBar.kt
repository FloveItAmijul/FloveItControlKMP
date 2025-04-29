package com.floveit.floveitcontrol.providerFunctions

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectVerticalDragGestures      // ①
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import floveitcontrol.composeapp.generated.resources.Res
import floveitcontrol.composeapp.generated.resources.scroll
import org.jetbrains.compose.resources.painterResource

@Composable
fun ScrollBar(
    modifier: Modifier = Modifier,
    onScrollUp: () -> Unit,
    onScrollDown: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            // ② replace your manual pointer loop with a built-in vertical-drag detector
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                    change.consume()    // mark this motion handled
                    // ③ dragAmount < 0 = user moved finger up → scroll content up
                    if (dragAmount < 0f) {
                        onScrollUp()
                    } else if (dragAmount > 0f) {
                        onScrollDown()
                    }
                }
            }
    ) {
        Image(
            painter = painterResource(resource = Res.drawable.scroll),
            contentDescription = "Scroll Bar",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )
    }
}
