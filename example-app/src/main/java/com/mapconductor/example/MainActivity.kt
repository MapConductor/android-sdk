package com.mapconductor.example

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mapconductor.example.ui.SelectedProviderStore
import com.mapconductor.example.ui.theme.AppTheme
import android.os.Bundle

class MainActivity : ComponentActivity() {
    companion object {
        /** Gesture preset requested via `--es gestures none|all`, for UI test runs. */
        var gesturesExtra: String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gesturesExtra = intent.getStringExtra("gestures")

        // `--es provider maptiler|longdo|...` は**開始時のプロバイダ**の指定。
        // 覚えている選択として置くだけにして、あとはユーザーが選んだときと同じ扱いにする。
        //
        // 以前はこれを静的な値として持ち、各ページが毎回参照していた。すると
        // adb から起動したプロセスでは**プロバイダを選び直してもページを移るたびに
        // 指定のプロバイダへ戻ってしまう**（指定が起動時ではなく常時の上書きになる）。
        intent.getStringExtra("provider")?.let { SelectedProviderStore.remember(it) }
        enableEdgeToEdge()

        setContent {
            AppTheme {
                DemoAppScreen(
                    initPage = intent.getStringExtra("page") ?: "map-basic",
//                    initPage = "startup",
                )
            }
        }
    }
}
