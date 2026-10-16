package com.ismartcoding.plain.tests

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ismartcoding.plain.features.tags.TagRelationStub
import com.ismartcoding.plain.platform.system.DataType
import com.ismartcoding.plain.features.tags.TagsClient
import com.ismartcoding.plain.api.graphql.models.TagHostRelation
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TagsRustHttpTest {
    @Test
    fun allTypesUseRustWithAtomicMetadataAndTypedKeys() = runBlocking {
        val prefix = "tags-rust-${UUID.randomUUID()}"
        val key = "$prefix/文件, 'quoted'"
        val tags = mutableMapOf<DataType, String>()
        try {
            for (type in DataType.entries.filter { it != DataType.DEFAULT }) {
                val id = TagsClient.addOrUpdate("") { name = "$prefix-${type.name}"; this.type = type.value }
                tags[type] = id
                TagsClient.addTagRelations(listOf(TagHostRelation(tagId=id,key=key,type=type.value,title="original",sizeBytes=5_000_000_001L)))
                val first = TagsClient.getTagRelationsByKey(key,type).single()
                assertEquals(5_000_000_001L,first.sizeBytes)
                assertEquals("original",first.title)
                assertEquals(1,checkNotNull(TagsClient.get(id)).count)
                TagsClient.editTagRelations(type,TagRelationStub(key,"updated",6_000_000_001L),listOf(id),emptyList())
                val second = TagsClient.getTagRelationsByKey(key,type).single()
                assertEquals(first.createdAt,second.createdAt)
                assertEquals("updated",second.title)
                assertEquals(6_000_000_001L,second.sizeBytes)
                val originalTag = checkNotNull(TagsClient.get(id))
                TagsClient.addOrUpdate(id) { name = "$prefix-renamed" }
                assertEquals(originalTag.createdAt,checkNotNull(TagsClient.get(id)).createdAt)
            }
            TagsClient.deleteTagRelationByKeys(setOf(key),DataType.AUDIO)
            assertTrue(TagsClient.getTagRelationsByKey(key,DataType.AUDIO).isEmpty())
            assertEquals(1,TagsClient.getTagRelationsByKey(key,DataType.IMAGE).size)
            val imageId = tags.getValue(DataType.IMAGE)
            val another = TagsClient.addOrUpdate("") { name = "$prefix-extra"; type = DataType.IMAGE.value }
            tags[DataType.DEFAULT] = another
            TagsClient.addTagRelations(listOf(TagHostRelation(tagId=another,key=key,type=DataType.IMAGE.value)))
            assertEquals(listOf(key),TagsClient.getKeysByTagIdsAsync(setOf(imageId,another)))
            var failed = false
            try {
                TagsClient.addTagRelations(listOf(
                    TagHostRelation(tagId=imageId,key="$key/new",type=DataType.IMAGE.value),
                    TagHostRelation(tagId=tags.getValue(DataType.VIDEO),key="$key/bad",type=DataType.IMAGE.value),
                ))
            } catch (_: Exception) { failed = true }
            assertTrue(failed)
            assertTrue(TagsClient.getTagRelationsByKey("$key/new",DataType.IMAGE).isEmpty())
            TagsClient.deleteTagRelationByKeysTagIds(setOf(key),setOf(imageId,another))
            assertTrue(TagsClient.getKeysByTagIdsAsync(setOf(imageId,another)).isEmpty())
            assertTrue(TagsClient.getKeysByTagIdsAsync(emptySet()).isEmpty())
        } finally {
            tags.values.forEach { TagsClient.delete(it) }
        }
    }
}
