package com.ismartcoding.plain.buildtools

import com.ismartcoding.plain.lib.JsonHelper
import java.io.File
import java.util.concurrent.TimeUnit
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class GeneratePrefKeysTest {
    @get:Rule
    val temporary = TemporaryFolder()

    @Serializable
    private data class CargoMetadata(val packages: List<CargoPackage>)

    @Serializable
    private data class CargoPackage(val name: String, @SerialName("manifest_path") val manifestPath: String)

    private data class Result(val exitCode: Int, val output: String, val generated: String)

    private fun generate(keys: String, settings: String): Result {
        val root = temporary.newFolder()
        val prefs = File(root, "src/prefs").apply { mkdirs() }
        File(prefs, "pref_keys.rs").writeText(keys)
        File(prefs, "settings/mod.rs").apply { checkNotNull(parentFile).mkdirs() }.writeText(settings)
        File(root, "metadata.json").writeText(JsonHelper.jsonEncode(CargoMetadata(
            listOf(CargoPackage("plain-rs", File(root, "Cargo.toml").path))
        )))
        val cargo = File(root, "cargo").apply {
            writeText("#!/bin/sh\ncat \"\$(dirname \"\$0\")/metadata.json\"\n")
            check(setExecutable(true))
        }
        val output = File(root, "PrefKeys.kt")
        val log = File(root, "process.log")
        val repository = buildTestRepository()
        val process = ProcessBuilder(
            "python3", File(repository, "scripts/generate-pref-keys.py").path,
            "--cargo", cargo.path, "--output", output.path,
        ).directory(repository).redirectErrorStream(true).redirectOutput(log).start()
        if (!process.waitFor(30, TimeUnit.SECONDS)) {
            process.destroyForcibly()
            error("Preference key generation timed out")
        }
        return Result(process.exitValue(), log.readText(), output.takeIf { it.exists() }?.readText().orEmpty())
    }

    @Test
    fun storageNamesAndSnapshotAliasesComeFromRust() {
        val result = generate(
            "    INTERVAL = \"interval_seconds\",\n    SORT = \"sort_key\",\n",
            "pub fn set_entry(\nkeys::INTERVAL => \"intervalSec\",\nkeys::SORT => \"packageSort\",",
        )
        assertEquals(0, result.exitCode, result.output)
        for (line in listOf(
            "const val INTERVAL = \"interval_seconds\"",
            "const val SORT = \"sort_key\"",
            "PrefKeys.INTERVAL -> \"intervalSec\"",
            "PrefKeys.SORT -> \"packageSort\"",
        )) assertTrue(line in result.generated, result.generated)
    }

    @Test
    fun missingOrDuplicateKeysFailGeneration() {
        for (keys in listOf("", "    A = \"same\",\n    B = \"same\",\n", "    A = \"one\",\n    A = \"two\",\n")) {
            val result = generate(keys, "pub fn set_entry(\nkeys::A => \"a\",")
            assertNotEquals(0, result.exitCode)
            assertTrue("Rust preference keys must be nonempty and unique" in result.output, result.output)
            assertEquals("", result.generated)
        }
    }

    @Test
    fun unknownStorageKeysInSnapshotMappingFailGeneration() {
        val result = generate("    A = \"one\",\n", "pub fn set_entry(\nkeys::B => \"b\",")
        assertNotEquals(0, result.exitCode)
        assertTrue("Rust settings fields must reference registered preference keys" in result.output, result.output)
        assertEquals("", result.generated)
    }
}
