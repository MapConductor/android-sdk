package com.mapconductor.core.marker

import com.mapconductor.core.ChildCollector
import com.mapconductor.core.ChildCollectorImpl
import com.mapconductor.settings.Settings
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class MarkerCollector(
    updateDebounce: Duration = Settings.Default.composeEventDebounce,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Main.immediate),
) : ChildCollector<MarkerState> by ChildCollectorImpl(
        asFlow = { it.asFlow() },
        updateDebounce = updateDebounce,
        scope = scope,
    )
