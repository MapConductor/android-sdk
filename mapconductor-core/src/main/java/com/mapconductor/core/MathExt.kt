package com.mapconductor.core

import java.math.BigDecimal
import java.math.RoundingMode

fun Double.toFixed(decimals: Int = 0): String =
    BigDecimal(this)
        .setScale(decimals, RoundingMode.DOWN)
        .toPlainString()

fun Float.toFixed(decimals: Int = 0): String =
    BigDecimal(this.toDouble())
        .setScale(decimals, RoundingMode.DOWN)
        .toPlainString()
