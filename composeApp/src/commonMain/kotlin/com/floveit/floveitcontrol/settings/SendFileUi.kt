package com.floveit.floveitcontrol.settings


import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.floveit.floveitcontrol.filetransfer.PickedFile
import com.floveit.floveitcontrol.platformSpecific.launchFilePicker
import com.floveit.floveitcontrol.viewmodel.FLoveItControlViewModel
import kotlinx.coroutines.launch

@Composable
fun SendFileUi(
    viewModel: FLoveItControlViewModel,
    modifier: Modifier
) {
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf<String?>(null) }

    // ⬇️ platform-provided launcher remembered as a lambda we can call from onClick
    val launchPicker = launchFilePicker { pf: PickedFile ->
        scope.launch {
            viewModel.sendPickedFile(pf) { ok ->
                status = if (ok) "Sent ✅" else "Failed ❌"
            }
        }
    }

    Row(modifier = modifier.fillMaxWidth()) {
        Button(
            onClick = { launchPicker() },
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color(0xFF4D2D44),
                contentColor = Color.White
            )
        ) { Text("Choose & Send") }
    }

    status?.let { Text(it, color = Color.White) }
}
