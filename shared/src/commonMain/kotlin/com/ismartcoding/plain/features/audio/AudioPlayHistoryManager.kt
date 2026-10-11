package com.ismartcoding.plain.features.audio

import com.ismartcoding.plain.graphql.models.AudioHostHistory
import com.ismartcoding.plain.rust.bridge.*
import com.ismartcoding.plain.rust.content.*
import com.ismartcoding.plain.rust.event.*
import com.ismartcoding.plain.rust.host.*
import com.ismartcoding.plain.rust.http.*
import com.ismartcoding.plain.rust.request.*
import kotlinx.serialization.json.*

object AudioPlayHistoryManager {
    suspend fun recordHistory(path: String, title: String, artist: String, durationMs: Long) {
        RustContentApi.mutate("audioHostRecordHistory(path: ${gql(path)}, title: ${gql(title)}, artist: ${gql(artist)}, durationMs: $durationMs)")
    }
    suspend fun artistPlayCounts(): Map<String, Long> = RustContentApi.query("audioArtistPlayCounts { artist count }")
        .getValue("audioArtistPlayCounts").jsonArray.associate { r -> r.jsonObject.string("artist") to r.jsonObject.getValue("count").jsonPrimitive.long }
    suspend fun recentPage(limit: Int, offset: Int): List<AudioHostHistory> = recentPageFiltered("",limit,offset)
    suspend fun recentPageFiltered(text: String, limit: Int, offset: Int): List<AudioHostHistory> =
        RustContentApi.query("audioHostHistory(offset: $offset, limit: $limit, query: ${gql(text)}) { $AUDIO_HISTORY_FIELDS }")
            .getValue("audioHostHistory").jsonArray.map { it.audioHistory() }
}
