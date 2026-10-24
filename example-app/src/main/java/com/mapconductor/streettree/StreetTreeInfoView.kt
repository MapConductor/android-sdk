package com.mapconductor.streettree

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** What the survey recorded about one tree. */
@Composable
fun StreetTreeInfoView(
    tree: StreetTree,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(tree.species, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        // The survey leaves these blank often enough that showing "0 m" would
        // read as a measurement rather than a gap.
        if (tree.heightM > 0f) Field("樹高", "%.1f m".format(tree.heightM))
        if (tree.girthCm > 0) Field("幹周", "${tree.girthCm} cm")
        if (tree.ward.isNotBlank()) Field("行政区", tree.ward)
        if (tree.roadName.isNotBlank()) Field("路線", tree.roadName)
    }
}

@Composable
private fun Field(
    label: String,
    value: String,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, fontSize = 13.sp, modifier = Modifier.width(46.dp))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}
