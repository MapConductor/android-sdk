package com.mapconductor.example.pages.marker.postofficecluster

import androidx.compose.foundation.clickable
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
fun PostOfficeInfoView(
    info: PostOffice,
    onClick: ((PostOffice) -> Unit)? = null,
) {
    val darkTheme: Boolean = isSystemInDarkTheme()
    if (!darkTheme) Color.Black else Color.White

    Column(
        modifier =
            Modifier
                .wrapContentSize()
                .clickable(true) {
                    onClick?.invoke(info)
                },
    ) {
        val name = info.name
        val address = info.address
        Text(name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text(address, fontSize = 13.sp)
    }
}
