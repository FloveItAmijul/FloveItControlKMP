package com.floveit.floveitcontrol.wifihid.helperfunction

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.BrowseGallery
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.sharp.Attachment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.floveit.floveitcontrol.filetransfer.PickedFile
import com.floveit.floveitcontrol.platformSpecific.LaunchFilePicker
import com.floveit.floveitcontrol.platformSpecific.LaunchPhotoPicker
import com.floveit.floveitcontrol.platformSpecific.LaunchVideoPicker
import floveitcontrol.composeapp.generated.resources.Res
import floveitcontrol.composeapp.generated.resources.input
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt

@Composable
fun InputRow(
    modifier: Modifier = Modifier,
    offsetX: State<Float>,
    onShakeRequest: () -> Unit,                 // screen triggers your shake coroutine
    onHaptic: (HapticFeedbackType) -> Unit,     // screen decides haptic
    onSendText: (String) -> Unit,               // screen sends text
    onSendFile: (PickedFile, (Boolean) -> Unit) -> Unit, // screen sends file + returns result


) {
    val focusManager = LocalFocusManager.current
    var text by remember { mutableStateOf("") }
    var pickedFile by remember { mutableStateOf<PickedFile?>(null) }
    var status by remember { mutableStateOf<String?>(null) }

    var showAttachMenu by remember { mutableStateOf(false) }

    LaunchedEffect(status) {
        if(status != null){
            delay(1000L)
            status = null
        }
    }

    LaunchedEffect(pickedFile){
        if(pickedFile != null) {
            text = ""
            focusManager.clearFocus(force = true)
        }
    }
    val launchPhoto = LaunchPhotoPicker { pf -> pickedFile = pf }
    val launchVideo = LaunchVideoPicker { pf -> pickedFile = pf }
    val launchFile = LaunchFilePicker { pf -> pickedFile = pf }


    Row(Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth()) {

            Image(
                painter = painterResource(Res.drawable.input),
                contentDescription = "Input",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.matchParentSize()
            )

            TextField(
                value = if(status?.isNotEmpty() == true) status!! else text,
                onValueChange = {
                    if(pickedFile == null){
                        text = it
                    }

                },
                shape = RoundedCornerShape(30),
                singleLine = true,

                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(25.dp)
                            .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                if(pickedFile != null){
                                    pickedFile = null
                                    onShakeRequest()
                                } else if (text.isNotEmpty()) {
                                    onHaptic(HapticFeedbackType.LongPress)
                                    text = ""
                                    onShakeRequest()
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Erase",
                            tint = if (text.isNotEmpty() || (pickedFile != null )) Color.White  else Color(0xFF696183),
                            modifier = Modifier.matchParentSize()
                        )
                    }
                },

                trailingIcon = {
                    Row(Modifier.padding(end = 15.dp)) {
                        if(pickedFile == null && text.isEmpty()){
                            // choose file
                            Icon(
                                imageVector = Icons.Sharp.Attachment,
                                contentDescription = "Choose File",
                                tint = if (pickedFile == null) Color.White else Color.White,
                                modifier = Modifier
                                    .clickable { showAttachMenu = true }
                                    .size(25.dp)
                            )

                            PremiumAttachMenu(
                                expanded = showAttachMenu,
                                onDismiss = { showAttachMenu = false },
                                onPhoto = { launchPhoto() },
                                onVideo = { launchVideo() },
                                onFile  = { launchFile() }
                            )


                        }

                        if(pickedFile != null || text.isNotEmpty()){
                            // send
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier
                                    .clickable {
                                        onHaptic(HapticFeedbackType.LongPress)

                                        val file = pickedFile
                                        if (file != null) {
                                            onSendFile(file) { ok ->
                                                status = if (ok) "File sent ✅" else "File failed to sent ❌"
                                                if (ok) pickedFile = null
                                            }
                                        } else {
                                            onSendText(text)
                                            status = "Text sent ✅"
                                            text = ""
                                        }
                                    }
                                    .size(25.dp)
                            )
                        }


                    }
                },

                placeholder = {
                    Text(
                        text = pickedFile?.displayName ?: "Input Text / Send File",
                        color = Color.White,
                        maxLines = 1
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

                modifier = modifier.fillMaxWidth()
            )
        }
    }

    //status?.let { Text(it, color = Color.White) }
}



