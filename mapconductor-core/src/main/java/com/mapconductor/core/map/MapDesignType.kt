package com.mapconductor.core.map

interface MapDesignType<T> {
    val id: T

    fun getValue(): T
}
