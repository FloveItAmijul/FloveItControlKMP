package com.floveit.floveitcontrol.navigations.pages

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import cafe.adriel.voyager.navigator.*
import com.floveit.floveitcontrol.navigations.helperfunction.tutorial.*
import com.floveit.floveitcontrol.navigations.screens.MainDashboardScreen
import com.floveit.floveitcontrol.platformSpecific.CameraView
import com.floveit.floveitcontrol.platformSpecific.isAndroid
import com.floveit.floveitcontrol.viewmodel.FLoveItControlViewModel
import floveitcontrol.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource

@Composable
fun LoginPage(modifier: Modifier, viewModel: FLoveItControlViewModel){

    val navigator = LocalNavigator.currentOrThrow

    val isConnected by viewModel.isConnected.collectAsState()
    val isLoginSuccess by viewModel.isLoginSuccess.collectAsState()
    val login by viewModel.login.collectAsState()
    val startConnecting by viewModel.startConnecting.collectAsState()

    var hasScanned by remember { mutableStateOf(false) }
    var showTutorial by remember { mutableStateOf(true) }
    var tutorialPage by remember { mutableStateOf(0) }
    var showScan by remember { mutableStateOf(false) }


    val pages  = listOf(
        TutorialPage(Res.drawable.tuto1 , "Open this app on your smart mirror"),
        TutorialPage(Res.drawable.tuto2 , "Go to settings within the FLoveIt mirror app"),
        TutorialPage(Res.drawable.tuto3, "Navigate to Linked Devices in the setting")
    )

    LaunchedEffect(isLoginSuccess) {
        if(isLoginSuccess){
            navigator.push(MainDashboardScreen)
        }
    }


    if(showTutorial && !login){

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

            Spacer(Modifier.height( if(isAndroid()) 2.dp else 16.dp))

            Row(
                Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.clickable {
                        if(login){
                            navigator.pop()
                        }else {
                            showTutorial = true
                        }


                    }
                )

                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.clickable {
                        navigator.push(MainDashboardScreen)
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

                            CameraView(onQRCodeScanned = { result ->
                                if(!hasScanned && result.startsWith("FLoveIt")){
                                    hasScanned = true
                                    viewModel.handleScanData(result)
                                }
                            })

                        }

                        if(startConnecting){
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = Color.White,
                                strokeWidth = 6.dp,
                                backgroundColor = Color.Green,
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }
                }
            }
        }
    }
}