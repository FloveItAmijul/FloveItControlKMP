package com.floveit.floveitcontrol.navigations.custombottombar

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import floveitcontrol.composeapp.generated.resources.*


@Composable
fun CustomBottomBar(
    modifier: Modifier = Modifier,
    selectedScreen: Int,
    onSettingsClick: () -> Unit,
    onLightClick: () -> Unit,
    onMouseClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            //.height(60.dp)
            .padding(5.dp ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ){
            // Right button: Mouse (index 2)
            PillButton(
                selected = selectedScreen == 2,
                resource = Res.drawable.mouse,
                selectedResource = Res.drawable.mouse_active,
                onClick = onMouseClick
            )
        }
        // Light Icon
        Box(modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ){
            // Center button: Light (index 1)
            PillButton(
                selected = selectedScreen == 1,
                resource = Res.drawable.lightnav,
                selectedResource = Res.drawable.light_active,
                onClick = onLightClick
            )
        }

        // setting Icon
        Box(modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ){
            // Left button: Settings (index 0)
            PillButton(
                selected = selectedScreen == 0,
                resource = Res.drawable.setting,
                selectedResource = Res.drawable.setting_active,
                onClick = onSettingsClick
            )
        }

    }
}





