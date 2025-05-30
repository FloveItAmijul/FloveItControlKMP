package com.floveit.floveitcontrol.lightControl.helperfunctions

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.*
import kotlin.math.roundToInt



@Composable
fun FloveItSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueFinished: (Float) -> Unit,
    onValueTap: (Float) -> Unit,
    min: Float = 0f,
    max: Float = 1f,
    sliderHeight: Dp = 250.dp,
    trackWidth: Dp = 70.dp,
    unfilledTrackColor: Color = Color.Black.copy(alpha = 0.38f),
    gradientSliderColors: List<Color> = listOf(Color.White, Color.LightGray),
    gradientDisableThumbColors: List<Color> = listOf(Color.Gray, Color.Gray),
    textColor: Color = if((value) >= 90) Color.Black else Color.White,
    isEnable: Boolean = true,
    showValue: Boolean = false,
    sensitivity: Float = 1f,
    modifier: Modifier = Modifier
) {
    val range = (max - min).coerceAtLeast(0.01f)
    val density = LocalDensity.current
    val sliderHeightPx = with(density) { sliderHeight.toPx() }

    // Convert actual value to normalized value (0f..1f)
    val normalizedValue = ((value - min) / range).coerceIn(0f, 1f)
    var dragOffset by remember { mutableStateOf((1 - normalizedValue) * sliderHeightPx) }

    LaunchedEffect(value) {
        val normalized = ((value - min) / range).coerceIn(0f, 1f)
        dragOffset = (1 - normalized) * sliderHeightPx
        onValueChange(value)
    }

    Box(
        modifier = modifier
            .width(trackWidth)
            .height(sliderHeight)
            .clip(RoundedCornerShape(35))
            .background(unfilledTrackColor)
            .pointerInput(isEnable) {
                detectTapGestures { offset ->
                    if (isEnable) {
                        val normalized = (1 - offset.y / sliderHeightPx).coerceIn(0f, 1f)
                        val actualValue = (normalized * range + min).coerceIn(min, max)
                        dragOffset = (1 - normalized) * sliderHeightPx
                        onValueChange(actualValue)
                        onValueTap(actualValue)
                    }
                }
            }
            .draggable(
                orientation = Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    if (isEnable) {
                        dragOffset = (dragOffset + delta * sensitivity).coerceIn(0f, sliderHeightPx)
                        val normalized = (1 - dragOffset / sliderHeightPx).coerceIn(0f, 1f)
                        val actualValue = (normalized * range + min).coerceIn(min, max)
                        onValueChange(actualValue)
                    }
                },
                onDragStopped = {
                    if (isEnable) {
                        val normalized = (1 - dragOffset / sliderHeightPx).coerceIn(0f, 1f)
                        val actualValue = (normalized * range + min).coerceIn(min, max)
                        onValueFinished(actualValue)
                    }
                }
            )
    ) {
        // Filled Track
        Canvas(modifier = Modifier.fillMaxSize()) {
            val filledHeight = size.height * normalizedValue
            val gradient = Brush.verticalGradient(
                colors = if (isEnable) gradientSliderColors else gradientDisableThumbColors,
                startY = 0f,
                endY = size.height
            )

            drawRect(
                brush = gradient,
                topLeft = Offset(0f, size.height - filledHeight),
                size = Size(size.width, filledHeight)
            )
        }
        // display value
        if(showValue){
            Text(
                text = "${value.roundToInt()}%",
                color = textColor,
                style = MaterialTheme.typography.h6,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
            )
        }


    }
}
