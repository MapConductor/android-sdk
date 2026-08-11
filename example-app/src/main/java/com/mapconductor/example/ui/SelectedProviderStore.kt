package com.mapconductor.example.ui

/**
 * 直近に選んだ地図プロバイダ。**ページをまたいで引き継ぐ**ための入れ物。
 *
 * ## なぜ要るのか
 *
 * 各ページの [DemoMapPageScaffold] は選択状態を `rememberSaveable` で持つが、それは
 * そのページの composable の寿命に紐づく。ページを移ると新しい composable になるので、
 * 毎回いちばん左のプロバイダに戻ってしまう。react-sdk はプロバイダの地図インスタンスを
 * シングルトンで持っているので既に引き継がれていて、**android と iOS だけが揃っていなかった**。
 *
 * ## 索引ではなくキーで持つ
 *
 * ページによって並びが違う（Camera Sync のように独自の一覧を出す画面がある）。
 * 索引で持つと別のプロバイダが選ばれてしまうので、`"maplibre"` のような
 * [IconItem.key] で持って、ページごとにその場の一覧から引き当てる。
 *
 * ## 永続化しない
 *
 * プロセス内だけ。アプリを再起動したら既定へ戻る。サンプルアプリなので
 * 「前回の続き」より「毎回同じ状態から始められる」ほうが都合がよい。
 */
object SelectedProviderStore {
    /** 直近に選ばれたプロバイダのキー。まだ何も選ばれていなければ null。 */
    var key: String? = null
        private set

    fun remember(key: String) {
        this.key = key
    }

    /**
     * [menuItems] の中から、引き継ぐべき索引を返す。
     *
     * 覚えているキーがこの一覧に無ければ（そのページが出さないプロバイダなら）
     * [fallback] を返す。
     *
     * 大文字小文字は無視する。`--es provider MapLibre` のように起動引数から
     * 入ってきたキーも引き当てられるようにするため。
     */
    fun indexIn(
        menuItems: List<IconItem<*>>,
        fallback: Int,
    ): Int {
        val remembered = key ?: return fallback
        val index = menuItems.indexOfFirst { it.key.equals(remembered, ignoreCase = true) }
        return index.takeIf { it >= 0 } ?: fallback
    }
}
