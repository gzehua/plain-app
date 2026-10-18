package com.ismartcoding.plain.tests

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ismartcoding.plain.platform.system.DataType
import com.ismartcoding.plain.features.tags.TagsClient
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TagsRustHttpTest {
    @Test
    fun allTypesUseRustWithAtomicTypedKeys() = runBlocking {
        val prefix = "tags-rust-${UUID.randomUUID()}"
        val key = "$prefix/文件, 'quoted'"
        val tags = mutableMapOf<DataType, String>()
        try {
            for (type in DataType.entries.filter { it != DataType.DEFAULT }) {
                val tag = TagsClient.create(type, "$prefix-${type.name}")
                tags[type] = tag.id
                TagsClient.editTagRelations(type, listOf(key), listOf(tag.id), emptyList())
                val first = TagsClient.getTagRelationsByKeys(setOf(key), type).single()
                assertEquals(tag.id, first.tagId)
                assertEquals(key, first.key)
                TagsClient.editTagRelations(type, listOf(key), listOf(tag.id), emptyList())
                assertEquals(1, TagsClient.getTagRelationsByKeys(setOf(key), type).size)
                assertEquals(1, TagsClient.getAll(type).single { it.id == tag.id }.count)
                val renamed = TagsClient.rename(tag.id, "$prefix-renamed")
                assertEquals(tag.id, renamed.id)
                assertEquals("$prefix-renamed", renamed.name)
                assertEquals(1, renamed.count)
            }
            val imageId = tags.getValue(DataType.IMAGE)
            var failed = false
            try {
                TagsClient.editTagRelations(DataType.IMAGE, listOf("$key/new"), listOf(imageId, tags.getValue(DataType.VIDEO)), emptyList())
            } catch (_: Exception) { failed = true }
            assertTrue(failed)
            assertTrue(TagsClient.getTagRelationsByKeys(setOf("$key/new"), DataType.IMAGE).isEmpty())
            TagsClient.editTagRelations(DataType.AUDIO, listOf(key), emptyList(), listOf(tags.getValue(DataType.AUDIO)))
            assertTrue(TagsClient.getTagRelationsByKeys(setOf(key), DataType.AUDIO).isEmpty())
            assertEquals(1, TagsClient.getTagRelationsByKeys(setOf(key), DataType.IMAGE).size)
            TagsClient.editTagRelations(DataType.IMAGE, listOf(key), emptyList(), listOf(imageId))
            assertTrue(TagsClient.getKeysByTagId(imageId).isEmpty())
        } finally {
            tags.values.forEach { TagsClient.delete(it) }
        }
    }
}
