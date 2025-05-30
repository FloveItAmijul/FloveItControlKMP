package com.floveit.floveitcontrol.lightControl.helperfunctions

import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.floveit.floveitcontrol.platformSpecific.isAndroid
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun ModeIcon(
    isModeOn: Boolean,
    painterOffRes: DrawableResource,
    painterOnRes: DrawableResource,
    onClick: () -> Unit,
) {
    val painter = if (isModeOn)
        painterResource(painterOnRes)
    else
        painterResource(painterOffRes)

    // ✅ Clip ripple + image inside rounded shape
    Box(
        modifier = Modifier.fillMaxSize()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(30))    // ✅ important: clips ripple + image
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current
            ) { onClick() }
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )
    }
}
