package com.floveit.floveitcontrol.settings.helper

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import floveitcontrol.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource

@Composable
fun TimerButton(
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    value: Float = 0f,
    toggle: String = "",
    modifier: Modifier = Modifier,
    isToggle: Boolean = false
) {

    Box(modifier = modifier.width(100.dp)//.padding(end = 10.dp)//.background(Color.White.copy(.3f))
        ,
        contentAlignment = Alignment.CenterEnd
    ){
        Image(
            painter = painterResource(Res.drawable.timer_background),
            contentDescription = "timer",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Image(
                    painter = painterResource(Res.drawable.minus),
                    contentDescription = "minus",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.clickable { onDecrement() }
                )
            }

            Box {
                if(isToggle){
                    Text(toggle , color = Color.White)
                }else {
                    Text("${value.toInt()}" , color = Color.White)
                }

            }


            Box {
                Image(
                    painter = painterResource(Res.drawable.pluse),
                    contentDescription = "plus",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.clickable { onIncrement() }
                )
            }
        }
    }
}