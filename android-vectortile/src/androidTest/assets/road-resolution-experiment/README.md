# Road raster resolution experiment

Actual Shortbread tile 11/1808/807 from the Pixel 5a cache, captured 2026-10-07:
https://vector.openstreetmap.org/shortbread_v1/11/1808/807.mvt

© OpenStreetMap contributors: https://www.openstreetmap.org/copyright
Style: https://tiles.versatiles.org/assets/styles/colorful/style.json
Sources are rewritten as in the example app.

RoadResolutionExperimentTest decodes/tessellates once and renders the exact same
vertex buffer, style, extent and batches at 512 and 1024 pixels. Both use ES3 and
4x MSAA. These fixtures and tests are not included in the production library.

Pixel 5a results: median draw+MSAA resolve+readback of seven samples:
512: 4.37ms; 1024: 9.81ms. Timing includes readback buffer allocation; production
uses a buffer pool. Upload/tessellation, framebuffer creation and PNG encoding
are excluded. Larger buffers and MSAA attachments cost approximately 4x memory.

The 512 image was pixel-identical to the example app's cached geometry PNG.
The 1024 image shows smoother road edges at the same display size.
This test is an isolated tile experiment. The online and offline example pages
now request 2x ground resolution on OMM and Google Maps.

Run from android-sdk:

    ./gradlew :android-vectortile:connectedDebugAndroidTest       -Pandroid.testInstrumentationRunnerArguments.class=com.mapconductor.vectortile.RoadResolutionExperimentTest

To retrieve images, install the Android test APK and run instrumentation directly,
then pull Android/data/com.mapconductor.vectortile.test/files/road-resolution-experiment
from the device. The Gradle connected-test task uninstalls its temporary APK.
