package com.floveit.floveitcontrol.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.floveit.floveitcontrol.viewmodel.FloveItControlViewModel
import floveitcontrol.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource

@Composable
fun SettingScreen(modifier: Modifier = Modifier , viewModel: FloveItControlViewModel){

    Column (modifier.fillMaxSize().padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ){
        Box{

            Image(
                painter = painterResource(resource = Res.drawable.settingbutton),
                contentDescription = "Setting Button",
                contentScale = ContentScale.FillBounds,
                modifier = modifier.clickable {  }
            )
        }

        Box{

            Image(
                painter = painterResource(resource = Res.drawable.settingbutton1),
                contentDescription = "Setting Button",
                contentScale = ContentScale.FillBounds,
                modifier = modifier.clickable {  }
            )
        }


        Box{

            Image(
                painter = painterResource(resource = Res.drawable.settingbutton2),
                contentDescription = "Setting Button",
                contentScale = ContentScale.FillBounds,
                modifier = modifier.clickable {  }
            )
        }


        Box{

            Image(
                painter = painterResource(resource = Res.drawable.settingbutton3),
                contentDescription = "Setting Button",
                contentScale = ContentScale.FillBounds,
                modifier = modifier.clickable {  }
            )
        }


        Box{

            Image(
                painter = painterResource(resource = Res.drawable.settingbutton4),
                contentDescription = "Setting Button",
                contentScale = ContentScale.FillBounds,
                modifier = modifier.clickable {
                    viewModel.logout()
                }
            )
        }

        Box{

            Image(
                painter = painterResource(resource = Res.drawable.settingbutton5),
                contentDescription = "Setting Button",
                contentScale = ContentScale.FillBounds,
                modifier = modifier.clickable {  }
            )
        }



    }


}