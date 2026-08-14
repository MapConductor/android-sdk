package com.mapconductor.template

import com.mapconductor.compose.MapViewScope

/**
 * 拡張モジュール（ヒートマップ / GeoJSON レイヤ / マーカークラスタリング）が
 * オーバーレイを差し込むためのスコープ。
 *
 * 中身はコアの [MapViewScope] が全部持っているので、ドライバーは
 * **この 3 行を書くだけ**でよい。その地図SDKにしかない機能を足したいときだけ
 * ここにメンバーを増やす。
 */
class TemplateMapViewScope : MapViewScope()
