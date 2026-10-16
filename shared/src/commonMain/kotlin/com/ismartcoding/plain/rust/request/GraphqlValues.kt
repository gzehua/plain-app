package com.ismartcoding.plain.rust.request

import kotlinx.serialization.json.*
import kotlin.time.Instant

internal fun gql(value: String): String = JsonPrimitive(value).toString()
internal fun gqlIds(values: Collection<String>): String = values.joinToString(",", "[", "]") { gql(it) }
internal fun selectionQuery(ids: Set<String>): String = "ids:${ids.joinToString(",") }"
internal fun JsonObject.string(name: String): String = getValue(name).jsonPrimitive.content
internal fun JsonObject.instant(name: String): Instant = Instant.parse(string(name))
internal fun JsonObject.nullableInstant(name: String): Instant? = this[name]?.takeUnless { it is JsonNull }?.jsonPrimitive?.content?.let(Instant::parse)
