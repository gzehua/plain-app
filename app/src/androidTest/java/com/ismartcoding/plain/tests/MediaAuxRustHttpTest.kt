package com.ismartcoding.plain.tests

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ismartcoding.plain.tests.fixtures.imageEmbedding
import com.ismartcoding.plain.tests.fixtures.ImageEmbeddings
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.ByteBuffer
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class MediaAuxRustHttpTest {
    @Test
    fun embeddingsUseRustWithExactValuesAndRankedSearch() = runBlocking {
        val prefix = "media-aux-${UUID.randomUUID()}"
        val bestId = "$prefix-best"
        val nextId = "$prefix-next"
        fun vector(first: Float): ByteArray = ByteBuffer.allocate(512 * 4).apply {
            putFloat(first)
            repeat(511) { putFloat(0f) }
        }.array()
        try {
            ImageEmbeddings.insertAll(listOf(
                imageEmbedding(bestId,"/synthetic/$bestId",vector(10f)),
                imageEmbedding(nextId,"/synthetic/$nextId",vector(9f)),
            ))
            assertTrue(ImageEmbeddings.getAllIds().containsAll(listOf(bestId,nextId)))
            val results = ImageEmbeddings.search(vector(1f),2)
            assertEquals(listOf(bestId,nextId),results.map { it.imageId })
            assertEquals(10f,results.first().score,0f)
            var failed = false
            try {
                ImageEmbeddings.insertAll(listOf(
                    imageEmbedding("$prefix-new","/synthetic/new",vector(1f)),
                    imageEmbedding("$prefix-bad","/synthetic/bad",vector(Float.NaN)),
                ))
            } catch (_: Exception) { failed = true }
            assertTrue(failed)
            assertFalse(ImageEmbeddings.getAllIds().contains("$prefix-new"))
            ImageEmbeddings.deleteByIds(emptyList())
            assertTrue(ImageEmbeddings.getAllIds().contains(bestId))
        } finally {
            ImageEmbeddings.deleteByIds(listOf(bestId,nextId,"$prefix-new","$prefix-bad"))
        }
    }
}
