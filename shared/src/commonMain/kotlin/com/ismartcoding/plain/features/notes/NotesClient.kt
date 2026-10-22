package com.ismartcoding.plain.features.notes

import com.ismartcoding.plain.models.Note
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.request.gql
import com.ismartcoding.plain.rust.request.selectionQuery
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object NotesClient : NotesApi(
    query = { PlainRust.mainQuery(it) },
    mutate = { PlainRust.mainMutate(it) },
)

open class NotesApi internal constructor(
    private val query: suspend (String) -> JsonObject,
    private val mutate: suspend (String) -> JsonObject,
) {
    suspend fun count(query: String): Int = this.query("noteCount(query: ${gql(query)})").getValue("noteCount").jsonPrimitive.int
    suspend fun search(query: String, limit: Int, offset: Int): List<Note> = this.query("notes(query: ${gql(query)}, limit: $limit, offset: $offset) { $NOTE_FIELDS }").getValue("notes").jsonArray.map { it.note() }
    suspend fun getById(id: String): Note? = query("note(id: ${gql(id)}) { $NOTE_FIELDS }")["note"]?.takeUnless { it is JsonNull }?.note()
    suspend fun save(id: String, title: String, content: String): Note {
        val input = "{ title: ${gql(title)}, content: ${gql(content)} }"
        val field = if (id.isEmpty()) "createNote" else "updateNote"
        val args = if (id.isEmpty()) "input: $input" else "id: ${gql(id)}, input: $input"
        return mutate("$field($args) { $NOTE_FIELDS }").getValue(field).note()
    }
    suspend fun saveFeedEntryAsync(id: String) {
        mutate("saveFeedEntriesToNotes(query: ${gql(selectionQuery(setOf(id)))})")
    }
    suspend fun trashAsync(ids: Set<String>) { if (ids.isNotEmpty()) mutate("trashNotes(query: ${gql(selectionQuery(ids))}) { affectedCount }") }
    suspend fun restoreAsync(ids: Set<String>) { if (ids.isNotEmpty()) mutate("restoreNotes(query: ${gql(selectionQuery(ids))}) { affectedCount }") }
    suspend fun deleteAsync(ids: Set<String>) { if (ids.isNotEmpty()) mutate("deleteNotes(query: ${gql(selectionQuery(ids))}) { affectedCount }") }
}
