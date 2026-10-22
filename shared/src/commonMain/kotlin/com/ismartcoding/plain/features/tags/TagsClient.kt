package com.ismartcoding.plain.features.tags

import com.ismartcoding.plain.models.Tag
import com.ismartcoding.plain.models.TagRelation
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.platform.system.DataType

object TagsClient {
    suspend fun getAll(type: DataType): List<Tag> = RustContentTags.all(type)
    suspend fun create(type: DataType, name: String): Tag = withIO { RustContentTags.create(type, name) }
    suspend fun rename(id: String, name: String): Tag = withIO { RustContentTags.rename(id, name) }
    suspend fun delete(id: String) = RustContentTags.delete(id)
    suspend fun getTagRelationsByKeys(keys: Set<String>, type: DataType): List<TagRelation> = RustContentTags.relations(keys, type)
    suspend fun getTagRelationsByKeysMap(keys: Set<String>, type: DataType): Map<String, List<TagRelation>> = getTagRelationsByKeys(keys, type).groupBy { it.key }
    suspend fun getKeysByTagId(tagId: String): List<String> = RustContentTags.keys(tagId)
    suspend fun editTagRelations(type: DataType, keys: Collection<String>, add: Collection<String>, remove: Collection<String>) = RustContentTags.edit(type, keys, add, remove)
}
