package com.ismartcoding.plain.data

import com.ismartcoding.plain.db.IData
import com.ismartcoding.plain.enums.DataType
import com.ismartcoding.plain.graphql.models.TagHostRelation
import kotlinx.serialization.Serializable

@Serializable
data class TagRelationStub(
    var key: String = "",
    var title: String = "",
    var size: Long = 0,
) {
    fun toTagRelation(
        tagId: String,
        type: DataType,
    ): TagHostRelation {
        val stub = this
        return TagHostRelation(tagId, stub.key, type.value).apply {
            title = stub.title
            sizeBytes = stub.size
        }
    }

    companion object {
        fun create(data: IData): TagRelationStub = when (data) {
            is IItemMetadata -> TagRelationStub(data.id, data.title, data.size)
            else -> TagRelationStub(data.id)
        }
    }
}
