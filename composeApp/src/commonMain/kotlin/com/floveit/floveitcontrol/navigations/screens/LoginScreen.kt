package com.floveit.floveitcontrol.navigations.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import com.floveit.floveitcontrol.navigations.pages.LoginPage
import com.floveit.floveitcontrol.viewmodel.FLoveItControlViewModel

data class LoginScreen(private val modifier: Modifier, private val viewModel: FLoveItControlViewModel) : Screen {

    @Composable
    override fun Content() {
        LoginPage(modifier = modifier, viewModel = viewModel)
    }


}
