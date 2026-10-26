package com.ismartcoding.plain.features.tags

import com.ismartcoding.plain.rust.graphql.GraphqlRequest
import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.models.Tag
import com.ismartcoding.plain.models.TagRelation
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.system.DataType
import kotlinx.serialization.json.*

internal object RustContentTags : TagsApi(
    query = { RustGraphqlApi.graphql(it) },
    mutate = { RustGraphqlApi.graphql(it) },
)

internal open class TagsApi(
    private val query: suspend (GraphqlRequest) -> JsonObject,
    private val mutate: suspend (GraphqlRequest) -> JsonObject,
) {
    private fun JsonElement.tag(): Tag = JsonHelper.jsonDecodeFromElement(this)
    suspend fun all(type: DataType): List<Tag> = query(Query.tags(type.name)).getValue("tags").jsonArray.map { it.tag() }
    suspend fun create(type: DataType, name: String): Tag =
        mutate(Mutation.createTag(type.name, name)).getValue("createTag").tag()
    suspend fun rename(id: String, name: String): Tag =
        mutate(Mutation.updateTag(id, name)).getValue("updateTag").tag()
    suspend fun delete(id: String) { mutate(Mutation.deleteTag(id)) }
    suspend fun keys(id: String): List<String> = query(Query.tagKeys(id)).getValue("tagKeys").jsonArray.map { it.jsonPrimitive.content }
    suspend fun relations(keys: Set<String>, type: DataType): List<TagRelation> = query(Query.tagRelations(type.name, keys))
        .getValue("tagRelations").let { JsonHelper.jsonDecodeFromElement(it) }
    suspend fun edit(type: DataType, keys: Collection<String>, add: Collection<String>, remove: Collection<String>) {
        if (keys.isEmpty() || (add.isEmpty() && remove.isEmpty())) return
        mutate(Mutation.updateTagRelations(type.name, keys, add, remove))
    }
}
