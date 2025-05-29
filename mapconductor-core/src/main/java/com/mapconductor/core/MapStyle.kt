package com.mapconductor.core

interface MapDesignType<T> {
    val id: T
    fun getValue(): T
}