package com.ismartcoding.plain.features.notes

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.models.Note
import com.ismartcoding.plain.rust.graphql.GraphqlRequest
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.rust.graphql.selectionQuery
import kotlinx.serialization.json.JsonObject

object NotesClient : NotesApi(
    query = { RustGraphqlApi.graphql(it) },
    mutate = { RustGraphqlApi.graphql(it) },
) {

    suspend fun readDraft(): String = RustGraphqlApi.readSavedValue("quick_note_draft", "")

    suspend fun writeDraft(value: String) = RustGraphqlApi.writeSavedValue("quick_note_draft", value)
}
open class NotesApi internal constructor(
    private val query: suspend (GraphqlRequest) -> JsonObject,
    private val mutate: suspend (GraphqlRequest) -> JsonObject,
) {

    suspend fun count(query: String): Int =
        RustGraphqlApi.read(Query.noteCount(query), this.query)

    suspend fun search(query: String, limit: Int, offset: Int): List<Note> =
        RustGraphqlApi.read(Query.notes(query, limit, offset), this.query)

    suspend fun getById(id: String): Note? =
        RustGraphqlApi.read(Query.note(id), query)

    suspend fun save(id: String, title: String, content: String): Note {
        return RustGraphqlApi.read(Mutation.saveNote(id, title, content), mutate)
    }

    suspend fun saveFeedEntryAsync(id: String) {
        mutate(Mutation.saveFeedEntriesToNotes(selectionQuery(setOf(id))))
    }

    suspend fun trashAsync(ids: Set<String>) {
        if (ids.isNotEmpty()) mutate(Mutation.trashNotes(selectionQuery(ids)))
    }

    suspend fun restoreAsync(ids: Set<String>) {
        if (ids.isNotEmpty()) mutate(Mutation.restoreNotes(selectionQuery(ids)))
    }

    suspend fun deleteAsync(ids: Set<String>) {
        if (ids.isNotEmpty()) mutate(Mutation.deleteNotes(selectionQuery(ids)))
    }
}
