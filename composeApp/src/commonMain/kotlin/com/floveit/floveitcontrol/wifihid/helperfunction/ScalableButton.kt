package com.floveit.floveitcontrol.wifihid.helperfunction

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch


@Composable
fun ScalableButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }

    suspend fun animateScaleClick() {
        scale.animateTo(0.9f, animationSpec = tween(50))
        scale.animateTo(1f, animationSpec = tween(100))
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            .clip(RoundedCornerShape(20)) // or parametrize
            .clickable {
                scope.launch {
                    animateScaleClick()
                    onClick()
                }
            },
        contentAlignment = Alignment.Center,
        content = content
    )
}
