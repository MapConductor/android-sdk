package com.mapconductor.vectortile

import com.mapconductor.core.InternalMapConductorApi
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

/** A cancelled tile must release its worker before an occupied render slot opens. */
@InternalMapConductorApi
internal fun Semaphore.acquireUnlessCancelled(isCancelled: () -> Boolean): Boolean {
    try {
        while (!isCancelled()) {
            if (tryAcquire(10, TimeUnit.MILLISECONDS)) {
                if (!isCancelled()) return true
                release()
                return false
            }
        }
    } catch (_: InterruptedException) {
        Thread.currentThread().interrupt()
    }
    return false
}
