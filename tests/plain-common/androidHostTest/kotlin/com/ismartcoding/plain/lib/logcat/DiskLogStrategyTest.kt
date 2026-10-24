package com.ismartcoding.plain.lib.logcat

import java.io.File
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import java.io.RandomAccessFile

class DiskLogStrategyTest {
    @get:Rule val directory = TemporaryFolder()

    @Test
    fun writesToTheConfiguredFileWithoutChoosingAnotherName() {
        val previous = LogCat.logFilePath
        val target = File(directory.root, "nested/sdk-output.txt")
        try {
            LogCat.logFilePath = target.absolutePath
            val strategy = DiskLogStrategy()
            strategy.log(LogCat.INFO, "fixture", "first")
            strategy.log(LogCat.INFO, "fixture", "second")
            assertEquals("first\nsecond\n", target.readText())
            assertFalse(File(target.parentFile, "latest.log").exists())
        } finally {
            LogCat.logFilePath = previous
        }
    }

    @Test
    fun rotationUsesTheConfiguredNameAndPreservesUnrelatedFiles() {
        val previous = LogCat.logFilePath
        val target = directory.newFile("sdk-output.txt")
        val unrelated = directory.newFile("unrelated.txt").apply { writeText("keep") }
        val backup = File(target.path + ".bak").apply { writeText("old backup") }
        try {
            LogCat.logFilePath = target.absolutePath
            RandomAccessFile(target, "rw").use { it.setLength(25L * 1024 * 1024) }
            DiskLogStrategy().log(LogCat.WARN, "fixture", "rotate")
            assertFalse(target.exists())
            assertTrue(backup.isFile)
            assertEquals(25L * 1024 * 1024 + "rotate\n".length, backup.length())
            DiskLogStrategy().log(LogCat.INFO, "fixture", "next")
            assertEquals("next\n", target.readText())
            assertEquals("keep", unrelated.readText())
        } finally {
            LogCat.logFilePath = previous
        }
    }
}
