package com.mapconductor.core.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import android.annotation.SuppressLint

sealed class StateOrValue<T> {
    abstract val value: T

    open fun update(newValue: T) {
        // Immutable by default
    }

    fun isDynamic() = this is StateOrValue.Dynamic

    fun isStatic() = this is StateOrValue.Static

    @SuppressLint("UnrememberedMutableState")
    @Composable
    fun <R> derived(transform: (T) -> R): StateOrValue<R> =
        when (this) {
            is Static -> Static(transform(value))
            is Dynamic -> {
                val derivedState = derivedStateOf { transform(this.state.value) }
                Dynamic(derivedState)
            }
        }

    class Static<T>(
        private val raw: T,
    ) : StateOrValue<T>() {
        override val value: T get() = raw
    }

    class Dynamic<T>(
        val state: State<T>,
    ) : StateOrValue<T>() {
        override val value: T get() = state.value

        override fun update(newValue: T) {
            if (state is MutableState<T>) {
                state.value = newValue
            }
        }
    }
}

fun <T> T.toStateOrValue(): StateOrValue<T> = StateOrValue.Static(this)

fun <T> State<T>.toStateOrValue(): StateOrValue<T> = StateOrValue.Dynamic(this)

@Composable
fun <T> StateOrValue<T>.asState(): State<T> =
    when (this) {
        is StateOrValue.Static -> remember { mutableStateOf(value) }
        is StateOrValue.Dynamic -> this.state
    }
