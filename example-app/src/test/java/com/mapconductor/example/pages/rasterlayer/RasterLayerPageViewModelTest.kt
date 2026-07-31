package com.mapconductor.example.pages.rasterlayer

import com.mapconductor.core.raster.RasterLayerSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test

class RasterLayerPageViewModelTest {
    private val nasaSource = RasterLayerSource.TileJson("https://example.com/nasa.json")
    private val standardSource = RasterLayerSource.TileJson("https://example.com/standard.json")
    private val nasa =
        GsiLayer(
            id = "nasa",
            displayName = "NASA",
            source = nasaSource,
        )
    private val standard =
        GsiLayer(
            id = "standard",
            displayName = "Standard",
            source = standardSource,
        )

    @Test
    fun injectedLayersDriveSelectionAndRasterSource() {
        val viewModel =
            RasterLayerPageViewModel(
                layers = listOf(nasa, standard),
                initialLayer = nasa,
            )

        assertEquals(listOf(nasa, standard), viewModel.availableLayers)
        assertSame(nasa, viewModel.selectedLayer)
        assertSame(nasaSource, viewModel.rasterLayerState.source)

        viewModel.selectLayer(standard)

        assertSame(standard, viewModel.selectedLayer)
        assertSame(standardSource, viewModel.rasterLayerState.source)
    }

    @Test
    fun selectingLayerOutsideInjectedDependenciesFails() {
        val viewModel =
            RasterLayerPageViewModel(
                layers = listOf(nasa, standard),
                initialLayer = nasa,
            )
        val unknown =
            GsiLayer(
                id = "unknown",
                displayName = "Unknown",
                source = RasterLayerSource.TileJson("https://example.com/unknown.json"),
            )

        assertThrows(IllegalArgumentException::class.java) {
            viewModel.selectLayer(unknown)
        }
    }
}
