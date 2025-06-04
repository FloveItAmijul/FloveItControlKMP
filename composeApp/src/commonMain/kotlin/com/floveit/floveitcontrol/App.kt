package com.floveit.floveitcontrol

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.floveit.floveitcontrol.lightControl.LightRepository
import com.floveit.floveitcontrol.meterialui.BackgroundColor
import com.floveit.floveitcontrol.navigations.screens.FLoveItNavController
import com.floveit.floveitcontrol.platformSpecific.SetTransparentSystemBars
import com.floveit.floveitcontrol.viewmodel.FLoveItControlViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(
    client: HidClient = provideHidClient()
) {
    val lightRepository = remember { LightRepository(client) }
    val viewModel = remember { FLoveItControlViewModel(lightRepository) }
    SetTransparentSystemBars()
    MaterialTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            BackgroundColor {
                FLoveItNavController(
                    modifier = Modifier.padding(paddingValues).statusBarsPadding(), // navigation bar padding
                    viewModel = viewModel
                )
            }
        }
    }
}

