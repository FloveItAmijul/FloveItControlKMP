package com.floveit.floveitcontrol.navigations.custombottombar


import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun PillButton(
    selected: Boolean,
    resource: DrawableResource,
    selectedResource: DrawableResource,
    onClick: () -> Unit
) {
    if (selected) {

        Box(
            modifier = Modifier
                //.height(100.dp)
                .clickable { onClick() }
                .clip(shape = RoundedCornerShape(35))


        ) {
            Image(
                painter = painterResource(resource = selectedResource),
                contentDescription = "$selectedResource",
                contentScale = ContentScale.FillBounds
            )
        }

    } else {

        Box(modifier = Modifier.clickable {
            onClick ()
        }
        ){
            Image(
                painter = painterResource(resource = resource),
                contentDescription = "$resource",
                contentScale = ContentScale.FillBounds

            )
        }

    }
}

