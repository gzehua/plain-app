package com.ismartcoding.plain.features.tags

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.models.Tag
import com.ismartcoding.plain.models.TagRelation
import com.ismartcoding.plain.platform.system.DataType
import com.ismartcoding.plain.rust.graphql.GraphqlRequest
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import kotlinx.serialization.json.JsonObject

internal object RustContentTags : TagsApi(
    query = { RustGraphqlApi.graphql(it) },
    mutate = { RustGraphqlApi.graphql(it) },
)
internal open class TagsApi(
    private val query: suspend (GraphqlRequest) -> JsonObject,
    private val mutate: suspend (GraphqlRequest) -> JsonObject,
) {

    suspend fun all(type: DataType): List<Tag> =
        RustGraphqlApi.read(Query.tags(type), query)

    suspend fun create(type: DataType, name: String): Tag =
        RustGraphqlApi.read(Mutation.createTag(type, name), mutate)

    suspend fun rename(id: String, name: String): Tag =
        RustGraphqlApi.read(Mutation.updateTag(id, name), mutate)

    suspend fun delete(id: String) {
        mutate(Mutation.deleteTag(id))
    }

    suspend fun keys(id: String): List<String> =
        RustGraphqlApi.read(Query.tagKeys(id), query)

    suspend fun relations(keys: Set<String>, type: DataType): List<TagRelation> =
        RustGraphqlApi.read(Query.tagRelations(type, keys), query)

    suspend fun edit(type: DataType, keys: Collection<String>, add: Collection<String>, remove: Collection<String>) {
        if (keys.isEmpty() || (add.isEmpty() && remove.isEmpty())) return
        mutate(Mutation.updateTagRelations(type, keys, add, remove))
    }
}
