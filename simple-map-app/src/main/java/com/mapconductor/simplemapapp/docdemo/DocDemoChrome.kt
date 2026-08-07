package com.mapconductor.simplemapapp.docdemo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * The furniture the six documentation recordings share.
 *
 * A screen recording is watched at a fraction of the phone's real size, so the
 * readout that carries each video's point is set larger and higher-contrast
 * than an app would normally use. Nothing here is part of MapConductor — it is
 * the studio lighting, kept out of the demo bodies so those stay close to the
 * snippet printed on the page.
 */

/** Deep enough to stay legible over any base map, at any zoom. */
private val PANEL = Color(0xE6101418)

/** A caption strip pinned to the top of the frame, naming what is on screen. */
@Composable
fun DemoCaption(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(PANEL)
                .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(title, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
        subtitle?.let {
            Text(it, color = Color(0xFF9FB3C8), fontSize = 14.sp)
        }
    }
}

/** A fixed-width readout: the numbers a video is meant to show changing. */
@Composable
fun DemoReadout(
    lines: List<String>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .background(PANEL, RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        lines.forEach {
            Text(
                it,
                color = Color(0xFFE6EDF3),
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
            )
        }
    }
}

/**
 * The control row along the bottom.
 *
 * Buttons are tall and full-bleed on purpose: the recordings are driven by
 * `adb shell input tap`, so a target that is easy to hit blind is a target that
 * does not need a retake.
 */
@Composable
fun DemoControls(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(PANEL)
                .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        content()
    }
}

@Composable
fun RowScopeDemoButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(10.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2D7FF9),
                contentColor = Color.White,
            ),
    ) {
        Text(label, fontSize = 17.sp, fontWeight = FontWeight.Medium)
    }
}

/** A label sitting directly on the map, for the two side-by-side demos. */
@Composable
fun MapBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .background(PANEL, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp),
    ) {
        Text(text, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}
