package com.ismartcoding.plain.tests

import androidx.core.content.FileProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ismartcoding.plain.app.AppIntents
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.platform.getContentTypeForPath
import java.io.File
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResourceMimeHostTest {
    @Test
    fun resourceMimeReadsTheActualProviderWithoutTreatingTheUriAsAFile() = runBlocking {
        val directory = File(appContext.cacheDir, "resource-mime-${UUID.randomUUID()}")
        assertTrue(directory.mkdir())
        try {
            for ((name, expected) in listOf("image.png" to "image/png", "document.pdf" to "application/pdf")) {
                val file = File(directory, name).apply { writeBytes(byteArrayOf(0, 1, 2)) }
                val uri = FileProvider.getUriForFile(appContext, AppIntents.AUTHORITY, file).toString()
                assertFalse(File(uri).exists())
                assertEquals(expected, getContentTypeForPath(uri))
                assertTrue(file.readBytes().contentEquals(byteArrayOf(0, 1, 2)))
            }
        } finally {
            directory.deleteRecursively()
        }
    }
}
