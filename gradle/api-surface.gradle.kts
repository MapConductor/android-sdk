// 公開 API サーフェスのスナップショット。
//
// ドライバー層の共通化中、アプリ開発者向けの公開 API を凍結し続けるための門番。
// `binary-compatibility-validator` は com.android.library モジュールに対応していない
// （apiDump タスクが 1 つも生成されない）ため、release AAR の classes.jar を javap で
// 読んでテキスト化する方式にしている。差分が git 上で人間に読める形で出るのが利点。
//
//   ./gradlew apiDump    ベースラインを <module>/api/<module>.api へ書き出す
//   ./gradlew apiCheck   ベースラインとの差分があれば失敗する
//
// 除外するもの（いずれも Kotlin コンパイラの生成物で、公開 API ではない）:
//   - 無名クラス / ラムダ / $WhenMappings / ComposableSingletons / Compose の $stable
//   - `access$…`（合成アクセサ）、`…$module_name()`（internal のマングル名）、
//     `…$default(…)`（デフォルト引数のブリッジ）
//
// $DefaultImpls は残す。Java から見える ABI であり、interface にデフォルト実装を
// 足したことが差分として見えるのは意図どおり（追加は非破壊なのでレビューで承認する）。

val excludedClassPatterns =
    listOf(
        // raw string 内で `$` はテンプレート展開されるため、文字クラス `[$]` で書く。
        Regex("""[$]\d+$"""),
        Regex("""[$][$]"""),
        Regex("""[$]WhenMappings$"""),
        Regex("""(^|\.)ComposableSingletons"""),
    )

// 行頭の空白に続くメンバー宣言のうち、識別子に `$` を含むもの。
val syntheticMemberPattern = Regex("""^\s+.*[A-Za-z0-9][$][A-Za-z0-9_]+\(""")

// ドライバー実装点であることを示す注釈の JVM 記述子。
val INTERNAL_API_DESCRIPTOR = "Lcom/mapconductor/core/InternalMapConductorApi;"

/** バイト列の部分一致。定数プールに注釈の記述子が入っているかを見るために使う。 */
fun ByteArray.indexOfSubList(needle: ByteArray): Int {
    if (needle.isEmpty() || needle.size > size) return -1
    outer@ for (i in 0..(size - needle.size)) {
        for (j in needle.indices) {
            if (this[i + j] != needle[j]) continue@outer
        }
        return i
    }
    return -1
}

fun Project.apiBaselineFile(): File = layout.projectDirectory.file("api/$name.api").asFile

fun Project.releaseAar(): File =
    layout.buildDirectory
        .file("outputs/aar/$name-release.aar")
        .get()
        .asFile

/** release AAR の classes.jar を javap にかけ、正規化したテキストを返す。 */
fun Project.generateApiSurface(): String {
    val aar = releaseAar()
    require(aar.isFile) { "release AAR が見つかりません: $aar（先に assembleRelease が必要）" }

    val work = File.createTempFile("api-surface-", "-dir").apply {
        delete()
        mkdirs()
    }
    try {
        val classesJar = File(work, "classes.jar")
        java.util.zip.ZipFile(aar).use { zip ->
            val entry =
                zip.getEntry("classes.jar")
                    ?: error("classes.jar が AAR に含まれていません: $aar")
            zip.getInputStream(entry).use { input ->
                classesJar.outputStream().use { input.copyTo(it) }
            }
        }

        val classNames =
            java.util.zip.ZipFile(classesJar).use { zip ->
                zip.entries()
                    .toList()
                    .filter { it.name.endsWith(".class") }
                    .filterNot { entry ->
                        // @InternalMapConductorApi が付いた型は「ドライバー実装点」であって
                        // アプリ開発者向けの公開 API ではないので、凍結の対象外にする。
                        // 注釈は BINARY retention なので、適用したクラスの定数プールに
                        // 記述子が入る。単に注釈付き API を呼ぶだけのクラスには入らない。
                        //
                        // 既知の限界: 判定はクラス単位。メンバー単位で注釈を付けても、
                        // そのクラス全体が対象外になる。
                        zip.getInputStream(entry).use { input ->
                            input.readBytes().let { bytes ->
                                INTERNAL_API_DESCRIPTOR.toByteArray(Charsets.UTF_8).let { needle ->
                                    bytes.indexOfSubList(needle) >= 0
                                }
                            }
                        }
                    }.map { it.name.removeSuffix(".class").replace('/', '.') }
                    .filterNot { name -> excludedClassPatterns.any { it.containsMatchIn(name) } }
                    .sorted()
            }
        if (classNames.isEmpty()) return ""

        val javap = File(System.getProperty("java.home"), "bin/javap")
        // javap は javac と違い @argfile を解釈しないので、クラス名は引数で渡す。
        // コマンドライン長の上限に当たらないよう分割して実行する。
        //
        // javap は依存クラスが classpath に無いと警告を出して exit=1 になるが、
        // 見つかったクラスのシグネチャは正常に出力する。AAR 単体を読む以上これは常態
        // なので、出力が空のときだけ失敗として扱う。stderr はパイプ詰まりを避けて
        // ファイルへ逃がす。
        val errFile = File(work, "javap.err")
        val output =
            classNames.chunked(150).joinToString("") { chunk ->
                val process =
                    ProcessBuilder(
                        listOf(
                            javap.absolutePath,
                            "-protected",
                            "-classpath",
                            classesJar.absolutePath,
                        ) + chunk,
                    ).redirectError(errFile).start()
                val chunkOutput = process.inputStream.bufferedReader().readText()
                val exit = process.waitFor()
                if (chunkOutput.isBlank()) {
                    val stderr =
                        errFile.takeIf { it.isFile }?.readText().orEmpty()
                            .lines().take(10).joinToString("\n")
                    error("javap が出力を返しませんでした (exit=$exit): $name\n$stderr")
                }
                chunkOutput
            }

        return output
            .lineSequence()
            .filterNot { it.startsWith("Compiled from ") }
            .filterNot { it.contains("\$stable") }
            .filterNot { syntheticMemberPattern.containsMatchIn(it) }
            .filter { it.isNotBlank() }
            .joinToString("\n", postfix = "\n")
    } finally {
        work.deleteRecursively()
    }
}

