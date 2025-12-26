package com.floveit.floveitcontrol.wifihid.helperfunction

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun PremiumAttachMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onPhoto: () -> Unit,
    onVideo: () -> Unit,
    onFile: () -> Unit,
) {
    MaterialTheme(colors = MaterialTheme.colors.copy(surface = Color.Transparent)) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss,
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF0E0E10)) // premium black
                .widthIn(min = 220.dp)         // keeps it compact
                .padding(vertical = 6.dp)
        )
        {
            PremiumMenuItem(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.PhotoLibrary,
                        contentDescription = "Photo",
                        tint = Color(0xFF4DA3FF)
                    )
                },
                title = "Photo",
                subtitle = "Choose from gallery",
                onClick = {
                    onDismiss()
                    onPhoto()
                }
            )

            Divider(color = Color(0xFF1D1D22), thickness = 1.dp)

            PremiumMenuItem(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.VideoLibrary,
                        contentDescription = "Video",
                        tint = Color(0xFFFF5DA2)
                    )
                },
                title = "Video",
                subtitle = "Pick a video clip",
                onClick = {
                    onDismiss()
                    onVideo()
                }
            )

            Divider(color = Color(0xFF1D1D22), thickness = 1.dp)

            PremiumMenuItem(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.FileCopy,
                        contentDescription = "File",
                        tint = Color(0xFFE7E7EA)
                    )
                },
                title = "File",
                subtitle = "PDF, docs, any file",
                onClick = {
                    onDismiss()
                    onFile()
                }
            )
        }
    }


}

@Composable
private fun PremiumMenuItem(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    DropdownMenuItem(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF17171C)),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.body1,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = Color(0xFFB6B6C2),
                    style = MaterialTheme.typography.caption
                )
            }
        }
    }
}
