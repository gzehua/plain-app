package com.ismartcoding.plain.features.tags

import com.ismartcoding.plain.features.tags.TagRelationStub
import com.ismartcoding.plain.platform.system.DataType
import com.ismartcoding.plain.api.graphql.models.TagHostRecord
import com.ismartcoding.plain.api.graphql.models.TagHostRelation
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.features.tags.RustContentTags

object TagsClient {
    suspend fun count(type: DataType): List<TagHostRecord> = getAll(type)
    suspend fun getAll(type: DataType): List<TagHostRecord> = RustContentTags.all(type)
    suspend fun get(id: String): TagHostRecord? = RustContentTags.get(id)
    suspend fun addOrUpdate(id: String, updateItem: TagHostRecord.() -> Unit): String = withIO {
        val create = id.isEmpty()
        val item = if (create) TagHostRecord() else get(id) ?: error("Tag $id not found")
        updateItem(item)
        RustContentTags.save(item, create)
    }
    suspend fun delete(id: String) = RustContentTags.delete(id)
    suspend fun getTagRelationsByKeys(keys: Set<String>, type: DataType): List<TagHostRelation> = RustContentTags.relations(keys, type)
    suspend fun getTagRelationsByKeysMap(keys: Set<String>, type: DataType): Map<String, List<TagHostRelation>> = getTagRelationsByKeys(keys, type).groupBy { it.key }
    suspend fun getTagRelationsByKey(key: String, type: DataType): List<TagHostRelation> = getTagRelationsByKeys(setOf(key), type)
    suspend fun getKeysByTagId(tagId: String): List<String> = RustContentTags.keys(tagId)
    suspend fun getKeysByTagIdsAsync(tagIds: Set<String>): List<String> = RustContentTags.intersection(tagIds)
    suspend fun addTagRelations(items: List<TagHostRelation>) = RustContentTags.add(items)
    suspend fun editTagRelations(type: DataType, item: TagRelationStub, add: Collection<String>, remove: Collection<String>) = RustContentTags.edit(item.key, type.value, add, remove, item.title, item.size)
    suspend fun deleteTagRelationsByTagId(tagId: String) = RustContentTags.clearTag(tagId)
    suspend fun deleteByTypeAsync(type: DataType) = RustContentTags.clearType(type)
    suspend fun deleteTagRelationByKeys(keys: Set<String>, type: DataType) = RustContentTags.removeKeys(keys, type)
    suspend fun deleteTagRelationByKeysTagId(keys: Set<String>, tagId: String) = RustContentTags.remove(keys, setOf(tagId))
    suspend fun deleteTagRelationByKeysTagIds(keys: Set<String>, tagIds: Set<String>) = RustContentTags.remove(keys, tagIds)
}
