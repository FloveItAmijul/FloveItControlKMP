package com.floveit.floveitcontrol.meterialui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity

@Composable
fun BackgroundColor(content: @Composable () -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxWidth = this.maxWidth
        val maxHeight = this.maxHeight
        // Get density to convert Dp to pixels.
        val density = LocalDensity.current
        // Calculate the end offset based on the available width and height.
        val endOffset = with(density) { Offset(maxWidth.toPx(), maxHeight.toPx()) }

        // Create a linear gradient with specific color stops.
        val gradientBrush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF281254),
                Color(0xFF95687B)
            )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = gradientBrush)
        ) {
            content()
        }
    }
}
