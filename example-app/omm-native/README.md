# OMM flat raster seam fix

The examples app bundles an arm64 `libmapscore.so` ahead of the OMM 4.0.0 AAR's
native library. The binary is built from openmobilemaps/maps-core commit
`82c1fac` plus `flat-raster-seams.patch`; the source changes are also available
in this workspace's `ios-sdk/maps-core` checkout. OMM is licensed under MPL-2.0
(see LICENSE). No MapConductor classes are added to OMM.

For flat, stencil-masked raster tiles, expand the quad and its UV coordinates
by the same fraction. Keeping the mapping unchanged inside the tile avoids
compressing boundary labels, while the expanded geometry covers subpixel gaps.
Textures use GL_CLAMP_TO_EDGE. Unmasked and 3D paths keep their previous behavior.
This refines the earlier zero-overlap proposal in upstream PR #928.

Rebuild from the Android SDK checkout with `example-app/omm-native/rebuild.sh`.
Set ANDROID_HOME if needed. The script uses NDK 27.1.12297006, CMake 3.22.1,
and arm64 only. Build products and the source checkout go into example-app/build.
After rebuilding, run `./gradlew :example-app:assembleDebug`.
