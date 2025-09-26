package com.floveit.floveitcontrol.settings.helper

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.*

@Composable
fun CustomSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    trackHeight: Dp = 30.dp,
    trackWidth: Dp = 60.dp,
    thumbSize: Dp = 24.dp,
    thumbIcon: ImageVector ?= null,
    trackColorChecked: Color = Color(0xFFA790AF),
    trackColorUnchecked: Color = Color.LightGray,
    thumbColor: Color = Color.White
) {
    val thumbOffsetX by animateDpAsState(
        targetValue = if (checked) (trackWidth - thumbSize - 4.dp) else 4.dp,
        label = "ThumbOffset"
    )

    Box(
        modifier = modifier
            .size(width = trackWidth, height = trackHeight)
            .clip(RoundedCornerShape(50))
            .background(if (checked) trackColorChecked else trackColorUnchecked)
            .clickable { onCheckedChange(!checked) }
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffsetX)
                .size(thumbSize)
                .align(Alignment.CenterStart)
                .clip(CircleShape)
                .background(thumbColor),
            contentAlignment = Alignment.Center
        ) {
            thumbIcon?.let {
                if(checked){
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = Color(0xFFA790AF)
                    )
                }

            }
        }
    }
}
