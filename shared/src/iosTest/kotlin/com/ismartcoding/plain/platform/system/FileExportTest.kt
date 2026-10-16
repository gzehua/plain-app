@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.ismartcoding.plain.platform.system

import com.ismartcoding.plain.platform.files.copyFileToUri
import com.ismartcoding.plain.platform.files.getCanonicalPath
import com.ismartcoding.plain.platform.files.writeBytesToPath

import com.ismartcoding.plain.lib.toByteArray
import kotlinx.coroutines.test.runTest
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FileExportTest {
    @Test
    fun selectedFileUrlsExportBytesAndAliasesCannotDeleteTheirSource() = runTest {
        val root = NSTemporaryDirectory() + NSUUID().UUIDString
        val manager = NSFileManager.defaultManager
        assertTrue(manager.createDirectoryAtPath(root, true, null, null))
        try {
            val source = "$root/source.zip"
            val target = "$root/export.zip"
            val alias = "$root/source-alias.zip"
            val bytes = byteArrayOf(0, 1, 127, -1)
            assertTrue(writeBytesToPath(source, bytes))
            assertTrue(copyFileToUri(source, NSURL.fileURLWithPath(target).absoluteString!!))
            assertContentEquals(bytes, manager.contentsAtPath(target)!!.toByteArray())
            assertEquals(0, platform.posix.symlink(source, alias))
            assertEquals(getCanonicalPath(source), getCanonicalPath(alias))
            assertFalse(copyFileToUri(source, NSURL.fileURLWithPath(alias).absoluteString!!))
            assertContentEquals(bytes, manager.contentsAtPath(source)!!.toByteArray())
            assertFalse(copyFileToUri(source, "https://example.invalid/export.zip"))
        } finally {
            manager.removeItemAtPath(root, null)
        }
    }
}
