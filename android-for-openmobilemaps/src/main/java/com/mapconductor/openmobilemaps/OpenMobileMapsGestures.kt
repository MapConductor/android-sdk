package com.mapconductor.openmobilemaps

import androidx.compose.ui.geometry.Offset
import io.openmobilemaps.mapscore.map.util.SimpleTouchInterface
import io.openmobilemaps.mapscore.shared.graphics.common.Vec2F
import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import kotlinx.coroutines.launch

/**
 * SDK のタッチをコアの受け口へつなぐ。
 *
 * ## 2 つの経路がある理由
 *
 * - **タップと長押し**は [SimpleTouchInterface] で受ける。押下時間・移動許容量の
 *   判定を SDK に任せられるので、他アプリと同じ感触になる。
 * - **ドラッグ中の指の位置**は Android の [MotionEvent] で受ける。SDK の
 *   `onMove` は**差分**しか渡してこないため、マーカーを指の真下に置けない。
 *
 * android-for-maplibre も同じ二段構え（あちらはジェスチャ検出器 + `OnTouchListener`）。
 */
internal class OpenMobileMapsTouchListener(
    private val controller: OpenMobileMapsMapViewController,
) : SimpleTouchInterface() {
    /**
     * 指が触れたらカメラアニメーションを止める。
     *
     * [OpenMobileMapsMapViewController.animateCamera] は自前でフレームを刻んで
     * カメラを動かすので、止めないとユーザーの操作と綱引きになり、
     * パンしても引き戻される。**消費はしない**（false を返す）ので、
     * SDK 側の通常の操作はそのまま流れる。
     */
    override fun onTouchDown(posScreen: Vec2F): Boolean {
        controller.cancelCameraAnimation()
        return false
    }

    override fun onClickConfirmed(posScreen: Vec2F): Boolean {
        val position = controller.holder.fromScreenOffsetSync(Offset(posScreen.x, posScreen.y)) ?: return false
        // カスケード（marker → circle → groundImage → polyline → polygon → map）は
        // コアが回す。ここは座標を渡すだけ。
        controller.mainCoroutine.launch { controller.dispatchTap(position) }
        return true
    }

    override fun onLongPress(posScreen: Vec2F): Boolean {
        val position = controller.holder.fromScreenOffsetSync(Offset(posScreen.x, posScreen.y)) ?: return false
        controller.mainCoroutine.launch { controller.handleLongPress(position) }
        return true
    }

    override fun onMoveComplete(): Boolean {
        controller.emitCameraMoveEndFromGesture()
        return false
    }
}

/**
 * 長押し。ドラッグ可能なマーカーの上ならドラッグを開始し、そうでなければ地図の長押し。
 *
 * **メインスレッドで呼ぶこと**（ビューのタッチリスナーを差し替えるため）。
 */
internal fun OpenMobileMapsMapViewController.handleLongPress(position: com.mapconductor.core.features.GeoPoint) {
    markerEventControllers.forEach { controller ->
        val entity = controller.find(position) ?: return@forEach
        if (!entity.state.draggable) return@forEach

        // ドラッグ中は地図を動かさない。タッチハンドラごと止めるのは、
        // 開始前に始まった慣性が残って地図が流れるのを防ぐため。
        runCatching { holder.mapView.mapView.setTouchEnabled(false) }
        activeDragController = controller
        controller.setSelectedMarker(entity)
        controller.dispatchDragStart(entity.state)
        installDragTouchInterceptor()
        return
    }

    emitMapLongClick(position)
}

/**
 * ドラッグ中だけタッチを横取りする。
 *
 * 内側の `MapView` に載せる。`OnTouchListener` が true を返すと `onTouchEvent` は
 * 呼ばれないので、地図は動かない。
 */
@SuppressLint("ClickableViewAccessibility")
internal fun OpenMobileMapsMapViewController.installDragTouchInterceptor() {
    if (dragTouchInterceptor != null) return
    val view = holder.mapView.mapView
    val listener =
        View.OnTouchListener { _, event ->
            val controller = activeDragController ?: return@OnTouchListener false
            val selected = controller.getSelectedMarker() ?: return@OnTouchListener false
            when (event.actionMasked) {
                MotionEvent.ACTION_MOVE -> {
                    holder.fromScreenOffsetSync(Offset(event.x, event.y))?.let { position ->
                        selected.state.position = position
                        controller.updateDragPosition(position)
                        controller.dispatchDrag(selected.state)
                    }
                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // 離した位置を確定させてから dragEnd を配送する。ここを省くと
                    // 最後の ACTION_MOVE の位置で確定し、指の位置と 1 フレームずれる。
                    holder.fromScreenOffsetSync(Offset(event.x, event.y))?.let { position ->
                        selected.state.position = position
                        controller.updateDragPosition(position)
                    }
                    controller.setSelectedMarker(null)
                    controller.dispatchDragEnd(selected.state)
                    runCatching { holder.mapView.mapView.setTouchEnabled(true) }
                    removeDragTouchInterceptor()
                    activeDragController = null
                    true
                }

                else -> false
            }
        }
    dragTouchInterceptor = listener
    view.setOnTouchListener(listener)
}

@SuppressLint("ClickableViewAccessibility")
internal fun OpenMobileMapsMapViewController.removeDragTouchInterceptor() {
    if (dragTouchInterceptor == null) return
    runCatching { holder.mapView.mapView.setOnTouchListener(null) }
    dragTouchInterceptor = null
}
