package com.mapconductor.kml

/**
 * `<NetworkLink>` が参照する外部 KML/KMZ ドキュメント。
 *
 * [href] は `<Link>`（KML 2.0 の旧名 `<Url>`）配下の `<href>` の値そのままで、
 * 相対参照の解決は行っていない。[KMLLoader] が読み込み元 URL に対して解決する。
 */
class KMLNetworkLink(
    val href: String,
    val visibility: Boolean = true,
)

/**
 * [KMLParser.parseDocument] の結果。描画可能な [features] に加えて、
 * まだ取得していない外部参照 [networkLinks] を保持する。
 *
 * [KMLParser.parse] は [features] だけを返す従来 API。リンク先まで合流させた
 * リストが欲しい場合は [KMLLoader] を使う。
 */
class KMLDocument(
    val features: List<KMLFeature>,
    val networkLinks: List<KMLNetworkLink> = emptyList(),
)
