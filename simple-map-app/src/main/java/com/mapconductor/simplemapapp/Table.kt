package com.mapconductor.simplemapapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
private fun RowScope.TableCell(
    text: String,
    weight: Float,
) {
    Text(
        text = text,
        Modifier
            .border(1.dp, Color.Black)
            .weight(weight)
            .padding(8.dp),
    )
}

@Composable
fun TableView(
    tableData: Map<String, Any?>,
    column1Weight: Float,
    column2Weight: Float,
) {
    Column(
        modifier =
            Modifier
                .width(400.dp)
                .heightIn(max = 300.dp)
                .padding(0.dp)
                .verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier.background(Color.Gray),
        ) {
            TableCell(text = "Column 1", weight = column1Weight)
            TableCell(text = "Column 2", weight = column2Weight)
        }

        tableData.map {
            val (id, text) = it
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                TableCell(text = id.toString(), weight = column1Weight)
                TableCell(text = text.toString(), weight = column2Weight)
            }
        }
    }
}
