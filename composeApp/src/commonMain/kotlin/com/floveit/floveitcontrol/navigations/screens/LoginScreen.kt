package com.floveit.floveitcontrol.navigations.screens




import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import com.floveit.floveitcontrol.di.LocalFLoveItViewModel
import com.floveit.floveitcontrol.navigations.pages.LoginPage

object LoginScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = LocalFLoveItViewModel.current
        LoginPage(modifier = Modifier, viewModel = viewModel)
    }
}
