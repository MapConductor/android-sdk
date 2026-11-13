package com.mapconductor.example

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class StarCircleView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.scale(10f, 10f)

        // 背景：黄色い円 (#FFD400), 半径12px, 中心(14, 14)
        paint.color = Color.parseColor("#FFD400")
        paint.style = Paint.Style.FILL
        canvas.drawCircle(14f, 14f, 12f, paint)

        // 五芒星座標（SVGの polygon → scale(0.91), translate(1.26, 0.26) を適用済み）
        val starPath = Path()

        // スケーリングと平行移動を反映した点列（直接数値を適用）
        val points =
            arrayOf(
                Pair(14f, 4f),
                Pair(16.94f, 11.4f),
                Pair(24.9f, 11.4f),
                Pair(18.48f, 16.1f),
                Pair(21.42f, 23.5f),
                Pair(14f, 18.8f),
                Pair(6.58f, 23.5f),
                Pair(9.52f, 16.1f),
                Pair(3.1f, 11.4f),
                Pair(11.06f, 11.4f),
            )

        // 変換処理：scale(0.91) → x*0.91, y*0.91 ／ translate(1.26, 0.26)
        val transformed =
            points.map { (x, y) ->
                val scaledX = x * 0.91f + 1.26f
                val scaledY = y * 0.91f + 0.26f
                Pair(scaledX, scaledY)
            }

        // Path構築（moveTo → lineTo）
        starPath.moveTo(transformed[0].first, transformed[0].second)
        for (i in 1 until transformed.size) {
            starPath.lineTo(transformed[i].first, transformed[i].second)
        }
        starPath.close()

        // 星を白で塗りつぶし
        paint.color = Color.WHITE
        canvas.drawPath(starPath, paint)

        canvas.restore()
    }
}
