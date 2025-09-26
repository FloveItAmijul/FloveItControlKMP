package com.floveit.floveitcontrol.navigations.screens

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import com.floveit.floveitcontrol.di.LocalFLoveItViewModel
import com.floveit.floveitcontrol.navigations.pages.FLoveItMainPage
import com.floveit.floveitcontrol.viewmodel.FLoveItControlViewModel

//data class MainDashboardScreen(private val modifier: Modifier , private val viewModel: FLoveItControlViewModel) : Screen {
//    @Composable
//    override fun Content() {
//        FLoveItMainPage(modifier = modifier , viewModel = viewModel)
//    }
//}

object MainDashboardScreen : Screen {

    @Composable
    override fun Content() {
        val viewModel = LocalFLoveItViewModel.current
        FLoveItMainPage(modifier = Modifier.statusBarsPadding(), viewModel = viewModel)
    }
}