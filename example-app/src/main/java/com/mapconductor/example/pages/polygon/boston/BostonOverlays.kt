package com.mapconductor.example.pages.polygon.boston

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/** The land-use colours, two to a row. */
@Composable
fun LandUseLegend(modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        LandUse.entries.chunked(2).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { use ->
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier =
                                Modifier
                                    .size(10.dp)
                                    .background(use.fill, RoundedCornerShape(2.dp))
                                    .border(1.dp, Color(0x80475569), RoundedCornerShape(2.dp)),
                        )
                        Text(use.label, fontSize = 11.sp, modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }
        }
    }
}

private const val MB = 1024.0 * 1024.0

/** Covers the map while a dataset loads, saying which phase it is in. */
@Composable
fun LoadingOverlay(
    phase: LoadPhase,
    modifier: Modifier = Modifier,
    /** What is loading, for the text: "polygons" or "roads". */
    shapes: String = "polygons",
) {
    var fraction: Float? = null
    val text =
        when (phase) {
            is LoadPhase.Read -> {
                val loaded = String.format(Locale.US, "%.1f", phase.loaded / MB)
                val total = phase.total?.takeIf { it > 0 }
                if (total != null) {
                    fraction = (phase.loaded.toFloat() / total).coerceIn(0f, 1f)
                    "Loading data… $loaded / ${String.format(Locale.US, "%.1f", total / MB)} MB"
                } else {
                    "Loading data… $loaded MB"
                }
            }
            LoadPhase.Decode -> "Reading the $shapes…"
            LoadPhase.Build -> "Preparing the $shapes…"
            LoadPhase.Draw -> "Drawing the map…"
        }
    Box(
        modifier = modifier.fillMaxSize().background(Color(0x590F172A)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier =
                Modifier
                    .width(260.dp)
                    .background(Color.White, RoundedCornerShape(10.dp))
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp, color = Color(0xFF2563EB))
            Text(text, fontSize = 14.sp, color = Color(0xFF0F172A))
            fraction?.let {
                LinearProgressIndicator(progress = {
                    it
                }, modifier = Modifier.fillMaxWidth(), color = Color(0xFF2563EB))
            }
        }
    }
}

/** What a tapped polygon carries, shown in an InfoBubble at the tap. */
@Composable
fun PolygonInfo(building: Building) {
    val rows = mutableListOf<Pair<String, String>>()
    val use = building.use
    if (use != null) {
        rows += "Land use" to use.label
    } else {
        building.name?.let { rows += "Name" to it }
        rows += "Height" to (building.height?.let { "$it m" } ?: "unknown")
    }
    rows += "Area" to formatArea(areaSquareMetres(building))
    val vertices = building.rings.sumOf { it.size / 2 }
    rows += "Vertices" to (if (building.rings.size > 1) "$vertices (holes ${building.rings.size - 1})" else "$vertices")
    Column {
        rows.forEach { (key, value) ->
            Row {
                Text(
                    key,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF475569),
                    modifier = Modifier.width(84.dp),
                )
                Text(value, fontSize = 12.sp, color = Color(0xFF0F172A))
            }
        }
    }
}

/** "1,234 m²", or hectares from 10,000 m² up. */
fun formatArea(squareMetres: Double): String =
    if (squareMetres >= 10_000) {
        String.format(Locale.US, "%.2f ha", squareMetres / 10_000)
    } else {
        String.format(Locale.US, "%,.0f m²", squareMetres)
    }
