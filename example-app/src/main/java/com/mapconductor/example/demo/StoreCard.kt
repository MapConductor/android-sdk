package com.mapconductor.example.demo

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mapconductor.example.R

@Preview(widthDp = 600, heightDp = 300, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(widthDp = 400, heightDp = 600)
@Composable
fun StoreCard(
    onClick: () -> Unit = {},
) {

    @Composable
    fun CallButtonPortrait(modifier: Modifier = Modifier) {
        Button(
            onClick = onClick,
            modifier = modifier,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF00704A),
            )
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
            modifier = modifier
                .heightIn(max = 100.dp)
                .fillMaxHeight(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00704A))
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
    fun StoreInfo(modifier: Modifier = Modifier) {
        Column(modifier = modifier) {
            Text("Pearlridge Center", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            TableRow("In store", "Available")
            TableRow("Drive", "---")
            TableRow("Only Reserved", "Yes")
        }
    }

    @Composable
    fun BoxScope.BackgroundImage() {
        Image(
            painter = painterResource(id = R.drawable.human),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
            alpha = 0.1f
        )
    }

    val configuration = LocalConfiguration.current

    @Composable
    fun PortraitLayout(modifier: Modifier = Modifier) {
        Box(
            modifier = modifier
                .wrapContentSize()
        ) {
            BackgroundImage()
            Column(modifier = Modifier) {
                StoreInfo(modifier = Modifier)
                CallButtonPortrait(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                )
            }
        }
    }

    @Composable
    fun LandscapeLayout(modifier: Modifier = Modifier) {
        Box(modifier = modifier) {
            BackgroundImage()
            StoreInfo(modifier = Modifier)
            CallButtonLandScape(
                modifier = Modifier
                    .align(alignment = Alignment.CenterEnd)
                    .padding(end = 4.dp)
            )
        }
    }
    val modifier = Modifier
        .widthIn(max = 350.dp)
        .wrapContentSize()
    if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        LandscapeLayout(modifier = modifier)
    } else {
        PortraitLayout(modifier = modifier)
    }
}