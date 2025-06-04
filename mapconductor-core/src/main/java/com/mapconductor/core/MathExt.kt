package com.mapconductor.core

import java.math.BigDecimal
import java.math.RoundingMode

internal fun Double.toFixed(decimals: Int = 0): String =
    BigDecimal(this)
        .setScale(decimals, RoundingMode.DOWN)
        .toPlainString()