subprojects {
    // 公開されるライブラリモジュールだけを対象にする（サンプルアプリと BOM は除く）。
    plugins.withId("com.android.library") {
        tasks.register("apiDump") {
            group = "verification"
            description = "公開 API サーフェスのベースラインを api/${project.name}.api へ書き出す"
            dependsOn("assembleRelease")
            doLast {
                val file = apiBaselineFile()
                file.parentFile.mkdirs()
                file.writeText(generateApiSurface())
                logger.lifecycle("apiDump: ${file.relativeTo(rootDir)} (${file.readLines().size} 行)")
            }
        }

        tasks.register("apiCheck") {
            group = "verification"
            description = "公開 API サーフェスがベースラインから変化していないか検証する"
            dependsOn("assembleRelease")
            doLast {
                val baseline = apiBaselineFile()
                if (!baseline.isFile) {
                    throw GradleException(
                        "API ベースラインがありません: ${baseline.relativeTo(rootDir)}\n" +
                            "先に ./gradlew :${project.name}:apiDump を実行してコミットしてください。",
                    )
                }
                val actual = generateApiSurface()
                val expected = baseline.readText()
                if (actual != expected) {
                    val actualFile =
                        layout.buildDirectory
                            .file("api/${project.name}.api")
                            .get()
                            .asFile
                            .apply {
                                parentFile.mkdirs()
                                writeText(actual)
                            }
                    val expectedLines = expected.lines().toSet()
                    val actualLines = actual.lines().toSet()
                    val removed = (expectedLines - actualLines).filter { it.isNotBlank() }
                    val added = (actualLines - expectedLines).filter { it.isNotBlank() }
                    throw GradleException(
                        buildString {
                            appendLine("公開 API が変化しています: ${project.name}")
                            appendLine("  ベースライン: ${baseline.relativeTo(rootDir)}")
                            appendLine("  実際:         ${actualFile.relativeTo(rootDir)}")
                            if (removed.isNotEmpty()) {
                                appendLine("  --- 削除/変更された宣言 (${removed.size}) ---")
                                removed.take(30).forEach { appendLine("  - ${it.trim()}") }
                                if (removed.size > 30) appendLine("  … 他 ${removed.size - 30} 件")
                            }
                            if (added.isNotEmpty()) {
                                appendLine("  --- 追加された宣言 (${added.size}) ---")
                                added.take(30).forEach { appendLine("  + ${it.trim()}") }
                                if (added.size > 30) appendLine("  … 他 ${added.size - 30} 件")
                            }
                            appendLine("意図した変更なら ./gradlew :${project.name}:apiDump で更新してください。")
                        },
                    )
                }
                logger.lifecycle("apiCheck: ${project.name} は変化なし")
            }
        }
    }
}

// 集約タスク。サブプロジェクトはこの時点でまだ評価されていないので、
// タスク名でのマッチング（遅延評価）で拾う。
tasks.register("apiDump") {
    group = "verification"
    description = "全ライブラリモジュールの公開 API ベースラインを書き出す"
    dependsOn(subprojects.map { sub -> sub.tasks.matching { it.name == "apiDump" } })
}

tasks.register("apiCheck") {
    group = "verification"
    description = "全ライブラリモジュールの公開 API を検証する"
    dependsOn(subprojects.map { sub -> sub.tasks.matching { it.name == "apiCheck" } })
}
