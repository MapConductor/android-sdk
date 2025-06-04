package com.mapconductor.core.marker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.info.InfoBubbleSpec
import com.mapconductor.core.info.LocalInfoBubbleCollector
import com.mapconductor.core.map.MapViewScope
import kotlinx.coroutines.flow.MutableStateFlow
import android.os.Parcelable

open class MarkerScope(
    val bubbleCollector: MutableStateFlow<List<InfoBubbleSpec>>,
)

@Composable
fun MapViewScope.Marker(
    entry: MarkerEntry,
    content: (@Composable MarkerScope.() -> Unit)? = null,
) {
    val rememberEntry = remember { entry }

    if (!allMarkerKeys.contains(rememberEntry.state.id)) {
        allMarkerKeys.add(rememberEntry.state.id)

        SideEffect {
            markerFlow.value = markerFlow.value + rememberEntry
        }
    }

    val bubbleCollector = LocalInfoBubbleCollector.current
    content?.let {
        MarkerScope(bubbleCollector).it()
    }
}

@Composable
fun MapViewScope.Marker(
    builder: MarkerBuilder.() -> Unit,
    content: (@Composable MarkerScope.() -> Unit)? = null,
) {
    val entry = MarkerBuilder().apply(builder).build()
    Marker(entry, content)
}

@Composable
fun MapViewScope.Marker(
    state: MarkerState,
    onClick: MarkerClickHandler? = null,
    content: (@Composable MarkerScope.() -> Unit)? = null,
) {
    val handlers =
        MarkerHandlers(
            onClick = onClick,
        )
    val entry =
        MarkerEntry(
            state = state,
            handlers = handlers,
        )
    Marker(entry, content)
}

@Composable
fun MapViewScope.Marker(
    position: GeoPoint,
    icon: MarkerIconProp? = null,
    extra: Parcelable? = null,
    onClick: MarkerClickHandler? = null,
    content: (@Composable MarkerScope.() -> Unit)? = null,
) {
    val state =
        MarkerState(
            position = position,
            extra = extra,
            icon = icon,
        )
    val handlers =
        MarkerHandlers(
            onClick = onClick,
        )
    val entry =
        MarkerEntry(
            state = state,
            handlers = handlers,
        )
    Marker(entry, content)
}
