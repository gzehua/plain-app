package com.ismartcoding.plain.rust.graphql

internal fun selectionQuery(ids: Collection<String>): String {
    require(ids.isNotEmpty()) { "Selection required" }
    require(ids.all { id -> id.isNotBlank() && ',' !in id }) { "Invalid ID list" }

    val joinedIds = ids.distinct().joinToString(",")
    return buildString {
        append("ids:=")
        for (character in joinedIds) {
            val needsEscape = when (character) {
                '\\', '\'', '"' -> true
                else -> character.isWhitespace()
            }
            if (needsEscape) {
                append('\\')
            }
            append(character)
        }
    }
}
