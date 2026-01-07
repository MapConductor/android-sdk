package com.mapconductor.core

import android.util.Log
import androidx.compose.ui.geometry.Offset
import com.mapconductor.core.features.GeoPoint
import com.mapconductor.core.features.GeoPointInterface
import kotlin.time.Duration
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

fun closestPointOnSegment(
    startPoint: Offset,
    endPoint: Offset,
    testPoint: Offset,
): Offset {
    val segmentVector = Offset(endPoint.x - startPoint.x, endPoint.y - startPoint.y)
    val pointVector = Offset(testPoint.x - startPoint.x, testPoint.y - startPoint.y)
    val segmentLengthSquared = segmentVector.x * segmentVector.x + segmentVector.y * segmentVector.y
    if (segmentLengthSquared == 0.0f) return startPoint // AとBが同じ点

    // 内積で射影係数 projectionRatio を求める (0 ≤ projectionRatio ≤ 1)
    val projectionRatio =
        ((pointVector.x * segmentVector.x + pointVector.y * segmentVector.y) / segmentLengthSquared)
            .coerceIn(0.0f, 1.0f)

    return Offset(startPoint.x + projectionRatio * segmentVector.x, startPoint.y + projectionRatio * segmentVector.y)
}

fun printPoints(
    tag: String,
    points: List<GeoPointInterface>,
) {
    Log.d(tag, "-----------")
    points.forEach { point ->
        Log.d(tag, GeoPoint.from(point).toUrlValue())
    }
}

/**
 * 指定時間の無入力でバーストを確定し、まとめて List で流す。
 * 例: window=300ms の間に来た値を 1 バッチとして emit。
 */
@OptIn(FlowPreview::class)
internal fun <T> Flow<T>.debounceBatch(
    window: Duration,
    maxSize: Int,
): Flow<List<T>> =
    channelFlow {
        require(maxSize > 0) { "maxSize must be > 0" }

        val acc = ArrayList<T>(maxSize)
        val lock = Mutex()

        // イベント発生通知用（タイマ用）ホットストリーム
        val activity = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)

        suspend fun flushIfNotEmpty() {
            val batch: List<T>? =
                lock.withLock {
                    if (acc.isEmpty()) null else acc.toList().also { acc.clear() }
                }
            if (batch != null) send(batch)
        }

        // 上流の値を取り込み、maxSize 到達なら即フラッシュ
        val collectorJob =
            launch {
                try {
                    this@debounceBatch.collect { v ->
                        var shouldFlushNow = false
                        lock.withLock {
                            acc.add(v)
                            if (acc.size >= maxSize) {
                                shouldFlushNow = true
                            }
                        }
                        if (shouldFlushNow) {
                            // 最大件数に達したので即フラッシュ
                            flushIfNotEmpty()
                        } else {
                            // タイマ更新用の「活動通知」
                            activity.tryEmit(Unit)
                        }
                    }
                } finally {
                    // 上流が完了したら残りを流して終了
                    flushIfNotEmpty()
                }
            }

        // 「静寂境界」：window の間新規通知が来なければフラッシュ
        val timerJob =
            launch {
                activity
                    .debounce(window)
                    .collect { flushIfNotEmpty() }
            }

        // channelFlow は子 Job の完了まで開いている
        collectorJob.join()
        timerJob.cancel()
    }
