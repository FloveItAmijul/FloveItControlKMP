package com.floveit.floveitcontrol.lightControl.helperfunctions


import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.*
import floveitcontrol.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource

@Composable
fun SwitchButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    thumbColor: Color = Color.White,
    trackColorChecked: Color = Color.Green,
    trackColorUnchecked: Color = Color.LightGray,
    thumbSize: Dp = 24.dp,
    trackWidth: Dp = 48.dp,
    trackHeight: Dp = 20.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Background image
        Image(
            painter = painterResource(Res.drawable.backbutton),
            contentDescription = "Back Button",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )

        Row(modifier.matchParentSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ){


            Column {
                Image(
                    painter = painterResource(if(checked) Res.drawable.light else Res.drawable.light_off),
                    contentDescription = "Light Icon",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.size(48.dp)
                )

            }

            Box(contentAlignment = Alignment.CenterEnd) {
                Image(
                    painter = painterResource(if(checked) Res.drawable.power else Res.drawable.power_off),
                    contentDescription = "Power Icon",
                    contentScale = ContentScale.FillBounds,
                )

                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier.padding(end = 32.dp)
                        .size(width = trackWidth, height = trackHeight)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(
                            color = if (checked) trackColorChecked else trackColorUnchecked
                        )
                        .clickable { onCheckedChange(!checked) }
                ) {

                    Box(
                        modifier = Modifier
                            .size(thumbSize)
                            .offset(
                                x = if (checked) (trackWidth - thumbSize - 5.dp) else 5.dp
                            )
                            .background(color = thumbColor, shape = CircleShape)
                    )
                }

            }

        }


    }

}
