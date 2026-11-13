package com.mapconductor.example.pages.circle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.mapconductor.compose.circle.Circles
import com.mapconductor.compose.info.InfoBubble
import com.mapconductor.core.circle.CircleState
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.map.MapCameraPosition
import com.mapconductor.core.map.MapViewStateInterface
import com.mapconductor.example.MapViewContainer
import com.mapconductor.example.ui.DefaultMapViewItems
import com.mapconductor.example.ui.DemoMapPageScaffold
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val carrierNames = listOf("AT&T", "T-Mobile / Sprint", "Verizon", "Others")
private val carrierColors = listOf(Color(0xFF2563EB), Color(0xFFDB2777), Color(0xFFD97706), Color(0xFF64748B))

private data class Antenna(
    val lat: Double,
    val lon: Double,
    val radius: Double,
    val registration: Long,
    val carrier: Int,
)

// Keep source records packed. CircleState.extra resolves a row on demand;
// retaining another 723,800 JVM record objects would duplicate the source data.
private class AntennaTable(
    bytes: ByteArray,
) : AbstractList<Antenna>() {
    private val buffer = ByteBuffer.wrap(bytes).asReadOnlyBuffer().order(ByteOrder.LITTLE_ENDIAN)
    override val size: Int
    val carrierCounts = IntArray(4)

    init {
        require(bytes.size >= 8 && String(bytes, 0, 4, Charsets.US_ASCII) == "MCCE")
        size = buffer.getInt(4)
        require(size >= 0 && bytes.size.toLong() == 8L + size * 16L)
        for (index in 0 until size) {
            val row = get(index)
            require(
                row.lat.isFinite() &&
                    row.lat in -90.0..90.0 &&
                    row.lon.isFinite() &&
                    row.lon in -180.0..180.0 &&
                    row.radius > 0 &&
                    row.radius <= 100000 &&
                    row.carrier in 0..3,
            )
            carrierCounts[row.carrier]++
        }
    }

    override fun get(index: Int): Antenna {
        require(index in 0 until size)
        val p = 8 + index * 16
        return Antenna(
            buffer.getInt(p) / 100000.0, buffer.getInt(p + 4) / 100000.0,
            buffer.getFloat(p + 8).toDouble(), index.toLong(), buffer.getInt(p + 12) / 1000,
        )
    }

    fun carrierAt(index: Int): Int = buffer.getInt(8 + index * 16 + 12) / 1000
}

