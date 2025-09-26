package com.floveit.floveitcontrol

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.floveit.floveitcontrol.di.LocalFLoveItViewModel
import com.floveit.floveitcontrol.settings.SettingsRepository
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
    val settingsRepository = remember { SettingsRepository() }
    val lightRepository = remember { LightRepository(client) }
    val viewModel = remember { FLoveItControlViewModel(lightRepository, settingsRepository) }
    SetTransparentSystemBars()
    MaterialTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            BackgroundColor {
                CompositionLocalProvider(LocalFLoveItViewModel provides viewModel ) {
                    FLoveItNavController(
                        modifier = Modifier
                            .padding(paddingValues)
                            .statusBarsPadding()
                    )
                }

            }
        }
    }
}

