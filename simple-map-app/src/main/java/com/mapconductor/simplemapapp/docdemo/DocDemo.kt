package com.mapconductor.simplemapapp.docdemo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The six documentation-site videos that have not been shot, as screens.
 *
 * Each entry is one page on mapconductor.com whose `doc.video` block has no
 * youtubeId yet, and each screen is that page's own code sample made runnable.
 * The slug is the contract: it names the page, the recording, and the file the
 * recording is saved as, so nothing has to be matched up by hand afterwards.
 *
 * `MainActivity` reads a `demo` intent extra holding the slug, which is how the
 * recording script starts a take without touching the screen first:
 *
 *   adb shell am start -n com.mapconductor.simplemapapp/.MainActivity \
 *       --es demo geopoint-bounds
 */
enum class DocDemo(
    /** The page slug on the documentation site. */
    val slug: String,
    val title: String,
    /** What the recording has to show — the CMS block's `note`, in short. */
    val shows: String,
) {
    GeoPointBounds(
        "geopoint-bounds",
        "GeoPoint & GeoRectBounds",
        "bounds built point by point, then fitBounds framing them",
    ),
    MapView(
        "mapview",
        "MapView",
        "initialisation proceeding, and the loading overlay handing over",
    ),
    ProjectionZoom(
        "projection-zoom",
        "Projection & zoom",
        "one zoom value held across two providers",
    ),
    RasterLayer(
        "raster-layer",
        "Raster layer",
        "raster tiles overlaid, source and opacity changing in place",
    ),
    ReadingCamera(
        "reading-camera",
        "Reading the camera",
        "values read while the map is being dragged",
    ),
    SwitchingProviders(
        "switching-providers",
        "Switching providers",
        "the map view swapped while the overlays stay put",
    ),
    ;

    companion object {
        fun bySlug(slug: String?): DocDemo? = entries.firstOrNull { it.slug == slug }
    }
}

@Composable
fun DocDemoScreen(
    demo: DocDemo,
    modifier: Modifier = Modifier,
) {
    when (demo) {
        DocDemo.GeoPointBounds -> FitBoundsDemo(modifier)
        DocDemo.MapView -> LifecycleDemo(modifier)
        DocDemo.ProjectionZoom -> ZoomCompareDemo(modifier)
        DocDemo.RasterLayer -> RasterLayerDemo(modifier)
        DocDemo.ReadingCamera -> ReadingCameraDemo(modifier)
        DocDemo.SwitchingProviders -> SwitchingProvidersDemo(modifier)
    }
}

@Composable
fun DocDemoLauncher(
    modifier: Modifier = Modifier,
    onPick: (DocDemo) -> Unit,
) {
    Column(modifier.fillMaxSize()) {
        DemoCaption("Documentation recordings", "one screen per unshot video")
        LazyColumn(Modifier.fillMaxSize()) {
            items(DocDemo.entries) { demo ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onPick(demo) }
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                ) {
                    Text(demo.title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        "/${demo.slug}",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                    )
                    Text(demo.shows, fontSize = 14.sp)
                }
                HorizontalDivider()
            }
        }
    }
}
