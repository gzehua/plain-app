package com.ismartcoding.plain.rust.content

import com.ismartcoding.plain.db.*
import com.ismartcoding.plain.graphql.models.Feed
import com.ismartcoding.plain.graphql.models.FeedEntry
import com.ismartcoding.plain.graphql.models.Note
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.chaCha20Decrypt
import com.ismartcoding.plain.preferences.RustSystemState
import kotlin.io.encoding.Base64
import kotlin.time.Instant
import kotlinx.serialization.json.*

internal fun gql(value: String): String = JsonPrimitive(value).toString()
internal fun gqlIds(values: Collection<String>): String = values.joinToString(",", "[", "]") { gql(it) }
internal fun selectionQuery(ids: Set<String>): String = "ids:${ids.joinToString(",") }"
internal const val NOTE_FIELDS = "id title content deletedAt createdAt updatedAt"
internal const val FEED_FIELDS = "id name url logo fetchContent lastSyncAt lastError { code detail } createdAt updatedAt"
internal const val ENTRY_FIELDS = "id title url image description author content feedId rawId publishedAt read createdAt updatedAt"
internal const val TAG_FIELDS = "id name type count"
internal fun JsonObject.string(name: String): String = getValue(name).jsonPrimitive.content
internal fun JsonObject.instant(name: String): Instant = Instant.parse(string(name))
internal fun JsonObject.nullableInstant(name: String): Instant? = this[name]?.takeUnless { it is JsonNull }?.jsonPrimitive?.content?.let(Instant::parse)
internal fun JsonElement.note(): Note = JsonHelper.jsonDecodeFromElement<Note>(this)
internal fun JsonElement.feed(): Feed = JsonHelper.jsonDecodeFromElement<Feed>(this)
internal fun JsonElement.entry(): FeedEntry = JsonHelper.jsonDecodeFromElement<FeedEntry>(this)
