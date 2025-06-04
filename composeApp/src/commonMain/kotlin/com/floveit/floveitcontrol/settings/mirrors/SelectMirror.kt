package com.floveit.floveitcontrol.settings.mirrors

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.floveit.floveitcontrol.viewmodel.FLoveItControlViewModel
import floveitcontrol.composeapp.generated.resources.Res
import floveitcontrol.composeapp.generated.resources.dropdown_menu
import org.jetbrains.compose.resources.painterResource

@Composable
fun SelectMirror(modifier: Modifier = Modifier , viewModel: FLoveItControlViewModel) {

    val mirrorDevices by viewModel.connectedMirrors.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    val navigator = LocalNavigator.currentOrThrow



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
                        Row(modifier.fillMaxWidth().padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Device List", color = Color.White, fontSize = 18.sp)
                            Text("Status", color = Color.White, fontSize = 18.sp)

                        }

                        Spacer(modifier.height(15.dp))
                        mirrorDevices.forEach { device ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),

                                ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
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
                                        modifier = modifier.clickable {
                                            viewModel.disconnectMirror()
                                            expanded = false
                                            viewModel.startDiscoveryMirror(device)
                                            viewModel.updateLastMirror(device)
                                        }
                                    )

                                    Button (
                                        onClick = {
                                            viewModel.removeMirror(device)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            backgroundColor = Color(0xFF251B3A),
                                            contentColor = Color.White
                                        )

                                    ){
                                        Text("Remove")
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
