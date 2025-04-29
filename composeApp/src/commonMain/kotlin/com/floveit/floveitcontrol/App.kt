package com.floveit.floveitcontrol

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(client: HidClient = provideHidClient()) {

    MaterialTheme {
        ClientScreen(modifier = Modifier, client = client)
    }
}