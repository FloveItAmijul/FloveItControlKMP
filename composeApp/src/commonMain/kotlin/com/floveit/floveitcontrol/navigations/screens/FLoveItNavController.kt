package com.floveit.floveitcontrol.navigations.screens

import com.floveit.floveitcontrol.viewmodel.FLoveItControlViewModel

// VoyagerNavHost.kt
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator

@Composable
fun FLoveItNavController(modifier: Modifier , viewModel: FLoveItControlViewModel) {

    val login by viewModel.login.collectAsState()

    val startScreen = if(login){
        MainDashboardScreen(modifier = modifier ,viewModel = viewModel)
    }else{
        LoginScreen(modifier = modifier ,viewModel = viewModel)
    }

    Navigator(startScreen)
}
