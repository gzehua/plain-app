plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    kotlin("plugin.parcelize")
}

// Dev-only single-target mode: pass -PenableDeviceTarget=false to skip
// configuring iosArm64 (dependency resolution, KSP, task graph) when only
// building for the simulator. Saves ~3-5s of configuration overhead per
// build. Restore by omitting the flag (default: both targets enabled).
val enableDeviceTarget = (findProperty("enableDeviceTarget") as? String)?.toBoolean() ?: true
val webBundleDir = providers.gradleProperty("plainWebDist")
    .orElse(providers.environmentVariable("PLAIN_WEB_DIST"))
    .orNull?.let { rootProject.file(it) }

val cargoBinDir = File(System.getenv("CARGO_HOME") ?: "${System.getProperty("user.home")}/.cargo", "bin")
val rustupExe = cargoBinDir.resolve("rustup")
val cargoExe = cargoBinDir.resolve("cargo")
val localCoreOverride = rootProject.file(".cargo/core-dependency-state").exists()
listOf(rustupExe, cargoExe).forEach {
    require(it.canExecute()) { "Rust tool not found or not executable: ${it.absolutePath}" }
}

kotlin {
    jvmToolchain(17)

    android {
        namespace = "com.ismartcoding.plain.shared"
        compileSdk = 37
        minSdk = 28

        // Required so that Compose Multiplatform resources (drawable/strings/etc.)
        // are packaged into the consuming Android app's assets. Without this the
        // new `com.android.kotlin.multiplatform.library` plugin disables Android
        // resource processing and `copyAndroidMainComposeResourcesToAndroidAssets`
        // is never wired up.
        androidResources {
            enable = true
        }

        // Enable Android host-side unit tests so `commonTest` (and any
        // androidUnitTest-specific sources) get compiled and runnable via
        // `testDebugUnitTest` / `testReleaseUnitTest`. Without this the
        // plugin silently ignores both source sets and `:shared:commonTest`
        // emits a warning on every build.
        withHostTest {}
    }

    // Dev-only single-target mode: see enableDeviceTarget above.
    val iosTargets = mutableListOf<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>()
    if (enableDeviceTarget) iosTargets.add(iosArm64())
    iosTargets.add(iosSimulatorArm64())
    // Skip DWARF debug-info generation: measured ~50% faster incremental iOS
    // dev cycles (2m17s -> 1m10s for a single-line change; the K/N backend
    // emits debug info at link time). Trade-off: no source-level Kotlin
    // debugging in lldb and no line numbers in debug-build crash traces.
    // Restore with -PkotlinDebuggable=true when you need to debug Kotlin.
    val kotlinDebuggable = (findProperty("kotlinDebuggable") as? String)?.toBoolean() ?: false
    iosTargets.forEach { iosTarget ->
        val rustTarget = if (iosTarget.name == "iosArm64") "aarch64-apple-ios" else "aarch64-apple-ios-sim"
        val rustTargetDir = layout.buildDirectory.dir("rust-target")
        val onnxRuntimeDir = layout.buildDirectory.dir("onnxruntime/$rustTarget")
        val installRustTarget = tasks.register<Exec>("installPlainRustTarget${iosTarget.name.replaceFirstChar { it.uppercase() }}") {
            commandLine(rustupExe.absolutePath, "target", "add", rustTarget)
        }
        val buildPlainRust = tasks.register<Exec>("buildPlainRust${iosTarget.name.replaceFirstChar { it.uppercase() }}") {
            dependsOn(installRustTarget)
            inputs.file(rootProject.file("plain-rs/Cargo.toml"))
            inputs.file(rootProject.file("plain-rs/Cargo.lock"))
            inputs.dir(rootProject.file("plain-rs/src"))
            outputs.file(rustTargetDir.map { it.file("$rustTarget/release/libplain_rust.a") })
            webBundleDir?.let {
                inputs.dir(it).withPropertyName("webBundle")
                environment("PLAIN_WEB_DIST", it.absolutePath)
            }
            environment("CARGO_TARGET_DIR", rustTargetDir.get().asFile.absolutePath)
            environment("PLAIN_ONNX_RUNTIME_DIR", onnxRuntimeDir.get().asFile.absolutePath)
            // iOS frameworks consume only the static archive.
            commandLine(listOfNotNull(cargoExe.absolutePath, "rustc", "--locked".takeUnless { localCoreOverride }, "--manifest-path", rootProject.file("plain-rs/Cargo.toml"), "--lib", "--crate-type", "staticlib", "--release", "--target", rustTarget))
        }
        iosTarget.compilations.getByName("main").cinterops.create("plainPrefs") {
            defFile(rootProject.file("shared/src/nativeInterop/cinterop/plainPrefs.def"))
            compilerOpts("-I${rootProject.file("plain-rs/include").absolutePath}")
        }
        tasks.matching { it.name == "cinteropPlainPrefs${iosTarget.name.replaceFirstChar { c -> c.uppercase() }}" }.configureEach {
            inputs.file(rootProject.file("plain-rs/include/plain_rust.h"))
        }
        iosTarget.binaries.framework {
            baseName = "PlainShared"
            isStatic = true
            debuggable = kotlinDebuggable
            linkerOpts("-L${rustTargetDir.get().asFile.absolutePath}/$rustTarget/release", "-lplain_rust", "-L${onnxRuntimeDir.get().asFile.absolutePath}", "-lonnxruntime", "-lc++", "-framework", "Foundation", "-framework", "CoreML", "-framework", "Accelerate")
        }
        tasks.matching { it.name.startsWith("link") && it.name.contains(iosTarget.name.replaceFirstChar { c -> c.uppercase() }) }.configureEach {
            dependsOn(buildPlainRust)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":plain-common"))
            api(libs.atomicfu)
            api(libs.kotlinx.serialization.json)
            api(libs.ktor.http)
            api(libs.coil)

            api(project(":plain-ui"))
            implementation(libs.runtime)
            implementation(libs.foundation)
            implementation(libs.material3)
            implementation(libs.ui)
            api("org.jetbrains.compose.components:components-resources:1.11.1")
            implementation(libs.navigation.compose)
            implementation(libs.jetbrains.lifecycle.viewmodel.compose)
            implementation(libs.compose.lifecycle.runtime)
            implementation(libs.kotlinx.datetime)
            // ktor-io only (ByteReadChannel streaming) — no HTTP client dependency from ktor
            implementation(libs.ktor.io)
            implementation(libs.coil.compose)

            // Vendored multiplatform-markdown-renderer (lib/markdown/) depends on these.
            api(libs.jetbrains.markdown)
            api(libs.kotlinx.collections.immutable)
            api(libs.kotlinx.coroutines.core)

            // LaTeX math rendering (KMP — Compose Multiplatform renderer)
            api(libs.latex.base)
            api(libs.latex.parser)
            api(libs.latex.renderer)
        }
        androidMain.dependencies {
            api(libs.androidx.core.ktx)
            implementation(libs.zxing.core)

            implementation(libs.okhttp)

            // Offline phone number metadata: region, number type, carrier, geo description

            implementation(libs.androidx.exifinterface)
            implementation(libs.androidx.appcompat)
            implementation(libs.coil)
            implementation(libs.coil.svg)
            implementation(libs.coil.gif)
            implementation(libs.coil.video)
            implementation(libs.coil.network.okhttp)

            // Shizuku (shell-uid privileged ops for SMS deletion)
            implementation(libs.shizuku.api)
            implementation(libs.shizuku.provider)
            implementation(libs.shizuku.aidl)

            // Vendored libraries (lib/) dependencies
            implementation(libs.pdfium.android)


            // Media3 (AudioPlayerService, audio playback, PlayerView UI)
            implementation(libs.media3.exoplayer)
            implementation(libs.media3.datasource)
            implementation(libs.media3.session)
            implementation(libs.media3.ui)
            implementation(libs.media3.ui.compose)
            implementation(libs.media3.dash)
            implementation(libs.media3.hls)

            // LifecycleService (HttpServerService)
            implementation(libs.androidx.lifecycle.service)

            // WorkManager (FeedFetchWorker, ImageEmbedWorker)
            implementation(libs.androidx.work.runtime.ktx)

            // Splash screen (MainActivity)
            implementation(libs.androidx.core.splashscreen)

            // Activity Compose (LocalActivity)
            implementation(libs.compose.activity)

        }
        iosMain.dependencies {
            implementation(libs.coil)
            implementation(libs.coil.network.core)
        }
        getByName("commonTest").kotlin.srcDirs("src/testFixtures/kotlin", "../plain-common/src/testFixtures/kotlin")
        getByName("androidHostTest").kotlin.srcDir("src/androidTestFixtures/kotlin")
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

