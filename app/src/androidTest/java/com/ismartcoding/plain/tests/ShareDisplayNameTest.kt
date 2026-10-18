package com.ismartcoding.plain.tests

import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ismartcoding.plain.app.AppIntents
import com.ismartcoding.plain.platform.files.ShareHelper
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ShareDisplayNameTest {
    @Test
    fun displayNameSharesTheOriginalProviderFileWithoutCopying() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val directory = File(base.cacheDir, "share-fixture-${UUID.randomUUID()}").apply { mkdirs() }
        val source = File(directory, "original.txt").apply { writeText("owned fixture") }
        val neighbor = File(directory, "neighbor.txt").apply { writeText("keep") }
        var launched: Intent? = null
        val context = object : ContextWrapper(base) {
            override fun startActivity(intent: Intent) {
                launched = intent
            }
        }
        try {
            val name = "export name.json"
            ShareHelper.shareFile(context, source, displayName = name)
            val send = checkNotNull(launched).getParcelableExtra<Intent>(Intent.EXTRA_INTENT)!!
            val uri = send.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)!!
            assertEquals("application/json", send.type)
            assertEquals(FileProvider.getUriForFile(base, AppIntents.AUTHORITY, source, name), uri)
            assertTrue(send.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
            base.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)!!.use {
                assertTrue(it.moveToFirst())
                assertEquals(name, it.getString(0))
            }
            base.contentResolver.openInputStream(uri)!!.use {
                assertArrayEquals(source.readBytes(), it.readBytes())
            }
            assertEquals(setOf(source.name, neighbor.name), directory.list()!!.toSet())
            assertEquals("keep", neighbor.readText())
        } finally {
            directory.deleteRecursively()
        }
    }
}
