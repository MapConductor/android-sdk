package com.mapconductor.example.pages.marker.postoffice

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun PostOfficeInfoView(info: PostOffice) {
    val darkTheme: Boolean = isSystemInDarkTheme()
    if (!darkTheme) Color.Black else Color.White

    Column(
        modifier = Modifier.wrapContentSize(),
    ) {
        val name = info.name
        val address = info.address
        Text(name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text(address, fontSize = 13.sp)
    }
}
