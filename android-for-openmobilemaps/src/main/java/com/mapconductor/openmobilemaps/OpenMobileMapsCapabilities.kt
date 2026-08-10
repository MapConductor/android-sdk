package com.mapconductor.openmobilemaps

import com.mapconductor.core.map.MapCapability
import com.mapconductor.core.map.MapCapabilityStatus
import com.mapconductor.core.map.MutableMapServiceRegistry

/**
 * このドライバーで何ができて何ができないかの宣言。
 *
 * **「宣言しない」＝「使えない」ではない**（[MapCapabilityStatus.Unknown]）。
 * コアは Unknown を非対応と断定しないので、書かなければ従来どおり動く。
 * 書く価値があるのは「**できない**と分かっているもの」で、それを宣言しておくと
 * 該当機能が黙って無反応になる代わりに理由つきのログを 1 回出す。
 *
 * コントローラの外に出してあるのは、**ネイティブライブラリ無しで検証できるようにする**ため。
 * コントローラを組み立てるにはレイヤ（`PolygonLayerInterface.create()` など JNI 呼び出し）が
 * 要るので、素の JVM テストからは作れない。
 */
object OpenMobileMapsCapabilities {
    fun declare(registry: MutableMapServiceRegistry) {
        registry.declare(MapCapability.ScreenProjectionSync, MapCapabilityStatus.Supported)
        registry.declare(MapCapability.PolygonHoles, MapCapabilityStatus.Supported)
        registry.declare(MapCapability.ClickPassthrough, MapCapabilityStatus.Supported)
        registry.declare(MapCapability.MarkerDrag, MapCapabilityStatus.Supported)
        registry.declare(
            MapCapability.CameraTilt,
            MapCapabilityStatus.Approximated(
                "the 2d camera has no pitch; tilt is emulated by rotating the view and " +
                    "shifting the camera target (same method as android-for-arcgis 2d)",
            ),
        )
        registry.declareUnsupported(
            MapCapability.GestureTilt,
            "the 2d camera has no pitch, so there is no tilt gesture to enable or disable",
        )
        registry.declareUnsupported(
            MapCapability.GestureScroll,
            "the sdk exposes no per-gesture toggle; only the whole touch handler can be turned off",
        )
        registry.declareUnsupported(
            MapCapability.GestureZoom,
            "the sdk exposes no per-gesture toggle; only the whole touch handler can be turned off",
        )
    }
}
