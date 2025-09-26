package com.floveit.floveitcontrol.navigations.screens




import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import com.floveit.floveitcontrol.di.LocalFLoveItViewModel
import com.floveit.floveitcontrol.settings.mirrors.SelectMirror

object MirrorsScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = LocalFLoveItViewModel.current
        SelectMirror(modifier = Modifier, viewModel = viewModel)
    }
}