@Composable
fun AntennaHorizonsPage(
    modifier: Modifier = Modifier,
    onToggleSidebar: () -> Unit = {},
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    var carriers by remember { mutableStateOf(setOf(0, 1, 2, 3)) }
    var rows by remember { mutableStateOf<List<Antenna>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var alaska by remember { mutableStateOf(false) }
    var mapState by remember { mutableStateOf<MapViewStateInterface<*>?>(null) }
    var states by remember { mutableStateOf<List<CircleState>>(emptyList()) }
    var allStates by remember { mutableStateOf<List<CircleState>>(emptyList()) }
    var tap by remember { mutableStateOf<Antenna?>(null) }
    var adding by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    LaunchedEffect(Unit) {
        try {
            rows =
                withContext(Dispatchers.IO) {
                    val bytes = context.assets.open("antennas/us-antennas.bin").use { it.readBytes() }
                    AntennaTable(bytes)
                }
        } catch (e: Exception) {
            error = e.message
        }
    }
    LaunchedEffect(rows) {
        tap = null
        allStates =
            withContext(Dispatchers.Default) {
                val onCellClick: com.mapconductor.core.circle.OnCircleEventHandler = { event ->
                    tap =
                        (event.state.extra as? String)?.removePrefix("asr-")?.toIntOrNull()?.let { rows.getOrNull(it) }
                }
                rows.map { a ->
                    val color = carrierColors[a.carrier]
                    val id = "asr-${a.registration}"
                    CircleState(
                        center = GeoPoint(a.lat, a.lon),
                        radiusMeters = 500.0,
                        id = id,
                        geodesic = true,
                        zIndex = a.registration.toInt(),
                        strokeWidth = 0.5.dp,
                        strokeColor = color.copy(alpha = 0.35f),
                        fillColor = color.copy(alpha = 0.65f),
                        extra = id,
                        onClick = onCellClick,
                    )
                }
            }
    }
    LaunchedEffect(allStates, carriers) {
        tap = null
        states =
            withContext(Dispatchers.Default) {
                if (carriers.size == carrierNames.size) {
                    allStates
                } else if (carriers.isEmpty()) {
                    emptyList()
                } else {
                    allStates.filterIndexed { index, _ ->
                        ((rows as? AntennaTable)?.carrierAt(index) ?: rows[index].carrier) in carriers
                    }
                }
            }
    }
    val camera =
        remember(alaska) {
            MapCameraPosition(
                position = if (alaska) GeoPoint(64.0, -150.0) else GeoPoint(39.0, -98.0),
                zoom = if (alaska) 5.0 else 4.0,
            )
        }
    key(alaska) {
        DemoMapPageScaffold(
            menuItems = DefaultMapViewItems(camera),
            onToggleSidebar = onToggleSidebar,
            onMapViewStateChanged = { mapState = it },
        ) { padding ->
            mapState?.let { state ->
                MapViewContainer(state = state, modifier = modifier.padding(padding), onMapClick = { tap = null }) {
                    Circles(
                        states,
                        onAddStart = { total -> adding = 0 to total },
                        onAddProgress = { added, total -> adding = added to total },
                        onAddEnd = { adding = null },
                    )
                    tap?.let { a ->
                        InfoBubble(
                            position =
                                GeoPoint(
                                    a.lat,
                                    a.lon,
                                ),
                        ) { Text("${carrierNames[a.carrier]}\nCell position · Display radius: 500 m") }
                    }
                }
            }
            Column(
                Modifier
                    .padding(top = padding.calculateTopPadding() + 12.dp, start = 12.dp)
                    .widthIn(max = 350.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp),
            ) {
                Text("US cell positions (MCC 310–314)", style = MaterialTheme.typography.titleMedium)
                Text(
                    error
                        ?: if (rows.isEmpty()) "Loading OpenCellID data…" else "${states.size} / ${rows.size} circles",
                )
                adding?.let { (added, total) -> Text("Adding to the map… $added / $total") }
                TextButton(onClick = { alaska = !alaska }) { Text(if (alaska) "US overview" else "Alaska") }
                carrierNames.forEachIndexed { index, name ->
                    Row {
                        Checkbox(
                            index in carriers,
                            { checked ->
                                carriers = if (checked) carriers + index else carriers - index
                            },
                        )
                        Text("$name (${(rows as? AntennaTable)?.carrierCounts?.get(index) ?: 0})")
                    }
                }
                Text(
                    "Cell positions shown as 500 m radius circles; not signal coverage. Showing all cells for the selected carriers. MCC 310–314; not a complete tower inventory.",
                    style = MaterialTheme.typography.bodySmall,
                )
                ClickableText(
                    text =
                        buildAnnotatedString {
                            append("Cell tower data from ")
                            pushStringAnnotation("URL", "https://opencellid.org")
                            append("OpenCellID")
                            pop()
                            append(" · ")
                            pushStringAnnotation(
                                "URL",
                                "https://creativecommons.org/licenses/by-sa/4.0/",
                            )
                            append("CC BY-SA 4.0")
                            pop()
                        },
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary),
                    onClick = { offset ->
                        // Link annotations keep both source and license independently tappable.
                        val url =
                            if (offset <
                                30
                            ) {
                                "https://opencellid.org"
                            } else {
                                "https://creativecommons.org/licenses/by-sa/4.0/"
                            }
                        uriHandler.openUri(url)
                    },
                )
            }
        }
    }
}
