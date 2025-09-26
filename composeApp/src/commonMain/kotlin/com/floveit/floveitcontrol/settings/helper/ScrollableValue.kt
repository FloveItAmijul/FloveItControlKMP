package com.floveit.floveitcontrol.settings.helper


import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import floveitcontrol.composeapp.generated.resources.*
import org.jetbrains.compose.resources.painterResource


@Composable
fun ScrollableValue(
    onValueChange: (Int) -> Unit,
    value: Int,
    modifier: Modifier = Modifier
) {
    val range = (-20..20).toList()
    var selectedValue by remember { mutableStateOf(value) }

    Box(
        modifier = modifier
            .width(100.dp),
        contentAlignment = Alignment.Center
    ) {

        Image(
            painter = painterResource(Res.drawable.timer_background),
            contentDescription = "timer",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )

        Row (modifier = Modifier.fillMaxWidth(),){
            LazyColumn(
                modifier = Modifier
                    .padding(10.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(range) { item ->
                    Text(
                        text = item.toString(),
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .clickable {
                                selectedValue = item
                                onValueChange(item)
                            },
                        color = if (item == selectedValue) Color.Magenta else Color.White
                    )
                }
            }
        }




    }
}
