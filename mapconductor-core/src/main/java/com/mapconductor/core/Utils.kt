package com.mapconductor.core

import kotlin.time.Duration
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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
