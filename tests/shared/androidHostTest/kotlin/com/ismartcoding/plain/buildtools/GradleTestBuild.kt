package com.ismartcoding.plain.buildtools

import java.io.File
import java.nio.file.Files
import java.util.Properties
import java.util.concurrent.TimeUnit

internal fun buildTestRepository(): File = generateSequence(File("").absoluteFile) { it.parentFile }
    .first { File(it, "settings.gradle.kts").isFile }

internal class GradleTestBuild : AutoCloseable {
    private val repository = buildTestRepository()
    private val temporary = Files.createTempDirectory("plain-gradle-test-").toFile()
    private val init = File(temporary, "build-check.init.gradle.kts").apply {
        writeText(checkNotNull(GradleTestBuild::class.java.getResource("/build-check.init.gradle.kts")).readText())
    }

    fun run(task: String, vararg arguments: String): Properties {
        val output = File(temporary, "plan.properties")
        output.delete()
        val log = File(temporary, "gradle.log")
        val process = ProcessBuilder(listOf(
            File(repository, "gradlew").path, "--quiet", "--console=plain", "--no-configuration-cache",
            "--project-cache-dir", File(temporary, "cache").path,
            "-I", init.path, "-Dplain.build.plan=${output.path}", task,
        ) + arguments).directory(repository).redirectErrorStream(true).redirectOutput(log).start()
        if (!process.waitFor(180, TimeUnit.SECONDS)) {
            process.destroyForcibly()
            error("Gradle test timed out: $task\n${log.readText().takeLast(6000)}")
        }
        check(process.exitValue() == 0) { "$task failed:\n${log.readText().takeLast(6000)}" }
        return Properties().apply { output.inputStream().use { load(it) } }
    }

    override fun close() {
        temporary.deleteRecursively()
    }
}
