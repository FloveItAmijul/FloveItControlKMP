package com.floveit.floveitcontrol.navigations.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import com.floveit.floveitcontrol.settings.mirrors.SelectMirror
import com.floveit.floveitcontrol.viewmodel.FLoveItControlViewModel

data class MirrorsScreen(private val modifier: Modifier , private val viewModel: FLoveItControlViewModel) : Screen {

    @Composable
    override fun Content() {
        SelectMirror(modifier = modifier ,viewModel = viewModel)
    }
}
