package com.floveit.floveitcontrol.lightControl

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.floveit.floveitcontrol.lightControl.helperfunctions.*
import com.floveit.floveitcontrol.platformSpecific.isAndroid
import com.floveit.floveitcontrol.viewmodel.FLoveItControlViewModel
import floveitcontrol.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource

@Composable
fun LightScreen(modifier: Modifier = Modifier, viewModel: FLoveItControlViewModel){

    val brightness by viewModel.ledBrightness.collectAsState()
    val colorTemp by viewModel.ledColorTemp.collectAsState()
    val ledState by viewModel.ledState.collectAsState()
    val boostMode by viewModel.boostMode.collectAsState()
    val makeupMode by viewModel.makeupMode.collectAsState()
    val nightMode by viewModel.nightMode.collectAsState()
    val favouriteMode by viewModel.favouriteMode.collectAsState()
    val login by viewModel.login.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    var sendBrightness by remember { mutableStateOf(false) }
    var sendColorTemp by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = modifier.fillMaxSize().padding(horizontal = 10.dp)) {
        val maxHeight = maxHeight
        val maxWidth = maxWidth
        // ✅ Main control panel center
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Custom Switch
            Box(modifier = modifier.widthIn(max = 380.dp)
                .heightIn(min = 130.dp , max = 150.dp)) {
                SwitchButton(
                    connection = isConnected,
                    checked = ledState,
                    onCheckedChange = {
                        val newState = !ledState
                        viewModel.updateLedState(newState)
                    },
                    thumbColor = Color.White,
                    thumbSize = 30.dp,
                    trackWidth = 55.dp,
                    trackHeight = 35.dp
                )
            }

            Spacer(modifier.height(if(isAndroid()) 30.dp else (maxHeight * 0.04f)))
            // Sliders
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painter = painterResource(Res.drawable.b_icon),
                        contentDescription = "Brightness",
                        contentScale = ContentScale.FillBounds,
                        //modifier = Modifier.size(45.dp)
                    )
                    Spacer(modifier.height(25.dp))
                    FloveItSlider(

                        value = brightness,
                        onValueChange = {newBrightness ->
                            if(!sendBrightness){
                                sendBrightness = true
                            }else if(login){
                                viewModel.updateLedBrightness(newBrightness)
                            }

                        },
                        min = 0f,
                        max = 100f,
                        trackWidth = if(isAndroid()) 95.dp else 105.dp,
                        sliderHeight = if(isAndroid()) 290.dp else (maxHeight * 0.43f),
                        onValueFinished = { /*if (isLogin && !isSending) clientViewModel.finishBrightness(it * 100)*/ },
                        onValueTap = {
                            if(login){
                                viewModel.updateLedBrightness(it)
                            }
                        },
                        isEnable = (!boostMode && !makeupMode && !nightMode && !favouriteMode), //!boostMode
                        showValue = true,
                        sensitivity = 2f ,
                    )

                }
                Spacer(modifier.width(if(isAndroid()) 60.dp else (maxWidth * 0.18f)))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(painter = painterResource(Res.drawable.colortemp),
                        contentDescription = "Color Temp",
                        contentScale = ContentScale.FillBounds,
                        //modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier.height(25.dp))
                    FloveItSlider(
                        value = colorTemp ,
                        onValueChange = { newColorTemp ->
                            if(!sendColorTemp){
                                sendColorTemp = true
                            }else if(login){
                                viewModel.updateLedColorTemp(newColorTemp)
                            }

                        },
                        min = 0f,
                        max = 255f,
                        trackWidth = if(isAndroid()) 95.dp else 105.dp,
                        sliderHeight = if(isAndroid()) 290.dp else (maxHeight * 0.43f),
                        onValueFinished = { /*if (isLogin && !isSending) clientViewModel.finishWarmCool(it * 255)*/ },
                        onValueTap = {
                            if (login){
                                viewModel.updateLedColorTemp(it)
                            }

                        },
                        isEnable = (!boostMode && !makeupMode && !nightMode && !favouriteMode),
                        gradientSliderColors = listOf(
                            Color(
                                red = 1.00f,
                                green = 1.00f,
                                blue = 0.639f,
                                alpha = 1f
                            ), Color.White
                        ),
                        sensitivity = 2f,

                    )
                }

            }

            Spacer(modifier.height(if(isAndroid()) 40.dp else (maxHeight * 0.05f)))

            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier.weight(1f).fillMaxHeight()){
                    ModeIcon(isModeOn = boostMode, painterOnRes = Res.drawable.boost_on, painterOffRes = Res.drawable.boost_off,
                        onClick = { viewModel.toggleBoostMode() }
                    )
                }


                Box(modifier.weight(1f).fillMaxHeight()){
                    ModeIcon(isModeOn = makeupMode, painterOnRes = Res.drawable.makeup_on, painterOffRes = Res.drawable.makeup_off,
                        onClick = { viewModel.toggleMakeupMode() }
                    )
                }



                Box(modifier.weight(1f).fillMaxHeight()){
                    ModeIcon(isModeOn = nightMode, painterOnRes = Res.drawable.night_on, painterOffRes = Res.drawable.night_off,
                        onClick = { viewModel.toggleNightMode() }
                    )
                }


                Box(modifier.weight(1f)){
                    ModeIcon(isModeOn = favouriteMode, painterOnRes = Res.drawable.favourite_on, painterOffRes = Res.drawable.favourite_off,
                        onClick = { viewModel.toggleFavouriteMode() }
                    )
                }

            }
        }
    }




}











