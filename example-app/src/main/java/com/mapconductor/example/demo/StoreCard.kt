package com.mapconductor.example.demo

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapconductor.example.R
import android.content.res.Configuration
import android.os.Bundle

@Composable
fun StoreCard(
    info: Bundle,
    onClick: () -> Unit = {},
) {
    val darkTheme: Boolean = isSystemInDarkTheme()
    val iconTintColor = if (!darkTheme) Color.Black else Color.White

    @Composable
    fun CallButtonPortrait(modifier: Modifier = Modifier) {
        Button(
            onClick = onClick,
            modifier = modifier,
            shape = CircleShape,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00704A),
                ),
        ) {
            Row {
                Icon(
                    imageVector = Icons.Rounded.Call,
                    contentDescription = null,
                    tint = Color.White,
                )
                Text(
                    text = "Call",
                    color = Color.White,
                )
            }
        }
    }

    @Composable
    fun CallButtonLandScape(modifier: Modifier = Modifier) {
        Button(
            onClick = onClick,
            modifier =
                modifier
                    .heightIn(max = 100.dp)
                    .fillMaxHeight(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00704A)),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Rounded.Call,
                    contentDescription = null,
                    tint = Color.White,
                )
                Text(
                    text = "Call",
                    color = Color.White,
                )
            }
        }
    }

    @Composable
    fun StoreInfo() {
        Column {
            val name = info.getString("name", "Starbucks")
            val address = info.getString("address", "address")
            val instore = info.getBoolean("instore", false)
            val driveThrough = info.getBoolean("drive_through", false)
            Text(name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(address, fontSize = 13.sp)
            if (instore || driveThrough) {
                Row {
                    if (instore) {
                        Icon(
                            painter = painterResource(R.drawable.instore),
                            contentDescription = "In store",
                            modifier = Modifier.size(32.dp),
                            tint = iconTintColor,
                        )
                    }

                    if (driveThrough) {
                        Icon(
                            painter = painterResource(R.drawable.drivethrough),
                            contentDescription = "Drive through",
                            modifier = Modifier.size(32.dp),
                            tint = iconTintColor,
                        )
                    }
                }
            }
        }
    }

    val configuration = LocalConfiguration.current

    @Composable
    fun PortraitLayout(modifier: Modifier = Modifier) {
        Column(modifier = Modifier) {
            StoreInfo()
            CallButtonPortrait(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
            )
        }
    }

    @Composable
    fun LandscapeLayout(modifier: Modifier = Modifier) {
        Box(modifier = modifier) {
            StoreInfo()
            CallButtonLandScape(
                modifier =
                    Modifier
                        .align(alignment = Alignment.CenterEnd)
                        .padding(end = 4.dp),
            )
        }
    }
    val modifier =
        Modifier
            .widthIn(max = 350.dp)
            .wrapContentSize()
    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        LandscapeLayout(modifier = modifier)
    } else {
        PortraitLayout(modifier = modifier)
    }
}
