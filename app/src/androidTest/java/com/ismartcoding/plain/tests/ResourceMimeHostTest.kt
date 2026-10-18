package com.ismartcoding.plain.tests

import androidx.core.content.FileProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ismartcoding.plain.app.AppIntents
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.rust.host.provider.SystemProviderHost
import java.io.File
import java.util.UUID
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
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
                val result = SystemProviderHost.handle("systemFileResource", JsonObject(mapOf(
                    "operation" to JsonPrimitive("mime"),
                    "path" to JsonPrimitive(uri),
                )))
                assertEquals(expected, result.jsonPrimitive.content)
                assertTrue(file.readBytes().contentEquals(byteArrayOf(0, 1, 2)))
            }
        } finally {
            directory.deleteRecursively()
        }
    }
}