dependencies {
    add("androidHostTestImplementation", kotlin("test"))
    add("androidHostTestImplementation", libs.junit)
    add("androidHostTestImplementation", libs.tink.android)
}

compose.resources {
    packageOfResClass = "com.ismartcoding.plain.i18n"
    generateResClass = always
    publicResClass = true
}

// ============================================================================
// Compile-time validation: detect duplicate string resource keys.
//
// Compose Resources treats all strings*.xml files in the same values/ or
// values-{lang}/ directory as a SINGLE namespace. A key appearing in more than
// one file within the same locale causes a runtime crash:
//   "Resource with ID='string:X' has more than one file"
// This task fails the build at compile time so the issue is caught before any
// device/emulator run. Also detects within-file duplicates.
// ============================================================================
val checkStringResourceDuplicates by tasks.registering {
    group = "verification"
    description = "Checks for duplicate string resource keys within each locale directory."

    val resourcesDir = layout.projectDirectory.dir("src/commonMain/composeResources")
    inputs.dir(resourcesDir).withPropertyName("composeResources")

    doLast {
        val keyPattern = Regex("""<string\s+name="([^"]+)"""")
        val errors = mutableListOf<String>()

        resourcesDir.asFile.listFiles()?.sortedBy { it.name }?.forEach { localeDir ->
            if (!localeDir.isDirectory) return@forEach
            val name = localeDir.name
            if (name != "values" && !name.startsWith("values-")) return@forEach

            // key -> set of files containing it
            val keyToFiles = mutableMapOf<String, MutableMap<String, Int>>()
            localeDir.listFiles { f -> f.isFile && f.name.startsWith("strings") && f.name.endsWith(".xml") }
                ?.sortedBy { it.name }
                ?.forEach { xmlFile ->
                    val content = xmlFile.readText()
                    keyPattern.findAll(content).forEach { match ->
                        val fileMap = keyToFiles.getOrPut(match.groupValues[1]) { mutableMapOf() }
                        fileMap[xmlFile.name] = (fileMap[xmlFile.name] ?: 0) + 1
                    }
                }

            keyToFiles.forEach { (key, fileCounts) ->
                val totalOccurrences = fileCounts.values.sum()
                val distinctFiles = fileCounts.keys
                // Cross-file duplicate (the crash scenario)
                if (distinctFiles.size > 1) {
                    errors.add("  $name/ key='$key' appears in multiple files: ${distinctFiles.sorted().joinToString(", ")}")
                }
                // Within-file duplicate (same key twice in one file)
                fileCounts.forEach { (file, count) ->
                    if (count > 1) {
                        errors.add("  $name/$file key='$key' appears $count times within the same file")
                    }
                }
            }
        }

        if (errors.isNotEmpty()) {
            throw GradleException(
                buildString {
                    appendLine("Found ${errors.size} duplicate string resource key(s) in Compose Resources.")
                    appendLine("Compose Resources does not allow the same key in multiple strings*.xml files")
                    appendLine("within the same locale directory, nor the same key twice in one file.")
                    appendLine("Fix by removing the duplicate key from all but one location.")
                    appendLine()
                    errors.forEach { appendLine(it) }
                }
            )
        }
    }
}

// Run the check before Compose Resources are processed and as part of `check`.
// This ensures the build fails at compile time, not at runtime.
tasks.matching { it.name.contains("ComposeResources", ignoreCase = true) }.configureEach {
    dependsOn(checkStringResourceDuplicates)
}
tasks.named("check").configure { dependsOn(checkStringResourceDuplicates) }

val checkJava21Collisions by tasks.registering {
    group = "verification"
    description = "Rejects Java 21 collection member calls in androidMain (unavailable below API 35)"
    val srcRoot = layout.projectDirectory.dir("src/androidMain")
    inputs.dir(srcRoot)
    doLast {
        val forbidden = Regex(
            """\.(removeLast|removeFirst|getFirst|getLast|addFirst|addLast|reversed|putFirst|putLast|firstEntry|lastEntry|pollFirstEntry|pollLastEntry|sequencedKeySet|sequencedValues|sequencedEntry)\("""
        )
        val offenders = mutableListOf<String>()
        srcRoot.asFile.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .forEach { f ->
                val source = f.readText()
                val blockingDeques = Regex("""\b(?:val|var)\s+(\w+)\s*=\s*(?:java\.util\.concurrent\.)?LinkedBlockingDeque\b""")
                    .findAll(source).map { it.groupValues[1] }.toSet()
                    .takeIf { source.contains("import java.util.concurrent.LinkedBlockingDeque") || source.contains("java.util.concurrent.LinkedBlockingDeque") }
                    .orEmpty()
                val dequeMethods = setOf("removeLast", "removeFirst", "getFirst", "getLast", "addFirst", "addLast", "putFirst", "putLast")
                source.lineSequence().forEachIndexed { i, line ->
                    if (line.contains("LockFreeLinkedListHead")) return@forEachIndexed
                    val invalid = forbidden.findAll(line).any { match ->
                        val receiver = line.substring(0, match.range.first).takeLastWhile { it.isLetterOrDigit() || it == '_' }
                        receiver !in blockingDeques || match.groupValues[1] !in dequeMethods
                    }
                    if (invalid) offenders += "${f.relativeTo(srcRoot.asFile)}:${i + 1}: ${line.trim()}"
                }
            }
        if (offenders.isNotEmpty()) {
            throw GradleException(
                "Java 21-only collection calls found (API 35+, minSdk is 28):\n" +
                    offenders.joinToString("\n") +
                    "\nUse removeAt(index) / first() / last() / add(0, e) / asReversed() instead."
            )
        }
    }
}

tasks.matching {
    it.name.startsWith("compile") && it.name.contains("Android", ignoreCase = true)
}.configureEach { dependsOn(checkJava21Collisions) }
