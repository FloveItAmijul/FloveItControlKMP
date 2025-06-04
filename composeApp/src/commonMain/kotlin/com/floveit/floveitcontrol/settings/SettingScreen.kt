package com.floveit.floveitcontrol.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import cafe.adriel.voyager.navigator.*
import com.floveit.floveitcontrol.navigations.screens.LoginScreen
import com.floveit.floveitcontrol.navigations.screens.MirrorsScreen
import com.floveit.floveitcontrol.viewmodel.FLoveItControlViewModel
import floveitcontrol.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource

@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    viewModel: FLoveItControlViewModel
) {
    val navigator = LocalNavigator.currentOrThrow

    val mirrorDevices by viewModel.connectedMirrors.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()

    var expanded by remember { mutableStateOf(false) }
    var showNet by remember { mutableStateOf(false) }

    val settingSize = 90.dp

    // Whenever we reconnect, hide the progress indicator
    LaunchedEffect(isConnected) {
        if (isConnected) {
            showNet = false
        }
    }

    Box(modifier = modifier.fillMaxSize()
        .padding(20.dp)
        .clickable(
            interactionSource = MutableInteractionSource(),
            indication = null
        ) {
            expanded = false
        }
    ) {
        // --------------------------------------
        // MAIN CONTENT: the “Settings” screen
        // --------------------------------------


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp , start = 10.dp , end = 10.dp)
                .clickable(
                    interactionSource = MutableInteractionSource(),
                    indication = null
                ) {
                    expanded = false
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {


            // 1) add new mirror device
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(settingSize), // set a fixed height matching your image’s aspect ratio
                contentAlignment = Alignment.Center
            ){
                Image(
                    painter = painterResource(Res.drawable.add_mirror),
                    contentDescription = "add mirros",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.clickable {
                        viewModel.updateAuthStatus(false)
                        navigator.push(LoginScreen(modifier = modifier, viewModel = viewModel))
                    }
                )
            }
            //  tapping this toggles our custom “dropdown”
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(settingSize), // set a fixed height matching your image’s aspect ratio
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.select_mirror),
                    contentDescription = "select mirror",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            navigator.push(MirrorsScreen(modifier = modifier, viewModel = viewModel))
                        }
                )

                // Place the count at the right side, vertically centered
                Text(
                    text = "${mirrorDevices.size}",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 40.dp) // give a little padding from the right edge
                )

                if (showNet && !isConnected) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.Center),
                        color = Color.Red,
                        strokeWidth = 4.dp,
                        backgroundColor = Color.Gray
                    )
                }
            }



            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(settingSize), // set a fixed height matching your image’s aspect ratio
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.timeout),
                    contentDescription = "screen timeout",
                    contentScale = ContentScale.FillBounds,
                )
                Row(modifier.matchParentSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box{
                        // empty
                    }
                    Box(contentAlignment = Alignment.Center){
                        // Three Box for the three buttons
                        Box(){
                            Image(
                                painter = painterResource(Res.drawable.previous_time),
                                contentDescription = "Previous Time",
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier.clickable {

                                }
                            )
                        }

                        Text("Time")

                        Box(){
                            Image(
                                painter = painterResource(Res.drawable.next_time),
                                contentDescription = "Next Time",
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier.clickable {

                                }
                            )
                        }
                    }

                }


            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(settingSize), // set a fixed height matching your image’s aspect ratio
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.version),
                    contentDescription = "version",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.clickable { /* ... */ }
                )
            }

        }

        // -------------------------------------------------------
        // OVERLAY: Centered, rounded “dropdown” when expanded = true
        // -------------------------------------------------------

    }
}
