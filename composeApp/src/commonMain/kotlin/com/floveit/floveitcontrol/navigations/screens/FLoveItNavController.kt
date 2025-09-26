package com.floveit.floveitcontrol.navigations.screens

import com.floveit.floveitcontrol.viewmodel.FLoveItControlViewModel

// VoyagerNavHost.kt
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import com.floveit.floveitcontrol.di.LocalFLoveItViewModel

@Composable
fun FLoveItNavController(modifier: Modifier ) {

    val viewModel = LocalFLoveItViewModel.current

    val login by viewModel.login.collectAsState()

    val startScreen = if (login) MainDashboardScreen else LoginScreen
    Navigator(startScreen)
}
