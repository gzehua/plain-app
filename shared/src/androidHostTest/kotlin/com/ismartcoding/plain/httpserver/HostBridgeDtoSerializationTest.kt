package com.ismartcoding.plain.httpserver

import com.ismartcoding.plain.models.File as FileEntry

import com.ismartcoding.plain.lib.JsonHelper.jsonEncode
import com.ismartcoding.plain.models.Package as AppPackage
import com.ismartcoding.plain.platform.system.PackageType
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Instant

/** File and package facts must remain serializable across the native bridge. */
class HostBridgeDtoSerializationTest {
    private val epoch = Instant.fromEpochMilliseconds(0)

    @Test
    fun fileFactsEncode() {
        val json = jsonEncode(
            FileEntry(name = "notes.txt", path = "/sdcard/Download/notes.txt", permission = "rwx", createdAt = epoch, updatedAt = epoch, size = 12, isDir = false, childCount = 0)
        )
        assertTrue(json.contains("notes.txt"), json)
        assertTrue(json.contains("/sdcard/Download/notes.txt"), json)
    }

    /** Already annotated; kept so a future edit cannot quietly drop it — the
     *  package facts go over the same bridge. */
    @Test
    fun packageFactsEncode() {
        val json = jsonEncode(
            listOf(
                AppPackage(
                    id = "com.example",
                    name = "Example",
                    type = PackageType.USER,
                    version = "1.0",
                    path = "/data/app/example",
                    size = 1024,
                    installedAt = epoch,
                    updatedAt = epoch,
                )
            )
        )
        assertTrue(json.contains("com.example"), json)
        assertTrue(json.contains("USER"), json)
    }
}
