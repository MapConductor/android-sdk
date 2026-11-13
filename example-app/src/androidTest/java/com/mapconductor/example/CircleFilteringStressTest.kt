package com.mapconductor.example

import androidx.compose.ui.platform.ViewRootForTest
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getAllSemanticsNodes
import androidx.compose.ui.semantics.getOrNull
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.ViewGroup

/** Full nationwide data, native camera zooms, repeated carrier removal/restoration. */
@RunWith(AndroidJUnit4::class)
class CircleFilteringStressTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    private fun <T : Any> onMain(block: () -> T): T {
        var result: T? = null
        instrumentation.runOnMainSync { result = block() }
        return result!!
    }

    private fun activity(): MainActivity =
        ActivityLifecycleMonitorRegistry
            .getInstance()
            .getActivitiesInStage(Stage.RESUMED)
            .filterIsInstance<MainActivity>()
            .single()

    // Read Compose semantics directly. This leaves the app's real frame clock
    // running and avoids depending on the device accessibility service cache.
    private fun nodes(view: View): Sequence<SemanticsNode> =
        sequence {
            if (view is ViewRootForTest) yieldAll(view.semanticsOwner.getAllSemanticsNodes(false))
            if (view is ViewGroup) for (index in 0 until view.childCount) yieldAll(nodes(view.getChildAt(index)))
        }

    private fun toggleVerizon() =
        onMain {
            val checkboxes =
                nodes(activity().window.decorView)
                    .filter { it.config.getOrNull(SemanticsProperties.ToggleableState) != null }
                    .sortedBy { it.boundsInRoot.top }
                    .toList()
            assertTrue(checkboxes.size >= 4)
            assertTrue(checkboxes[2].config[SemanticsActions.OnClick].action!!.invoke())
        }

    private fun waitForCount(count: Int) {
        val deadline = SystemClock.uptimeMillis() + 60_000
        var lastTexts = emptyList<String>()
        while (SystemClock.uptimeMillis() < deadline) {
            lastTexts =
                onMain {
                    nodes(activity().window.decorView)
                        .mapNotNull {
                            it.config.getOrNull(SemanticsProperties.Text)?.joinToString("") { text -> text.text }
                        }.toList()
                }
            if (lastTexts.any { it == "$count / 723800 circles" }) return
            SystemClock.sleep(250)
        }
        throw AssertionError("Circle count did not reach $count; semantics texts: $lastTexts")
    }

    private fun mapView(view: View): MapView? {
        if (view is MapView) return view
        if (view is ViewGroup) for (i in 0 until view.childCount) mapView(view.getChildAt(i))?.let { return it }
        return null
    }

    @Test
    fun zoomAndToggleVerizonFiveTimes() {
        val intent =
            Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java).apply {
                putExtra("page", "antenna-horizons")
                putExtra("provider", "maplibre")
            }
        ActivityScenario.launch<MainActivity>(intent).use {
            waitForCount(723800)
            SystemClock.sleep(5_000)
            lateinit var map: MapLibreMap
            val ready = CountDownLatch(1)
            onMain {
                val view = mapView(activity().window.decorView)!!
                view.getMapAsync {
                    map = it
                    ready.countDown()
                }
            }
            assertTrue(ready.await(10, TimeUnit.SECONDS))

            fun zoom(): Double {
                var result = 0.0
                instrumentation.runOnMainSync { result = map.cameraPosition.zoom }
                return result
            }
            repeat(5) {
                Log.i("CircleFilterStress", "Starting cycle ${it + 1}")
                val before = zoom()
                onMain { map.animateCamera(CameraUpdateFactory.zoomTo(before + 2), 300) }
                SystemClock.sleep(1_000)
                assertTrue("Native map must actually zoom in", zoom() > before + 0.5)
                onMain { map.animateCamera(CameraUpdateFactory.zoomTo(before), 300) }
                SystemClock.sleep(1_000)
                assertTrue("Native map must actually zoom out", zoom() < before + 0.5)
                toggleVerizon()
                waitForCount(576636)
                SystemClock.sleep(3_000)
                toggleVerizon()
                waitForCount(723800)
                SystemClock.sleep(3_000)
                Log.i("CircleFilterStress", "Completed cycle ${it + 1}")
            }
        }
    }
}
