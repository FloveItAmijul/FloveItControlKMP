package com.floveit.floveitcontrol

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.floveit.floveitcontrol.lightControl.LightRepository
import com.floveit.floveitcontrol.meterialui.BackgroundColor
import com.floveit.floveitcontrol.navigations.FLoveItNavigation
import com.floveit.floveitcontrol.platformSpecific.SetTransparentSystemBars
import com.floveit.floveitcontrol.viewmodel.FloveItControlViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(
    client: HidClient = provideHidClient()
) {
    val lightRepository = remember { LightRepository(client) }
    val viewModel = remember { FloveItControlViewModel(lightRepository) }
    SetTransparentSystemBars()
    MaterialTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            LaunchedEffect(Unit) {
                viewModel.discover()
            }
            BackgroundColor {
                FLoveItNavigation(
                    modifier = Modifier.padding(paddingValues).statusBarsPadding(),//.navigationBarsPadding(),
                    viewModel = viewModel
                )
            }
        }
    }
}

