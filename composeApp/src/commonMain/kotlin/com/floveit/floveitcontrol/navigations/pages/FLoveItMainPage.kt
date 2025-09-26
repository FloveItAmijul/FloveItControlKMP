package com.floveit.floveitcontrol.navigations.pages

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.*
import com.floveit.floveitcontrol.lightControl.LightScreen
import com.floveit.floveitcontrol.navigations.custombottombar.CustomBottomBar
import com.floveit.floveitcontrol.platformSpecific.*
import com.floveit.floveitcontrol.settings.SettingScreen
import com.floveit.floveitcontrol.viewmodel.FLoveItControlViewModel
import com.floveit.floveitcontrol.wifihid.WifiHidScreen
import floveitcontrol.composeapp.generated.resources.*
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource


@Composable
fun FLoveItMainPage(modifier: Modifier = Modifier  , viewModel: FLoveItControlViewModel){


    val currentScreen by viewModel.currentScreen.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()


    LaunchedEffect(isConnected) {
        while(!isConnected){
            viewModel.startLastMirrorDiscovery()
            println("Trying To Connected")
            delay(7000L)
        }
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {



            Box(modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {

                when (currentScreen) {
                    0 -> SettingScreen(modifier = modifier ,viewModel = viewModel)
                    1 -> LightScreen(modifier = modifier ,viewModel = viewModel)
                    2 -> WifiHidScreen(modifier = modifier, viewModel = viewModel)
                }
            }

            // Floating bottom bar, aligned at the bottom center.
            Box(modifier = modifier
                .fillMaxWidth()
                .padding(top = 10.dp , start = 10.dp , end = 10.dp , bottom = if(isAndroid()) 10.dp else 20.dp)
            ){
                Image(
                    painter = painterResource(Res.drawable.navbg),
                    contentDescription = "Navigation Background",
                    contentScale = ContentScale.FillBounds,
                    modifier = modifier.matchParentSize()
                )
                CustomBottomBar(
                    //  modifier.align(Alignment.Center),
                    selectedScreen = currentScreen,
                    onSettingsClick = { viewModel.tabNavigation(0)  },
                    onLightClick = {  viewModel.tabNavigation(1) },
                    onMouseClick = {  viewModel.tabNavigation(2) }
                )
            }

    }
}
