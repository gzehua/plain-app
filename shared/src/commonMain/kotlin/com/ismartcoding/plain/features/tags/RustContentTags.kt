package com.ismartcoding.plain.features.tags

import com.ismartcoding.plain.api.graphql.models.Tag
import com.ismartcoding.plain.api.graphql.models.TagRelation
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.system.DataType
import com.ismartcoding.plain.rust.request.RustContentApi
import com.ismartcoding.plain.rust.request.gql
import com.ismartcoding.plain.rust.request.gqlIds
import kotlinx.serialization.json.*

internal object RustContentTags : TagsApi(
    query = { RustContentApi.query(it) },
    mutate = { RustContentApi.mutate(it) },
)

internal open class TagsApi(
    private val query: suspend (String) -> JsonObject,
    private val mutate: suspend (String) -> JsonObject,
) {
    private fun JsonElement.tag(): Tag = JsonHelper.jsonDecodeFromElement(this)
    suspend fun all(type: DataType): List<Tag> = query("tags(type: ${type.name}) { $TAG_FIELDS }").getValue("tags").jsonArray.map { it.tag() }
    suspend fun create(type: DataType, name: String): Tag =
        mutate("createTag(type: ${type.name}, name: ${gql(name)}) { $TAG_FIELDS }").getValue("createTag").tag()
    suspend fun rename(id: String, name: String): Tag =
        mutate("updateTag(id: ${gql(id)}, name: ${gql(name)}) { $TAG_FIELDS }").getValue("updateTag").tag()
    suspend fun delete(id: String) { mutate("deleteTag(id: ${gql(id)})") }
    suspend fun keys(id: String): List<String> = query("tagKeys(id: ${gql(id)})").getValue("tagKeys").jsonArray.map { it.jsonPrimitive.content }
    suspend fun relations(keys: Set<String>, type: DataType): List<TagRelation> = query("tagRelations(type: ${type.name}, keys: ${gqlIds(keys)}) { tagId key }")
        .getValue("tagRelations").let { JsonHelper.jsonDecodeFromElement(it) }
    suspend fun edit(type: DataType, keys: Collection<String>, add: Collection<String>, remove: Collection<String>) {
        if (keys.isEmpty() || (add.isEmpty() && remove.isEmpty())) return
        mutate("updateTagRelations(type: ${type.name}, keys: ${gqlIds(keys)}, addTagIds: ${gqlIds(add)}, removeTagIds: ${gqlIds(remove)})")
    }
}
