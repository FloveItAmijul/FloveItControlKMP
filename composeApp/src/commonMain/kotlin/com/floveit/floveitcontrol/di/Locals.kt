package com.floveit.floveitcontrol.di


import androidx.compose.runtime.staticCompositionLocalOf
import com.floveit.floveitcontrol.viewmodel.FLoveItControlViewModel

val LocalFLoveItViewModel = staticCompositionLocalOf<FLoveItControlViewModel> {
    error("FLoveItControlViewModel not provided")
}
