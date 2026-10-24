# Pixel 5a GPU stencil experiment

This reproduces the expensive forest in Shortbread 8/226/100. These are
instrumentation fixtures; they are not shipped in the SDK or example app.
No production renderer is switched by this experiment.

Data: https://vector.openstreetmap.org/shortbread_v1/8/226/100.mvt
Captured from the Pixel source-tile cache on 2026-10-07.
© OpenStreetMap contributors: https://www.openstreetmap.org/copyright

Style: https://tiles.versatiles.org/assets/styles/colorful/style.json
Only land-forest is retained; sources are rewritten as in VectorTileStyleLoader.

## Files

- 8-226-100.mvt: actual cached, decompressed tile.
- style.json: forest-only comparison style.
- forest-rings.bin.gzip: gzip of LE u32 extent, four float32 colour channels,
  u32 ring count, then each ring's u32 vertex count and float32 xy pairs.
- forest-earcut.bin.gzip: gzip of float32-LE triangle vertices: x,y,r,g,b,a.

There is one forest feature with 9,911 rings and 87,787 vertices.
Earcut produces 85,126 triangles; simple fans produce 67,965 triangles.
The device's native triangle output is checked against this reference.

## Running

From android-sdk with the Pixel connected:

    ./gradlew :android-vectortile:connectedDebugAndroidTest       -Pandroid.testInstrumentationRunnerArguments.class=com.mapconductor.vectortile.GpuStencilExperimentTest

The baseline takes tens of seconds. To preserve PNGs, run the test APK directly
(Gradle uninstalls its temporary test app):

    adb install -r android-vectortile/build/outputs/apk/androidTest/debug/android-vectortile-debug-androidTest.apk
    adb shell am instrument -w       -e class com.mapconductor.vectortile.GpuStencilExperimentTest       com.mapconductor.vectortile.test/androidx.test.runner.AndroidJUnitRunner
    adb pull /sdcard/Android/data/com.mapconductor.vectortile.test/files/stencil-experiment

Add -e skipBaseline true to repeat the GPU measurements without slow native
triangle generation/reference validation.

## Method and limitations

The baseline measures the actual Android JNI decode + earcut on the original
MVT. Both images are GPU-rendered at 512x512 with 4x MSAA. No CPU rasterisation.

The candidate packs simple fans linearly, draws them with colour writes off and
GL_INVERT into a one-bit stencil mask, then colours a covering rectangle.
All rings of this feature contribute to the same even-odd mask.
A D24S8 attachment adds approximately 4 MiB at this size/sample count.

Seven interleaved reference/candidate GPU timings include upload, completion,
MSAA resolve and readback. glFinish runs before each sample. PNG saving is
outside these timings.

Candidate preparation includes fixture read/decompression and fan packing.
Its rings are decoded offline, so it excludes MVT decode, production style/filter
processing and JNI transfer. These are stage comparisons, not full-app speedups.

Four translated 256x256 quadrants are joined and must exactly match the single
512x512 stencil image, including coverage along tile boundaries.

138 pixels differ in alpha from earcut (0.053%); mean absolute alpha difference
is 0.052 on a 0..255 scale. Some differences are fully opaque. They must not all
be described as antialiasing differences. The output is not pixel-identical.

Adoption requires winding/overlapping-ring rules, separate features, style order,
paint variation, other sizes, ES2/driver support and pipeline integration tests.

## Regenerating

The native export_stencil_experiment example accepts a directory containing
the full example style.json and 8-226-100.mvt, and emits rings and triangles.
Compress the .bin files with gzip to .bin.gzip before copying them here.
The .gzip suffix avoids Android asset-merger special handling of .gz files.
