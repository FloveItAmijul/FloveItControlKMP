package com.floveit.floveitcontrol.wifihid

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.*
import com.floveit.floveitcontrol.platformSpecific.isAndroid
import com.floveit.floveitcontrol.viewmodel.FLoveItControlViewModel
import com.floveit.floveitcontrol.wifihid.helperfunction.AmijulSlider
import com.floveit.floveitcontrol.wifihid.helperfunction.ScalableButton
import com.floveit.floveitcontrol.wifihid.helperfunction.TouchPadTwo
import floveitcontrol.composeapp.generated.resources.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock.*
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun WifiHidScreen(modifier: Modifier = Modifier ,viewModel: FLoveItControlViewModel) {

    val scope = rememberCoroutineScope()
    var text by remember { mutableStateOf("") }
    var mute by remember { mutableStateOf(false) }
    var brightness by remember { mutableStateOf(0f) }
    var power by remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current
    val rounded = 20
    val offsetX = remember { Animatable(0f, Float.VectorConverter) }

    val trackpadSensitivity by viewModel.trackpadSensitivity.collectAsState()
    val scrollSensitivity by viewModel.scrollSensitivity.collectAsState()
    val scrollbar by viewModel.scrollBar.collectAsState()
    val scrollPosition by viewModel.scrollPosition.collectAsState()
    val scrollDirection by viewModel.scrollDirection.collectAsState()

    var lastHapticTime by remember { mutableStateOf(Instant.DISTANT_PAST) }

//    val currentTime = System.now()
//
//    if (currentTime - lastHapticTime >= kotlinx.datetime.Duration.parse("1s")) {
//        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
//        lastHapticTime = currentTime
//    }



    suspend fun shakeIcon(){
        offsetX.snapTo(0f)
        val shake = listOf(-10f, 10f, -8f, 8f, -3f, 3f , 0f)
        for (shakeValue in shake){
            offsetX.animateTo(shakeValue , animationSpec = tween(100))
        }
    }


    BoxWithConstraints(modifier = modifier.fillMaxSize()
        .background(Color.Transparent)
    ) {
        val maxHeight = maxHeight
        val iosSpace = maxHeight * 0.018f

        Column(
            modifier.fillMaxSize()
                .padding( end = 10.dp , start = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //Text("Wi-Fi Remote")
            // Keyboard
            Row(Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth()){
                    Image(
                        painter = painterResource(resource = Res.drawable.input),
                        contentDescription = "Input",
                        contentScale = ContentScale.FillBounds,
                        modifier = modifier.matchParentSize()
                    )
                    TextField(
                        value = text,
                        onValueChange = { text = it},
                        shape = RoundedCornerShape(30),
                        leadingIcon = {
                            Box(
                                modifier = Modifier
                                    .size(25.dp) // defines actual clickable/touch area
                                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                                    .clickable(
                                        indication = null, // 👈 disables ripple
                                        interactionSource = remember { MutableInteractionSource() } // required when disabling indication
                                    ) {
                                        if (text.isNotEmpty()) {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                            text = ""
                                            scope.launch { shakeIcon() }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Erase",
                                    tint = if (text.isEmpty()) Color(0xFF696183) else Color.White,
                                    modifier = Modifier.matchParentSize() // just icon size, inside the box
                                )
                            }
                        },

                        singleLine = true,

                        trailingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send Icon",
                                tint = Color.White,
                                modifier = modifier.clickable {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.sendData("Keyboard$text"){}
                                }.size(25.dp)
                            )
                        },
                        colors = TextFieldDefaults.textFieldColors(
                            cursorColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            backgroundColor = Color.Transparent,
                            textColor = Color.White,
                            placeholderColor = Color.White,
                            disabledIndicatorColor = Color.White,
                            errorIndicatorColor = Color.White,
                        ),
                        placeholder = {
                            Text("Input Text" , color = Color.White)
                        },
                        modifier = modifier.fillMaxWidth(),

                        )

                }

            }
            Spacer(modifier = modifier.height(if(isAndroid()) 15.dp else (iosSpace)))
            // Mouse Pad
            Row(modifier.fillMaxWidth().height(maxHeight * 0.47f)
            ) {
                TouchPadTwo(
                    modifier = modifier,
                    onSingleTap = {
                        viewModel.sendData("MouseLeft") { }
                    },
                    onDoubleTap = {

                    },
                    onTwoFingerTap = {

                        viewModel.sendData("MouseRight") { }
                    },
                    onSingleDrag = { dragValue ->
                        viewModel.sendData("MouseMove${dragValue.x * trackpadSensitivity} ,${dragValue.y * trackpadSensitivity}") { }
                        println("Single Drag ${dragValue.x * trackpadSensitivity } and ${dragValue.y * trackpadSensitivity }")

                    },
                    onTwoFingerDrag = { value ->
                        println("Two Finger Drag ${value.x} and ${value.y}")


                        if (value.y > 0) {
                            viewModel.sendData("MouseSwipeDown") { }
                        } else {
                            viewModel.sendData("MouseSwipeUp") { }
                        }

                    },
                    onFingerRelease = { value, position ->
                        viewModel.sendData("MouseRelease") { }
                    },

                    onSinglePressDrag = {value, pressed ->
                        viewModel.sendData("MousePressDragX=${value.x},Y=${value.y},Press=$pressed") {}
                    },
                    onSingleLongPress = {
                        // empty
                    },
                    scrollbar = scrollbar,
                    scrollPosition = scrollPosition,
                    scrollDirection = scrollDirection,
                    onVerticalDrag = { dragAmount ->
                        viewModel.sendData("MouseVerticalScroll${dragAmount  * scrollSensitivity}") {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        println("Vertical Drag ${dragAmount * scrollSensitivity}")

                    },
                    onHorizontalDrag = { dragAmount ->
                        viewModel.sendData("MouseHorizontalScroll$dragAmount") {
                            //hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        println("Horizontal Drag ${dragAmount * scrollSensitivity}")
                    }

                )

            }
            Spacer(modifier = modifier.height(if(isAndroid()) 15.dp else (iosSpace)))
            // Media Function
            Row(modifier = modifier.fillMaxWidth()
                .height(maxHeight * 0.08f)
            ) {

                // 1. Previous (weight 1)
                ScalableButton(
                    modifier = modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.sendData("MediaPrevious") { }
                    }

                ) {
                    Image(
                        painter = painterResource(Res.drawable.previous_music),
                        contentDescription = "Previous",
                        contentScale = ContentScale.FillBounds,
                        modifier = modifier.matchParentSize()
                    )
                }
                Spacer(modifier = modifier.width(12.dp))

                // 2. Play/Pause (weight 2)
                ScalableButton(
                    modifier = modifier
                        .weight(2f)
                        .fillMaxHeight(),
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.sendData("MediaPlay") { }
                    }

                ) {
                    Image(
                        painter = painterResource(Res.drawable.play_pause),
                        contentDescription = "Play/Pause",
                        contentScale = ContentScale.FillBounds,
                        modifier = modifier.matchParentSize()

                    )
                }
                Spacer(modifier = modifier.width(12.dp))
                // 3. Next (weight 1)
                ScalableButton(
                    modifier = modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.sendData("MediaNext") { }
                    }
                ) {
                    Image(
                        painter = painterResource(Res.drawable.next_music),
                        contentDescription = "Next",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.matchParentSize()
                    )
                }

            }
            // Volume Function and quick setting
            Spacer(modifier = modifier.height(if(isAndroid()) 15.dp else (iosSpace)))
            Row(modifier = modifier.fillMaxWidth().height(maxHeight * 0.2f)
            ){
                // first column
                Column(modifier = modifier.weight(3f).height(maxHeight * 0.2f).fillMaxWidth()) {

                    Box(modifier = modifier.height(maxHeight * 0.09f).fillMaxWidth()
                        .clip(RoundedCornerShape(rounded))
                        .clickable {
                            //
                            power = !power
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            if(power){

                                viewModel.sendData("PowerOff") {  }
                            }else {
                                viewModel.sendData("PowerOn") {  }
                            }
                        }
                    ){
                        Image(
                            painter = painterResource(Res.drawable.screen_on_off),
                            contentDescription = "Screen On Off",
                            contentScale = ContentScale.FillBounds,
                            modifier = modifier.matchParentSize()
                        )
                    }

                    Spacer(modifier = modifier.height(if(isAndroid()) 15.dp else (iosSpace)))
                    Row(modifier = modifier.fillMaxWidth().height(maxHeight * 0.09f),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Screenshot Icon
                        Box(modifier = modifier.weight(1f).fillMaxWidth().height(maxHeight * 0.09f)
                            .clip(RoundedCornerShape(rounded))
                            .clickable {
                                //
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.sendData("NavigationScreenshot") {  }
                            }
                        ){
                            Image(
                                painter = painterResource(Res.drawable.screenshot),
                                contentDescription = "Screenshot",
                                contentScale = ContentScale.FillBounds,
                                modifier = modifier.matchParentSize()
                            )
                        }
                        Spacer(modifier = modifier.width(12.dp))
                        // Mute Icon
                        Box(modifier = modifier.weight(1f).fillMaxWidth().height(maxHeight * 0.09f)
                            .clip(RoundedCornerShape(rounded))
                            .clickable {
                                mute = !mute
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)

                                if(!mute){
                                    viewModel.sendData("MediaMute") {  }
                                }else {
                                    viewModel.sendData("MediaUnMute") {  }
                                }
                            }
                        ){
                            Image(
                                painter = painterResource(Res.drawable.mute),
                                contentDescription = "Mute",
                                contentScale = ContentScale.FillBounds,
                                modifier = modifier.matchParentSize()
                            )
                        }

                    }

                }
                Spacer(modifier = modifier.width(12.dp))
                // Second column Volume Control
                Column(modifier = modifier.weight(3f).height(maxHeight * 0.2f).fillMaxWidth()) {

                    Box(modifier = modifier.weight(1f).fillMaxSize()
                        .clip(RoundedCornerShape(rounded))
                        .clickable {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.sendData("MediaVolumeUp") {  }
                        }
                    ){
                        Image(
                            painter = painterResource(Res.drawable.volumeup),
                            contentDescription = "Volume UP",
                            contentScale = ContentScale.FillBounds,
                            modifier = modifier.matchParentSize()
                        )
                    }
                    Spacer(modifier = modifier.height(if(isAndroid()) 15.dp else (iosSpace)))
                    // Mute Icon
                    Box(modifier = modifier.weight(1f).fillMaxSize()
                        .clip(RoundedCornerShape(rounded))
                        .clickable {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.sendData("MediaVolumeDown") {  }
                        }
                    ){
                        Image(
                            painter = painterResource(Res.drawable.volumedown),
                            contentDescription = "Volume Down",
                            contentScale = ContentScale.FillBounds,
                            modifier = modifier.matchParentSize()
                        )
                    }

                }
                Spacer(modifier = modifier.width(12.dp))
                // third column Slider
                Box(
                    modifier = modifier
                        .weight(1f)
                        .height(maxHeight * 0.2f)
                        .fillMaxWidth()
                    //
                ) {
                    Image(
                        painter = painterResource(Res.drawable.brightness_slider),
                        contentDescription = "Slider Background",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(modifier.fillMaxSize() // occupy the same space as image
                        .padding(top = 4.dp , bottom = 4.dp , start = 4.dp , end = 4.dp)
                        .clip(RoundedCornerShape(28.7f))

                    ){
                        Box( modifier = Modifier
                            .fillMaxSize() // occupy the same space as image
                        ){

                            Image(
                                painter = painterResource(Res.drawable.inner_slider),
                                contentDescription = "Slider Background",
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier.fillMaxSize()
                            )
                            AmijulSlider(
                                value = brightness,
                                onValueChange = {
                                    brightness = it
                                    viewModel.sendData("ScreenBrightness$it") { }
                                },
                                min = 0f,
                                max = 1f,
                                icon = {
                                    Icon(
                                        painter = painterResource(Res.drawable.brightness_icon),
                                        contentDescription = "Brightness",
                                        tint = Color(0xFF6E4C6E),
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxSize() // occupy the same space as image
                            )

                        }
                    }



                }

            }
            Spacer(modifier = modifier.height(if(isAndroid()) 15.dp else (iosSpace)))
            // Navigation's Function
            Row(modifier = modifier.fillMaxWidth()
                .height(maxHeight * 0.1f)//.background(Color.White.copy(alpha = 0.1f))
            ) {

                // 1.Recent Task (weight 1)
                ScalableButton(
                    modifier = modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.sendData("NavigationRecent") { }
                    }

                ) {
                    Image(
                        painter = painterResource(Res.drawable.recent_task),
                        contentDescription = "Recent Task",
                        contentScale = ContentScale.FillBounds,
                        modifier = modifier.matchParentSize()
                    )
                }
                Spacer(modifier = modifier.width(12.dp))

                // 2. Home Navigation (weight 2)
                ScalableButton(
                    modifier = modifier
                        .weight(2f)
                        .fillMaxHeight(),
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.sendData("NavigationHome") { }
                    }

                ) {
                    Image(
                        painter = painterResource(Res.drawable.home),
                        contentDescription = "Home Navigation",
                        contentScale = ContentScale.FillBounds,
                        modifier = modifier.matchParentSize()

                    )
                }
                Spacer(modifier = modifier.width(12.dp))
                // 3. Back navigation (weight 1)
                ScalableButton(
                    modifier = modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    onClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.sendData("NavigationBack") { }
                    }
                ) {
                    Image(
                        painter = painterResource(Res.drawable.back),
                        contentDescription = "Back navigation",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.matchParentSize()
                    )
                }

            }
        }

    }

}
