package com.mapconductor.example.pages.marker.icons

import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import com.mapconductor.core.ResourceProvider
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.info.InfoBubble
import com.mapconductor.core.map.MapViewState
import com.mapconductor.core.marker.DefaultIcon
import com.mapconductor.core.marker.DrawableDefaultIcon
import com.mapconductor.core.marker.ImageIcon
import com.mapconductor.core.marker.Marker
import com.mapconductor.core.marker.MarkerState
import com.mapconductor.example.MapViewContainer
import com.mapconductor.example.R
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable

@Composable
fun MarkerBasicMapComponent(
    mapViewState: MapViewState<*>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var selected by remember { mutableStateOf<MarkerState?>(null) }
    val darkTheme: Boolean = isSystemInDarkTheme()
    val bubbleColor = if (darkTheme) Color.Black else Color.White

    MapViewContainer(
        modifier = modifier,
        state = mapViewState,
        onMapClick = { selected = null },
        onMarkerClick = { selected = it },
    ) {
        Marker(
            position = GeoPoint.fromLatLong(0.018, 0.004),
            icon =
                DefaultIcon(
                    scale = 0.7f,
                    label = "0.7",
                    debug = true,
                ),
            extra =
                """
                DefaultIcon(
                    scale = 0.7f,
                    label = "0.7",
                    debug = true,
                ),
                """.trimIndent(),
        )

        Marker(
            position = GeoPoint.fromLatLong(0.018, 0.006),
            icon =
                DefaultIcon(
                    scale = 1.0f,
                    label = "1.0",
                    debug = true,
                ),
            extra =
                """
                DefaultIcon(
                    scale = 1.0f,
                    label = "1.0",
                    debug = true,
                ),
                """.trimIndent(),
        )
        Marker(
            position = GeoPoint.fromLatLong(0.018, 0.009),
            icon =
                DefaultIcon(
                    scale = 1.4f,
                    label = "1.4",
                    debug = true,
                ),
            extra =
                """
                DefaultIcon(
                    scale = 1.4f,
                    label = "1.4",
                    debug = true,
                ),
                """.trimIndent(),
        )

        Marker(
            position = GeoPoint.fromLatLong(0.018, 0.013),
            icon =
                DefaultIcon(
                    scale = 2.1f,
                    label = "2.1",
                    debug = true,
                ),
            extra =
                """
                DefaultIcon(
                    scale = 2.1f,
                    label = "2.1",
                    debug = true,
                ),
                """.trimIndent(),
        )

        Marker(
            position = GeoPoint.fromLatLong(0.014, 0.004),
            extra = "DefaultIcon()",
        )
        Marker(
            position = GeoPoint.fromLatLong(0.014, 0.008),
            icon =
                DefaultIcon(
                    fillColor = Color.Yellow,
                    strokeColor = Color.Black,
                    strokeWidth = 2.dp,
                ),
            extra =
                """
                DefaultIcon(
                    fillColor = Color.Yellow,
                    strokeColor = Color.Black,
                    strokeWidth = 2.dp,
                )
                """.trimIndent(),
        )
        Marker(
            position = GeoPoint.fromLatLong(0.014, 0.012),
            icon =
                DefaultIcon(
                    fillColor =
                        Color(
                            red = 0x2E,
                            green = 0xF5,
                            blue = 0x27,
                        ),
                    strokeColor =
                        Color(
                            red = 0xFC,
                            green = 0x22,
                            blue = 0x5C,
                        ),
                    label = "AB",
                    labelTextColor = Color.White,
                    labelStrokeColor = Color.Black,
                ),
            extra =
                """
                DefaultIcon(
                    fillColor = Color(
                        red = 0x2E,
                        green = 0xF5,
                        blue = 0x27,
                    ),
                    strokeColor = Color(
                        red = 0xFC,
                        green = 0x22,
                        blue = 0x5C,
                    ),
                    label = "AB",
                    labelTextColor = Color.White,
                    labelStrokeColor = Color.Black,
                )
                """.trimIndent(),
        )

        AppCompatResources.getDrawable(context, R.drawable.human)?.let { icon ->
            Marker(
                position = GeoPoint.fromLatLong(0.01, 0.004),
                icon =
                    DrawableDefaultIcon(
                        backgroundDrawable = icon,
                    ),
                extra =
                    """
                    DrawableDefaultIcon(
                        backgroundDrawable = icon,
                    )
                    """.trimIndent(),
            )
        }

        AppCompatResources.getDrawable(context, R.drawable.ic_launcher_foreground)?.let { icon ->
            Marker(
                position = GeoPoint.fromLatLong(0.01, 0.006),
                icon =
                    DrawableDefaultIcon(
                        backgroundDrawable = icon,
                        strokeColor = Color.Black,
                        scale = 1.5f,
                    ),
                extra =
                    """
                    DrawableDefaultIcon(
                        backgroundDrawable = icon,
                        strokeColor = Color.Black,
                        scale = 1.5f,
                    )
                    """.trimIndent(),
            )
        }

        AppCompatResources.getDrawable(context, R.drawable.wmo_00_clear)?.let { icon ->
            Marker(
                position = GeoPoint.fromLatLong(0.01, 0.009),
                icon =
                    ImageIcon(
                        drawable = icon,
                        debug = true,
                        anchor = Offset(0.5f, 1.0f),
                    ),
                extra =
                    """
                    ImageIcon(
                        drawable = icon,
                        debug = true,
                        anchor = Offset(0.5f, 1.0f),
                    )
                    """.trimIndent(),
            )
        }

        createMarkerWithLabelIcon(context, "Label")?.let {
            Marker(
                position = GeoPoint.fromLatLong(0.01, 0.012),
                icon =
                    ImageIcon(
                        drawable = it,
                        anchor = Offset(0.5f, 1.0f),
                    ),
                extra =
                    """
                    ImageIcon(
                        drawable = createMarkerWithLabelIcon(label),
                        anchor = Offset(0.5f, 1.0f),
                    )
                    """.trimIndent(),
            )
        }
        (selected?.extra as? String)?.let { snippet ->
            InfoBubble(
                marker = selected!!,
                bubbleColor = bubbleColor,
            ) {
                Text(
                    text = snippet,
                    textAlign = TextAlign.Left,
                )
            }
        }
    }
}

fun createMarkerWithLabelIcon(
    context: Context,
    label: String,
): BitmapDrawable? {
    val drawable = AppCompatResources.getDrawable(context, R.drawable.marker_with_label) ?: return null

    val iconBitmap = drawable.toBitmap()
    Canvas(iconBitmap).apply {
        val textPaint =
            Paint().apply {
                color = Color.White.toArgb()
                this.textSize = ResourceProvider.dpToPx(40f).toFloat()
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
                isSubpixelText = true
            }

        drawText(label, ResourceProvider.dpToPx(77f).toFloat(), ResourceProvider.dpToPx(120f).toFloat(), textPaint)
    }

    return iconBitmap.toDrawable(context.resources)
}
