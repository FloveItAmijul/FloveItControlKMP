package com.floveit.floveitcontrol.navigations

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.floveit.floveitcontrol.lightControl.LightScreen
import com.floveit.floveitcontrol.navigations.custombottombar.CustomBottomBar
import com.floveit.floveitcontrol.navigations.helperfunction.tutorial.Tutorial
import com.floveit.floveitcontrol.navigations.helperfunction.tutorial.TutorialPage
import com.floveit.floveitcontrol.platformSpecific.CameraView
import com.floveit.floveitcontrol.platformSpecific.isAndroid
import com.floveit.floveitcontrol.settings.SettingScreen
import com.floveit.floveitcontrol.viewmodel.FloveItControlViewModel
import com.floveit.floveitcontrol.wifihid.WifiHidScreen
import floveitcontrol.composeapp.generated.resources.Res
import floveitcontrol.composeapp.generated.resources.navbg
import floveitcontrol.composeapp.generated.resources.tuto1
import floveitcontrol.composeapp.generated.resources.tuto2
import floveitcontrol.composeapp.generated.resources.tuto3
import floveitcontrol.composeapp.generated.resources.tuto4
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource


@Composable
fun FLoveItNavigation(modifier: Modifier = Modifier  , viewModel: FloveItControlViewModel){

    // 0 = Settings, 1 = Light, 2 = Mouse; default selected is Light.
   // val currentScreen by bluetoothViewModel.currentScreen.collectAsState()
    var hasScanned by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf(1) }
    var showTutorial by remember { mutableStateOf(true) }
    var tutorialPage by remember { mutableStateOf(0) }
    var showScan by remember { mutableStateOf(false) }

    val connected by viewModel.isConnected.collectAsState()
    val login by viewModel.login.collectAsState()

    val pages  = listOf(
        TutorialPage(Res.drawable.tuto1 , "Open this app on your smart mirror"),
        TutorialPage(Res.drawable.tuto2 , "Go to settings within the FloveIt mirror app"),
        TutorialPage(Res.drawable.tuto3, "Navigate to Linked Devices in the setting")
    )
    LaunchedEffect(login) {
        if (!login) {
            showTutorial = true
            showScan = false
            hasScanned = false
            tutorialPage = 0
        }
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (login) {
            Box(modifier.fillMaxWidth().weight(1f)) {

                when (currentScreen) {
                    0 -> SettingScreen(viewModel = viewModel)
                    1 -> LightScreen(viewModel = viewModel)
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
                    onSettingsClick = { currentScreen = 0 },
                    onLightClick = { currentScreen = 1 },
                    onMouseClick = { currentScreen = 2 }
                )
            }

        } else {

            if(showTutorial){

                Tutorial(modifier = modifier,
                    pages = pages,
                    currentPage = tutorialPage,
                    onNext = {
                        tutorialPage++
                    },
                    onBack = {
                        tutorialPage--
                    },
                    onFinish = { showTutorial = false}
                )

            } else {

                Column( modifier = modifier
                    .fillMaxSize()
                    //.background(Color.White)
                    .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.clickable {
                                showTutorial = true
                            }
                        )

                    }

                    Spacer(Modifier.height(32.dp))
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                        //.background(Color.Black.copy(alpha = 0.2f))
                        ,
                        contentAlignment = Alignment.Center
                    ) {

                        Column( modifier = Modifier
                            .fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            //verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Scan QR",
                                style = MaterialTheme.typography.body1.copy(
                                    fontSize = 28.sp
                                ),
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight(550),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                            //Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Scan the QR display on your smart mirror to complete setup",
                                style = MaterialTheme.typography.body1.copy(
                                    fontSize = 18.sp,
                                    lineHeight = 24.sp  // <- more room between wrapped lines
                                ),
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight(370),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                            Spacer(Modifier.height(24.dp))
                            Box(
                                modifier = Modifier.fillMaxWidth(0.9f)
                                    .aspectRatio(1f)
                                    .clip(shape = RoundedCornerShape(8.dp))
                                    .clipToBounds(),
                                contentAlignment = Alignment.Center
                            ) {
                                if(!showScan){
                                    Image(
                                        painter = painterResource(Res.drawable.tuto4),
                                        contentDescription = null,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .aspectRatio(1f)
                                            .clickable {
                                                showScan = true
                                            }
                                    )
                                } else {
                                    if(connected){

                                        CameraView(onQRCodeScanned = { result ->
                                            if(!hasScanned && result.startsWith("FloveIt")){
                                                hasScanned = true
                                                viewModel.sendAuthenticate("authenticated") { send ->
                                                    if (send) {
                                                        viewModel.handleScanData(result)

                                                    }
                                                    println("QR code result = $result & sending: $send")
                                                }
                                            }
                                        })

                                    } else {

                                        LaunchedEffect(Unit) {
                                            while (!connected){
                                                viewModel.discover()
                                                delay(5000L)
                                                println("Searching...")
                                            }

                                        }
                                        Column(
                                            modifier = modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ){
                                            CircularProgressIndicator(
                                                modifier = modifier.size(64.dp),
                                                color = Color.White,
                                                strokeWidth = 8.dp ,
                                                strokeCap = StrokeCap.Round,
                                                backgroundColor = Color(0xFF999999)
                                            )

                                            Text("Please Check Your Wifi Connection" , color = Color.White)
                                            Text("&", color = Color.White)
                                            Text("Your Smart Mirror Wifi Connection", color = Color.White)

                                        }

                                    }
                                }

                            }

                        }


                    }


                }


            }

        }

    }

}
