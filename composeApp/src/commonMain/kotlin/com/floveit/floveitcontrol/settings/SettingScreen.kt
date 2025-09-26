package com.floveit.floveitcontrol.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import cafe.adriel.voyager.navigator.*
import com.floveit.floveitcontrol.navigations.screens.*
import com.floveit.floveitcontrol.platformSpecific.isAndroid
import com.floveit.floveitcontrol.settings.helper.CustomSwitch
import com.floveit.floveitcontrol.settings.helper.TimerButton
import com.floveit.floveitcontrol.viewmodel.FLoveItControlViewModel

@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    viewModel: FLoveItControlViewModel
) {
    val navigator = LocalNavigator.currentOrThrow

    val mirrorDevices by viewModel.connectedMirrors.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    // settings
    val timer by viewModel.timer.collectAsState()
    val trackpadSensitivity by viewModel.trackpadSensitivity.collectAsState()
    val scrollBar by viewModel.scrollBar.collectAsState()
    val scrollbarPosition by viewModel.scrollPosition.collectAsState()
    val scrollSensitivity by viewModel.scrollSensitivity.collectAsState()
    val scrollDirection by viewModel.scrollDirection.collectAsState()

    var showNet by remember { mutableStateOf(false) }
    var appVersion by remember { mutableStateOf("Version ") }


    val settingHeight = if(isAndroid()) 60.dp else 70.dp
    val settingBoxRound = 15
    val settingRound = 25
    val settingColor = Color.White.copy(0.1f)
    val settingHPadding = 15.dp
    val settingTextSize = 14.sp
    val spacedBy = if(isAndroid()) 15.dp else 20.dp

    val trackpadSensitivityList = remember { (1..20).map { it / 5f } }
    val currentIndexOfTrackpad = trackpadSensitivityList.indexOfFirst {
        kotlin.math.abs(it - trackpadSensitivity) < 0.05f
    }.coerceAtLeast(0)

    val scrollSensitivityList = remember { (1..20).map { it / 5f } }
    val currentIndexOfScroll = scrollSensitivityList.indexOfFirst {
        kotlin.math.abs(it - scrollSensitivity) < 0.05f
    }.coerceAtLeast(0)



    // Whenever we reconnect, hide the progress indicator
    LaunchedEffect(isConnected) {
        if (isConnected) {
            showNet = false
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 15.dp)
        .verticalScroll(rememberScrollState())
        ,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacedBy)
    ) {
        // Connection Section
        Box(
            modifier = Modifier.fillMaxWidth()
                .background( color = Color.Black.copy(.17f) , shape = RoundedCornerShape(settingBoxRound))
            ,
            contentAlignment = Alignment.Center
        ) {

            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)
                ,
                verticalArrangement = Arrangement.spacedBy(spacedBy)
            ) {




                // Header Of Connection
                Text("Connection" , color = Color.White)
                Row(modifier = Modifier.fillMaxWidth()
                    .height(settingHeight)
                    .background(color = settingColor, shape = RoundedCornerShape(settingRound))
                    .padding( horizontal = settingHPadding)
                    .clickable {
                        viewModel.disconnectMirror()
                        viewModel.updateAuthStatus(false)
                        navigator.push(LoginScreen)
                    }
                    ,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Add new mirror" , color = Color.White , fontSize = settingTextSize)
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Add new mirror",
                        tint = Color(0xFFD7C2FF),
                        modifier = Modifier.size(30.dp)
                    )
                }

                Row(modifier = Modifier.fillMaxWidth()
                    .height(settingHeight)
                    .background(color = settingColor, shape = RoundedCornerShape(settingRound))
                    .padding( horizontal = settingHPadding)
                    .clickable {
                        navigator.push(MirrorsScreen)// Removed modifier from push
                    }
                    ,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Paired mirrors" , color = Color.White , fontSize = settingTextSize)
                    Text(
                        text = "${mirrorDevices.size}",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }


            }

        }
        // Screen timeout box
        Box(
            modifier = Modifier.fillMaxWidth()
                .background( color = Color.Black.copy(.17f) , shape = RoundedCornerShape(settingBoxRound))
            ,
            contentAlignment = Alignment.Center
        ) {

            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)
                ,
                verticalArrangement = Arrangement.spacedBy(spacedBy)
            ) {

                // Header Of Screen Display
                Text("Display" , color = Color.White)
                Row(modifier = Modifier.fillMaxWidth()
                    .height(settingHeight)
                    .background(color = settingColor, shape = RoundedCornerShape(settingRound))
                    .padding( horizontal = settingHPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Screen timeout", color = Color.White , fontSize = settingTextSize)
                    Box{
                        TimerButton(
                            onIncrement = {
                                viewModel.nextTimer()
                            },
                            onDecrement = {
                                viewModel.previousTimer()
                            },
                            toggle = timer,
                            isToggle = true
                        )
                    }
                }
            }



        }

        // Mouse Setting
        Box( modifier = Modifier.fillMaxWidth()
            .background( color = Color.Black.copy(.17f) , shape = RoundedCornerShape(7))
            ,
            contentAlignment = Alignment.Center
        ) {

            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)
                ,
                verticalArrangement = Arrangement.spacedBy(spacedBy)
            ) {

                // Mouse Header
                Text("Mouse & Input", color = Color.White)
                // Trackpad sensitivity setting
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .height(settingHeight)
                        .background(color = settingColor, shape = RoundedCornerShape(settingRound))
                        .padding(horizontal = settingHPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Trackpad Sensitivity", color = Color.White , fontSize = settingTextSize)
                    Box {
                        TimerButton(
                            onIncrement = {
                                val nextIndex = (currentIndexOfTrackpad + 1).coerceAtMost(trackpadSensitivityList.lastIndex)
                                viewModel.setTrackpadSensitivity(trackpadSensitivityList[nextIndex])
                            },
                            onDecrement = {
                                val prevIndex = (currentIndexOfTrackpad - 1).coerceAtLeast(0)
                                viewModel.setTrackpadSensitivity(trackpadSensitivityList[prevIndex])
                            },
                            value = ((trackpadSensitivity * 5) / 2 ),
                        )
                    }
                }
                // Scrollbar setting
                Row(modifier = Modifier.fillMaxWidth()
                    .height(settingHeight)
                    .background(color = settingColor, shape = RoundedCornerShape(settingRound))
                    .padding( horizontal = settingHPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Scrollbar", color = Color.White)

                    CustomSwitch(
                        checked = scrollBar ,
                        onCheckedChange = { viewModel.setScrollbar(!scrollBar) },
                        trackWidth = 50.dp,
                        trackHeight = 25.dp,
                        thumbSize = 20.dp ,
                        thumbIcon = Icons.Default.Check,
                        trackColorUnchecked = Color(0xFF332537),
                    )
                    //println("scrollbar: $scrollBar")
                }


                Row(
                    modifier = Modifier.fillMaxWidth()
                        .height(settingHeight)
                        .background(color = settingColor, shape = RoundedCornerShape(settingRound))
                        .padding(horizontal = settingHPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Scrollbar Position", color = Color.White , fontSize = settingTextSize)
                    Box {
                        TimerButton(
                            onIncrement = {viewModel.setScrollbarPosition("Right")},
                            onDecrement = { viewModel.setScrollbarPosition("Left")},
                            toggle = scrollbarPosition,
                            isToggle = true
                        )
                    }
                }


                Row(
                    modifier = Modifier.fillMaxWidth()
                        .height(settingHeight)
                        .background(color = settingColor, shape = RoundedCornerShape(settingRound))
                        .padding(horizontal = settingHPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Scrollbar Sensitivity", color = Color.White , fontSize = settingTextSize)
                    Box {
                        TimerButton(
                            onIncrement = {
                                val nextIndex = (currentIndexOfScroll + 1).coerceAtMost(scrollSensitivityList.lastIndex)
                                viewModel.setScrollSensitivity(scrollSensitivityList[nextIndex])
                            },
                            onDecrement = {
                                val prevIndex = (currentIndexOfScroll - 1).coerceAtLeast(0)
                                viewModel.setScrollSensitivity(scrollSensitivityList[prevIndex])
                            },
                            value = ((scrollSensitivity * 5) / 2),
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth()
                    .height(settingHeight)
                    .background(color = settingColor, shape = RoundedCornerShape(settingRound))
                    .padding( horizontal = settingHPadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Scroll Direction Inverse", color = Color.White)


                    CustomSwitch(
                        checked = scrollDirection,
                        onCheckedChange = { viewModel.setScrollDirection(!scrollDirection) },
                        trackWidth = 50.dp,
                        trackHeight = 25.dp,
                        thumbSize = 20.dp ,
                        thumbIcon = Icons.Default.Check,
                        trackColorUnchecked = Color(0xFF332537),
                    )
                }


            }

        }

        // About Device
        Box(
            modifier = Modifier.fillMaxWidth()
                .background( color = Color.Black.copy(.17f) , shape = RoundedCornerShape(settingBoxRound))
            ,
            contentAlignment = Alignment.Center
        ) {

            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)
                ,
                verticalArrangement = Arrangement.spacedBy(spacedBy)
            ) {

                // Header Of About Device
                Text("About Device" , color = Color.White)

                Row(
                    modifier = Modifier.fillMaxWidth()
                        .height(settingHeight)
                        .clickable {
                            appVersion = "Version: ${viewModel.getAppVersion()}"
                        }
                        .background(color = settingColor, shape = RoundedCornerShape(settingRound))
                        .padding(horizontal = settingHPadding),

                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(appVersion, color = Color.White , fontSize = settingTextSize)
                }


                SendFileUi(viewModel = viewModel, modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp))

            }

        }
    }


}
