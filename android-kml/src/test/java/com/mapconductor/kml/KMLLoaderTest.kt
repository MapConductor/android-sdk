package com.mapconductor.kml

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.FileNotFoundException
import java.io.InputStream
import kotlinx.coroutines.runBlocking

class KMLLoaderTest {
    private fun kmlWithPoint(
        name: String,
        links: List<String> = emptyList(),
    ): String {
        val linkTags =
            links.joinToString("") {
                "<NetworkLink><Link><href>$it</href></Link></NetworkLink>"
            }
        return """<?xml version="1.0"?><kml><Document>
            $linkTags
            <Placemark><name>$name</name><Point><coordinates>1,2</coordinates></Point></Placemark>
            </Document></kml>"""
    }

    private fun loaderFor(
        documents: Map<String, String>,
        maxDocuments: Int = KMLLoader.DEFAULT_MAX_DOCUMENTS,
        onDocumentError: ((String, Throwable) -> Unit)? = null,
        fetchLog: MutableList<String> = ArrayList(),
    ): Pair<KMLLoader, MutableList<String>> {
        val loader =
            KMLLoader(
                maxDocuments = maxDocuments,
                onDocumentError = onDocumentError,
                fetch = { url ->
                    fetchLog.add(url)
                    documents[url]?.byteInputStream() ?: throw FileNotFoundException(url)
                },
            )
        return loader to fetchLog
    }

    private fun names(features: List<KMLFeature>): List<String> = features.map { it.properties["name"] as String }

    @Test
    fun followsAbsoluteAndRelativeNetworkLinks() {
        val (loader, _) =
            loaderFor(
                mapOf(
                    "https://example.com/maps/root.kml" to
                        kmlWithPoint("root", links = listOf("sub/child.kml", "https://other.com/abs.kml")),
                    "https://example.com/maps/sub/child.kml" to kmlWithPoint("child"),
                    "https://other.com/abs.kml" to kmlWithPoint("abs"),
                ),
            )

        val features = runBlocking { loader.load("https://example.com/maps/root.kml") }

        assertEquals(listOf("root", "child", "abs"), names(features))
    }

    @Test
    fun cyclicLinksAreFetchedOnlyOnce() {
        val (loader, fetchLog) =
            loaderFor(
                mapOf(
                    "https://example.com/a.kml" to
                        kmlWithPoint("a", links = listOf("https://example.com/b.kml")),
                    "https://example.com/b.kml" to
                        kmlWithPoint("b", links = listOf("https://example.com/a.kml")),
                ),
            )

        val features = runBlocking { loader.load("https://example.com/a.kml") }

        assertEquals(listOf("a", "b"), names(features))
        assertEquals(2, fetchLog.size)
    }

    @Test
    fun maxDocumentsCapsTheChain() {
        val chain = HashMap<String, String>()
        for (i in 0 until 10) {
            chain["https://example.com/$i.kml"] =
                kmlWithPoint("doc$i", links = listOf("https://example.com/${i + 1}.kml"))
        }
        val (loader, _) = loaderFor(chain, maxDocuments = 3)

        val features = runBlocking { loader.load("https://example.com/0.kml") }

        assertEquals(listOf("doc0", "doc1", "doc2"), names(features))
    }

    @Test
    fun failedLinkIsSkippedAndReported() {
        val errors = ArrayList<String>()
        val (loader, _) =
            loaderFor(
                mapOf(
                    "https://example.com/root.kml" to
                        kmlWithPoint(
                            "root",
                            links = listOf("https://example.com/missing.kml", "https://example.com/ok.kml"),
                        ),
                    "https://example.com/ok.kml" to kmlWithPoint("ok"),
                ),
                onDocumentError = { url, _ -> errors.add(url) },
            )

        val features = runBlocking { loader.load("https://example.com/root.kml") }

        assertEquals(listOf("root", "ok"), names(features))
        assertEquals(listOf("https://example.com/missing.kml"), errors)
    }

    @Test
    fun streamOverloadResolvesRelativeLinksAgainstBaseUrl() {
        val (loader, fetchLog) =
            loaderFor(
                mapOf("https://example.com/data/child.kml" to kmlWithPoint("child")),
            )
        val root: InputStream = kmlWithPoint("root", links = listOf("child.kml")).byteInputStream()

        val features = runBlocking { loader.load(root, baseUrl = "https://example.com/data/root.kml") }

        assertEquals(listOf("root", "child"), names(features))
        assertEquals(listOf("https://example.com/data/child.kml"), fetchLog)
    }

    @Test
    fun streamOverloadWithoutBaseUrlSkipsRelativeLinks() {
        val (loader, fetchLog) =
            loaderFor(
                mapOf("https://other.com/abs.kml" to kmlWithPoint("abs")),
            )
        val root: InputStream =
            kmlWithPoint("root", links = listOf("child.kml", "https://other.com/abs.kml")).byteInputStream()

        val features = runBlocking { loader.load(root) }

        assertEquals(listOf("root", "abs"), names(features))
        assertEquals(listOf("https://other.com/abs.kml"), fetchLog)
    }

    @Test
    fun resolveHrefHandlesAbsoluteRelativeAndMissingBase() {
        assertEquals(
            "https://a.com/x.kml",
            KMLLoader.resolveHref("https://b.com/base.kml", "https://a.com/x.kml"),
        )
        assertEquals(
            "https://b.com/dir/x.kml",
            KMLLoader.resolveHref("https://b.com/dir/base.kml", "x.kml"),
        )
        assertEquals(
            "https://b.com/x.kml",
            KMLLoader.resolveHref("https://b.com/dir/base.kml", "/x.kml"),
        )
        assertNull(KMLLoader.resolveHref(null, "x.kml"))
    }
}
