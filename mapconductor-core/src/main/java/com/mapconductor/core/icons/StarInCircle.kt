package com.mapconductor.core.icons

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.mapconductor.core.marker.MarkerIcon
import android.graphics.Color
import android.graphics.Path

fun MarkerIcon.Companion.StarInCircle(
    fillColor: Int? = Color.YELLOW,
    strokeColor: Int? = Color.WHITE,
    strokeWidth: Float? = 1f,
    scale: Float? = 2f,
): MarkerIcon {
    // https://www.iconfinder.com/icons/1398914/circle_five_point_gold_star_favorite_icon

    val path = Path()

    // 元のSVGの描画内容のバウンディングボックス
    val originalContentXMin = 50f
    val originalContentYMin = 140f
    val originalContentWidth = 512f // (562 - 50)
    val originalContentHeight = 512f // (652 - 140) (このアイコンは正方形が基準)

    // 全体のターゲットサイズとマージン
    // val overallSize = 28f // この値は直接計算には使わないが、以下の値の導出元
    val margin = 2f
    val drawableAreaSize = 28f - (2f * margin) // 28f - 4f = 20f

    // スケーリング係数（描画領域のサイズに合わせる）
    val scale = drawableAreaSize / originalContentWidth // 20f / 512f

    // スケーリングとオフセット（マージン）を適用するヘルパー関数
    // 絶対座標を新しい座標系に変換
    fun sX(originalX: Float): Float = ((originalX - originalContentXMin) * scale) + margin

    fun sY(originalY: Float): Float = ((originalY - originalContentYMin) * scale) + margin

    // 相対的な距離（寸法）をスケーリング
    fun sDim(dim: Float): Float = dim * scale

    // SVGコマンドを処理するための状態変数 (元の座標系で保持)
    var currentX = 0f
    var currentY = 0f
    var lastKnotX = 0f // 直前のコマンドの終点X
    var lastKnotY = 0f // 直前のコマンドの終点Y
    var lastControlX = 0f // 直前のベジェ曲線の第2制御点X (S,sコマンド用)
    var lastControlY = 0f // 直前のベジェ曲線の第2制御点Y (S,sコマンド用)
    var lastCommand = ' ' // 直前のコマンド種別

    // --- パス記述開始 ---

    // サブパス1: 円 (Circle)
    // M562,396
    currentX = 562f
    currentY = 396f
    // sX(562) = ((562-50)*(20/512))+2 = (512*(20/512))+2 = 20+2 = 22.0
    // sY(396) = ((396-140)*(20/512))+2 = (256*(20/512))+2 = 10+2 = 12.0
    path.moveTo(sX(currentX), sY(currentY))
    lastKnotX = currentX
    lastKnotY = currentY
    lastCommand = 'M'

    // c0-141.4-114.6-256-256-256  (relative cubic Bézier)
    val c1d1 = Offset(0f, -141.4f)
    val c1d2 = Offset(-114.6f, -256f)
    val c1d = Offset(-256f, -256f)
    path.rCubicTo(
        sDim(c1d1.x), sDim(c1d1.y), // sDim(0)=0.0, sDim(-141.4)=-5.5234375
        sDim(c1d2.x), sDim(c1d2.y), // sDim(-114.6)=-4.4765625, sDim(-256)=-10.0
        sDim(c1d.x), sDim(c1d.y), // sDim(-256)=-10.0, sDim(-256)=-10.0
    )
    lastControlX = currentX + c1d2.x
    lastControlY = currentY + c1d2.y
    currentX += c1d.x
    currentY += c1d.y
    lastKnotX = currentX
    lastKnotY = currentY
    lastCommand = 'c'

    // S50,254.6,50,396 (absolute smooth cubic)
    val s1AbsC1 =
        if (lastCommand == 'c' || lastCommand == 's' || lastCommand == 'C' || lastCommand == 'S') {
            Offset(
                lastKnotX + (lastKnotX - lastControlX),
                lastKnotY + (lastKnotY - lastControlY),
            )
        } else {
            Offset(lastKnotX, lastKnotY)
        }
    val s1absC2 = Offset(50f, 254.6f)
    val s1AbsEnd = Offset(50f, 396f)
    path.cubicTo(
        // sX(164.6)=((164.6-50)*(20/512))+2 = 4.4765625+2 = 6.4765625, sY(140.0)=((140-140)*(20/512))+2 = 0+2 = 2.0
        sX(s1AbsC1.x), sY(s1AbsC1.y),
        // sX(50.0)=2.0, sY(254.6)=((254.6-140)*(20/512))+2=4.4765625+2 = 6.4765625
        sX(s1absC2.x), sY(s1absC2.y),
        // sX(50.0)=2.0, sY(396.0)=12.0
        sX(s1AbsEnd.x), sY(s1AbsEnd.y),
    )
    lastControlX = s1absC2.x
    lastControlY = s1absC2.y
    currentX = s1AbsEnd.x
    currentY = s1AbsEnd.y
    lastKnotX = currentX
    lastKnotY = currentY
    lastCommand = 'S'

    // s114.6,256,256,256 (relative smooth cubic)
    val s2AbsC1 =
        if (lastCommand == 'c' || lastCommand == 's' || lastCommand == 'C' || lastCommand == 'S') {
            Offset(
                lastKnotX + (lastKnotX - lastControlX),
                lastKnotY + (lastKnotY - lastControlY),
            )
        } else {
            Offset(lastKnotX, lastKnotY)
        }
    val s2D2 = Offset(114.6f, 256f)
    val s2D = Offset(256f, 256f)
    val s2AbsC2 = Offset(currentX + s2D2.x, currentY + s2D2.y)
    val s2AbsEnd = Offset(currentX + s2D.x, currentY + s2D.y)
    path.cubicTo(
        sX(s2AbsC1.x), sY(s2AbsC1.y), // sX(50.0)=2.0, sY(537.4)=((537.4-140)*(20/512))+2=15.5234375+2 = 17.523438
        sX(s2AbsC2.x), sY(s2AbsC2.y), // sX(164.6)=6.4765625, sY(652.0)=((652-140)*(20/512))+2=20+2 = 22.0
        sX(s2AbsEnd.x), sY(s2AbsEnd.y), // sX(306.0)=((306-50)*(20/512))+2=10+2 = 12.0, sY(652.0)=22.0
    )
    lastControlX = s2AbsC2.x
    lastControlY = s2AbsC2.y
    currentX = s2AbsEnd.x
    currentY = s2AbsEnd.y
    lastKnotX = currentX
    lastKnotY = currentY
    lastCommand = 's'

    // S562,537.4,562,396 (absolute smooth cubic Bézier)
    val s3AbsC1 =
        if (lastCommand == 'c' || lastCommand == 's' || lastCommand == 'C' || lastCommand == 'S') {
            Offset(
                lastKnotX + (lastKnotX - lastControlX),
                lastKnotY + (lastKnotY - lastControlY),
            )
        } else {
            Offset(lastKnotX, lastKnotY)
        }
    val s3AbsC2 = Offset(562f, 537.4f)
    val s3AbsEnd = Offset(562f, 396f)
    path.cubicTo(
        sX(s3AbsC1.x), sY(s3AbsC1.y), // sX(447.4)=((447.4-50)*(20/512))+2=15.5234375+2 = 17.523438, sY(652.0)=22.0
        sX(s3AbsC2.x), sY(s3AbsC2.y), // sX(562.0)=22.0, sY(537.4)=17.523438
        sX(s3AbsEnd.x), sY(s3AbsEnd.y), // sX(562.0)=22.0, sY(396.0)=12.0
    )
    path.close()

    // サブパス2: 星 (Star)
    // M499.3,352.6
    currentX = 499.3f
    currentY = 352.6f
    // sX(499.3)=((499.3-50)*(20/512))+2=17.55078125+2 = 19.550781
    // sY(352.6)=((352.6-140)*(20/512))+2=8.3046875+2 = 10.3046875
    path.moveTo(sX(currentX), sY(currentY))
    lastKnotX = currentX
    lastKnotY = currentY
    lastCommand = 'M'

    // L415.5,451
    currentX = 415.5f
    currentY = 451f
    // sX(415.5)=((415.5-50)*(20/512))+2=14.27734375+2 = 16.277344
    // sY(451.0)=((451.0-140)*(20/512))+2=12.1484375+2 = 14.1484375
    path.lineTo(sX(currentX), sY(currentY))
    lastKnotX = currentX
    lastKnotY = currentY
    lastCommand = 'L'

    // l10,128.8 (relative lineTo)
    var lDx = 10f
    var lDy = 128.8f
    // sDim(10.0)=10.0*(20/512) = 0.390625
    // sDim(128.8)=128.8*(20/512) = 5.03125
    path.rLineTo(sDim(lDx), sDim(lDy))
    currentX += lDx
    currentY += lDy
    lastKnotX = currentX
    lastKnotY = currentY
    lastCommand = 'l'

    // L306,530.5
    currentX = 306f
    currentY = 530.5f
    // sX(306.0)=12.0
    // sY(530.5)=((530.5-140)*(20/512))+2=15.25390625+2 = 17.253906
    path.lineTo(sX(currentX), sY(currentY))
    lastKnotX = currentX
    lastKnotY = currentY
    lastCommand = 'L'

    // l-119.5,49.3 (relative lineTo)
    lDx = -119.5f
    lDy = 49.3f
    // sDim(-119.5)=-4.66796875
    // sDim(49.3)=1.92578125
    path.rLineTo(sDim(lDx), sDim(lDy))
    currentX += lDx
    currentY += lDy
    lastKnotX = currentX
    lastKnotY = currentY
    lastCommand = 'l'

    // l10,-128.8 (relative lineTo)
    lDx = 10f
    lDy = -128.8f
    // sDim(10.0)=0.390625
    // sDim(-128.8)=-5.03125
    path.rLineTo(sDim(lDx), sDim(lDy))
    currentX += lDx
    currentY += lDy
    lastKnotX = currentX
    lastKnotY = currentY
    lastCommand = 'l'

    // l-83.8,-98.4 (relative lineTo)
    lDx = -83.8f
    lDy = -98.4f
    // sDim(-83.8)=-3.2734375
    // sDim(-98.4)=-3.84375
    path.rLineTo(sDim(lDx), sDim(lDy))
    currentX += lDx
    currentY += lDy
    lastKnotX = currentX
    lastKnotY = currentY
    lastCommand = 'l'

    // l125.6,-30.3 (relative lineTo)
    lDx = 125.6f
    lDy = -30.3f
    // sDim(125.6)=4.90625
    // sDim(-30.3)=-1.18359375
    path.rLineTo(sDim(lDx), sDim(lDy))
    currentX += lDx
    currentY += lDy
    lastKnotX = currentX
    lastKnotY = currentY
    lastCommand = 'l'

    // L306,212.2
    currentX = 306f
    currentY = 212.2f
    // sX(306.0)=12.0
    // sY(212.2)=((212.2-140)*(20/512))+2=2.8203125+2 = 4.8203125
    path.lineTo(sX(currentX), sY(currentY))
    lastKnotX = currentX
    lastKnotY = currentY
    lastCommand = 'L'

    // l67.6,110.1 (relative lineTo)
    lDx = 67.6f
    lDy = 110.1f
    // sDim(67.6)=2.640625
    // sDim(110.1)=4.299218654632568
    path.rLineTo(sDim(lDx), sDim(lDy))
    currentX += lDx
    currentY += lDy
    lastKnotX = currentX
    lastKnotY = currentY
    lastCommand = 'l'

    // L499.3,352.6
    currentX = 499.3f
    currentY = 352.6f
    // sX(499.3)=19.550781
    // sY(352.6)=10.3046875
    path.lineTo(sX(currentX), sY(currentY))
    path.close()

    return MarkerIcon(
        fillColor = fillColor,
        strokeColor = strokeColor,
        strokeWidth = strokeWidth,
        scale = 1f,
        anchor = Offset(0.5f, 0.5f),
        size = Size(32f, 32f),
        infoAnchor = Offset(0.5f, 0.5f),
        strokePath = path,
    )
}
