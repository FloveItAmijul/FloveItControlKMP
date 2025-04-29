package com.floveit.floveitcontrol


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.floveit.floveitcontrol.providerFunctions.ScrollBar
import com.floveit.floveitcontrol.providerFunctions.TouchPad
import com.floveit.floveitcontrol.providerFunctions.TouchPadTwo
import floveitcontrol.composeapp.generated.resources.Res
import floveitcontrol.composeapp.generated.resources.back
import floveitcontrol.composeapp.generated.resources.home
import floveitcontrol.composeapp.generated.resources.mute
import floveitcontrol.composeapp.generated.resources.next_music
import floveitcontrol.composeapp.generated.resources.play_pause
import floveitcontrol.composeapp.generated.resources.previous_music
import floveitcontrol.composeapp.generated.resources.recent_task
import floveitcontrol.composeapp.generated.resources.volume_down
import floveitcontrol.composeapp.generated.resources.volume_up
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@Composable
fun ClientScreen(modifier: Modifier = Modifier ,client: HidClient) {
    val scope = rememberCoroutineScope()
    val connected by client.isConnected.collectAsState()
    var text by remember { mutableStateOf("") }
    var mute by remember { mutableStateOf(false) }
    var brightness by remember { mutableStateOf(0f) }
    var power by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp)) {
        val width = maxWidth
        val height = maxHeight

        Column(
            modifier.fillMaxHeight(),
        ) {
            if (connected) {
                Row(modifier.fillMaxWidth()) {
                    TextField(
                        value = text,
                        onValueChange = { text = it},
                        shape = RoundedCornerShape(50),
                        leadingIcon = {
                            if(text.isNotEmpty()){
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Erase",
                                    tint = Color.White,
                                    modifier = Modifier.clickable {
                                        text = ""
                                    }.padding(start = 15.dp).size(22.dp)
                                )
                            }

                        },
                        //maxLines =  1 ,
                        singleLine = true,

                        trailingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send Icon",
                                tint = Color.White,
                                modifier = Modifier.clickable {
                                    scope.launch {
                                        client.send("Keyboard$text")
                                    }
                                }.padding(end = 15.dp).size(22.dp)
                            )
                        },
                        colors = TextFieldDefaults.textFieldColors(
                            cursorColor = Color.White,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            backgroundColor = Color(0xFF2C2C55),
                            textColor = Color.White,
                            placeholderColor = Color.White,
                            disabledIndicatorColor = Color.White,
                            errorIndicatorColor = Color.White,
                            disabledTrailingIconColor = Color.Gray,
                            disabledLeadingIconColor = Color.Gray,
                            disabledPlaceholderColor = Color.White,
                        ),
                        placeholder = {
                            Text("Type Here" , color = Color.White)
                        },
                        modifier = modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = modifier.height(20.dp))
                Row(modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Column(modifier.width(width * 0.8f).height(height * 0.54f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TouchPadTwo(
                            modifier = modifier,
                            onSingleTap = {
                                scope.launch {
                                    client.send("MouseLeft")
                                }
                            },
                            onDoubleTap = {

                            },
                            onTwoFingerTap = {
                                scope.launch {
                                    client.send("MouseRight")
                                }
                            },
                            onSingleDrag = {
                                scope.launch {
                                    client.send("MouseMove${it.x},${it.y}")
                                }
                            },
                            onTwoFingerDrag = {value ->
                                println("Two Finger Drag ${value.x} and ${value.y}")
                                scope.launch {
                                    println("Swipe UP")
                                    if(value.y > 0){
                                        client.send("MouseSwipeDown")
                                    }else {
                                        client.send("MouseSwipeUp")
                                    }

                                }

                            },
                            onFingerRelease = {value , position ->
                                scope.launch {
                                    client.send("MouseRelease")
                                }
                            },
                            onSingleLongPress = {

                            },

                        )
//                        TouchPad (
//                            modifier = modifier ,
//                            onPress = {
//                              scope.launch {
//                                  client.send("MouseLeft")
//                              }
//                            },
//                            onRelease = {
//                                println("on Release")
//                                scope.launch {
//                                    client.send("MouseRelease")
//                                }
//                            },
//                            onLongPress = {
//                                scope.launch {
//                                    client.send("MouseRight")
//                                }
//                            },
//                            onDrag = {x , y ->
//                                scope.launch {
//                                    client.send("MouseMove$x,$y")
//                                }
//
//                            },
//                            onSwipeUp = {
//                                scope.launch {
//                                    println("Swipe UP")
//                                    client.send("MouseSwipeUp")
//                                }
//                            },
//                            onSwipeDown = {
//                                scope.launch {
//                                    println("Swipe Down")
//                                    client.send("MouseSwipeDown")
//                                }
//                            },
//                            onSwipeLeft = {
//                                scope.launch {
//                                    client.send("MouseSwipeLeft")
//                                }
//                            },
//                            onSwipeRight = {
//                                scope.launch {
//                                    client.send("MouseSwipeRight")
//                                }
//                            },
//
//                        )

                    }

                    Column(modifier.height(height * 0.54f).fillMaxWidth()){
                        ScrollBar(
                            modifier = modifier ,
                            onScrollUp = {
                                 scope.launch {
                                     client.send("MouseScrollUp")
                                 }
                             },
                            onScrollDown = {
                                scope.launch {
                                    client.send("MouseScrollDown")
                                }
                             },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .height(height * 0.08f)
                        .background(Color.Transparent),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    Box(
                        Modifier
                            .clickable(
                                onClick = {
                                    scope.launch {
                                        client.send("MediaPrevious")
                                    }
                                }
                            )
                    ) {
                        Image(
                            painter = painterResource( Res.drawable.previous_music),
                            contentDescription = "Previous",
                            contentScale = ContentScale.FillBounds,

                            )
                    }

                    Box(
                        Modifier.clickable(
                            onClick = {
                                scope.launch {
                                    client.send("MediaPlay")
                                }
                            }
                        )
                    ) {
                        Image(
                            painter = painterResource( Res.drawable.play_pause),
                            contentDescription = "Play/Pause",
                            contentScale = ContentScale.FillBounds,
                        )

                    }

                    Box(
                        Modifier.clickable(
                            onClick = {
                                scope.launch {
                                    client.send("MediaNext")
                                }
                            }
                        )
                    ) {
                        Image(
                            painter = painterResource( Res.drawable.next_music),
                            contentDescription = "Next",
                            contentScale = ContentScale.FillBounds,
                        )
                    }

                }

                Spacer(Modifier.height(12.dp))
                // Volume Function
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .height(height * 0.1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    Box(
                        Modifier
                            .fillMaxHeight(1f)
                            .clickable(
                                onClick = {
                                    scope.launch {
                                        client.send("MediaVolumeDown")
                                    }
                                }
                            )
                    ) {
                        Image(
                            painter = painterResource( Res.drawable.volume_down),
                            contentDescription = "Volume Down",
                            contentScale = ContentScale.FillBounds,

                            )
                    }

                    Box(
                        Modifier.fillMaxHeight(1f)
                            .clickable(
                                onClick = {
                                    scope.launch {
                                        client.send("MediaVolumeUp")
                                    }
                                }
                            )
                    ) {
                        Image(
                            painter = painterResource( Res.drawable.volume_up),
                            contentDescription = "Volume Up",
                            contentScale = ContentScale.FillBounds,
                        )

                    }

                    Box(
                        Modifier.fillMaxHeight(1f)
                            .clickable(
                                onClick = {
                                    mute = !mute
                                    scope.launch {
                                        if(mute){
                                            client.send("MediaMute")
                                        }else {
                                            client.send("MediaUnMute")
                                        }

                                    }
                                }
                            )
                    ) {
                        Image(
                            painter = painterResource( Res.drawable.mute),
                            contentDescription = "Mute",
                            contentScale = ContentScale.FillBounds,
                        )
                    }


                }

                Spacer(modifier = Modifier.height(12.dp))
                // Navigation's Function
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .height(height * 0.08f)
                        .background(
                            color = Color(0xFF1C1C36) , shape = RoundedCornerShape(30)
                        )
                        .clip(shape = RoundedCornerShape(30))
                        //.align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {

                    Box(
                        Modifier
                            .clickable(
                                onClick = {
                                    scope.launch {
                                        client.send("NavigationRecent")
                                    }
                                }
                            )
                    ){
                        Image(
                            painter = painterResource( Res.drawable.recent_task),
                            contentDescription = "Recent Task",
                            contentScale = ContentScale.FillBounds,

                            )
                    }

                    Box(
                        Modifier.clickable(
                            onClick = {
                                scope.launch {
                                    client.send("NavigationHome")
                                }
                            }
                        )
                    ){
                        Image(
                            painter = painterResource( Res.drawable.home),
                            contentDescription = "Home navigation",
                            contentScale = ContentScale.FillBounds,
                        )

                    }

                    Box(
                        Modifier.clickable(
                            onClick = {
                                scope.launch {
                                    client.send("NavigationBack")
                                }
                            }
                        )
                    ){
                        Image(
                            painter = painterResource(Res.drawable.back),
                            contentDescription = "Back navigation",
                            contentScale = ContentScale.FillBounds,
                        )
                    }


                }

                Row(modifier = modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Slider(
                        value = brightness ,
                        onValueChange = {newValue->
                            scope.launch {
                                client.send("Brightness$newValue")
                            }
                            brightness = newValue
                        },
                        valueRange = 0f..1f,
                        modifier = modifier.width(width * 0.7f)
                    )
                    Button(
                      onClick = {
                          power = !power
                          scope.launch {
                              if (!power) {
                                  client.send("PowerOn")
                              } else {
                                  client.send("PowerOff")
                              }
                          }

                      }
                    ) {
                        Text("Power")
                    }
                }


            } else {
                Button(onClick = { scope.launch { client.discover() } }) {
                    Text("Connect")
                }
            }


        }

    }

}
