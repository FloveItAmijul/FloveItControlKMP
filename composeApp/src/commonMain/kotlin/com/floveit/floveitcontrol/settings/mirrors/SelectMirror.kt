package com.floveit.floveitcontrol.settings.mirrors


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import cafe.adriel.voyager.navigator.*
import com.floveit.floveitcontrol.viewmodel.FLoveItControlViewModel
import floveitcontrol.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource

@Composable
fun SelectMirror(modifier: Modifier = Modifier , viewModel: FLoveItControlViewModel) {

    val mirrorDevices by viewModel.connectedMirrors.collectAsState()
    val connected by viewModel.isConnected.collectAsState()
    val lastConnectedMirror by viewModel.lastConnectedMirror.collectAsState()
    val navigator = LocalNavigator.currentOrThrow

    var tryingToConnect by remember { mutableStateOf(false) }


    LaunchedEffect(connected) {
        if(connected){
            tryingToConnect = false
        }
    }

    Column(modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        Row(modifier.fillMaxWidth()){
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.clickable {
                    navigator.pop()
                }
            )
        }



        // Center‐aligned menu card
        Column(modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 300.dp , max = 400.dp)
                ,
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.dropdown_menu),
                    contentScale = ContentScale.FillBounds,
                    contentDescription = "",
                    modifier = modifier.matchParentSize()
                )

                // Scrollable column of devices (or a “no devices” message)
                if (mirrorDevices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No Device Found",
                            color = Color.LightGray,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp)
                    ) {

                        Spacer(modifier.height(40.dp))
                        val itemHeight = 60.dp // Define a consistent height for your items

                        mirrorDevices.forEach { device ->
                            val isThisConnected = connected && (device.id == lastConnectedMirror?.id)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(itemHeight) // <<< Make each item have a fixed height
                                    .padding(vertical = 4.dp) // This padding is now *outside* the 60.dp item, for spacing BETWEEN items
                            ) {
                                Image(
                                    painter = painterResource(Res.drawable.sub_connect),
                                    contentDescription = "Device item background",
                                    contentScale = ContentScale.FillBounds, // Will fill the itemHeight
                                    modifier = Modifier.matchParentSize() // Make the image fill the Box (which has a fixed height)
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxSize() // Fill the Box (which has the fixed height)
                                        .padding(horizontal = 16.dp, vertical = 8.dp), // Adjust padding for content inside the item
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = device.name,
                                        style = MaterialTheme.typography.subtitle1.copy(
                                            fontSize = 14.sp,
                                            color = Color.White
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier
                                            .weight(1f) // Text takes available space, allowing button to be placed first
                                            .padding(end = 8.dp) // Add some space between text and button

                                    )

                                    Box{
                                        Image(
                                            painter = painterResource(if(isThisConnected) Res.drawable.active_connect_button else Res.drawable.connect_button),
                                            contentDescription = "Connect",
                                            contentScale = ContentScale.FillBounds,
                                            modifier = modifier.clickable { // Corrected modifier usage
                                                tryingToConnect = true
                                                viewModel.disconnectMirror()
                                                viewModel.startDiscoveryMirror(device)
                                                viewModel.updateLastMirror(device)
                                            }
                                        )
                                    }

                                    Spacer(modifier.width(10.dp))

                                    Box{
                                        Image(
                                            painter = painterResource(Res.drawable.subtract),
                                            contentDescription = "Remove",
                                            contentScale = ContentScale.FillBounds,
                                            modifier = Modifier.clickable {
                                                viewModel.removeMirror(device)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                if(!connected && tryingToConnect){

                    CircularProgressIndicator(
                        modifier = Modifier.size(50.dp),
                        color = Color.White,
                        strokeWidth = 4.dp,
                        backgroundColor = Color.LightGray,
                        strokeCap = StrokeCap.Round
                    )

                }

            }


        }
    }

}
