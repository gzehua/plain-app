package com.ismartcoding.plain.rust.request

import kotlinx.serialization.json.*
import kotlin.time.Instant

internal fun gql(value: String): String = JsonPrimitive(value).toString()
internal fun gqlIds(values: Collection<String>): String = values.joinToString(",", "[", "]") { gql(it) }
internal fun selectionQuery(ids: Collection<String>): String {
    require(ids.isNotEmpty()) { "Selection required" }
    require(ids.all { it.isNotBlank() && ',' !in it }) { "Invalid ID list" }
    return buildString {
        append("ids:=")
        for (character in ids.distinct().joinToString(",")) {
            if (character == '\\' || character == '\'' || character == '"' || character.isWhitespace()) append('\\')
            append(character)
        }
    }
}
