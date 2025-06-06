package com.mapconductor.example.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.os.Bundle

@Composable
fun StoreCard(
    info: Bundle,
    onClick: () -> Unit = {},
) {
    val darkTheme: Boolean = isSystemInDarkTheme()
    if (!darkTheme) Color.Black else Color.White

    Column(
        modifier = Modifier.wrapContentSize(),
    ) {
        val name = info.getString("name", "Starbucks")
        val address = info.getString("address", "address")
        val instore = info.getBoolean("instore", false)
        val driveThrough = info.getBoolean("drive_through", false)
        Text(name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text(address, fontSize = 13.sp)

        if (instore || driveThrough) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween, // like justify-content
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (instore) {
                    Text("● In store eating")
                }

                if (driveThrough) {
                    Text("● Drive Through")
                }
            }
        }
        RoundedLabel(
            text = "Get Directions",
            onClick = onClick,
        )
    }
}

@Composable
fun RoundedLabel(
    text: String,
    onClick: () -> Unit = {},
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5)),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) {
        // 黒い丸アイコン
        Box(
            modifier =
                Modifier
                    .size(12.dp)
                    .background(Color.Black, shape = CircleShape),
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
        )
    }
}
