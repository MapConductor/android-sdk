package com.mapconductor.core

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlinx.coroutines.flow.MutableStateFlow

class StateFlowDelegate<T>(
    initialValue: T,
) : ReadWriteProperty<Any?, T> {
    private val stateFlow = MutableStateFlow(initialValue)

    override fun getValue(
        thisRef: Any?,
        property: KProperty<*>,
    ): T = stateFlow.value

    override fun setValue(
        thisRef: Any?,
        property: KProperty<*>,
        value: T,
    ) {
        stateFlow.value = value
    }

    fun asStateFlow() = stateFlow
}
