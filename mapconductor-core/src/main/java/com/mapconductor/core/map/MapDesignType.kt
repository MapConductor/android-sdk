package com.mapconductor.core.map

interface MapDesignTypeInterface<T> {
    val id: T

    fun getValue(): T
}
