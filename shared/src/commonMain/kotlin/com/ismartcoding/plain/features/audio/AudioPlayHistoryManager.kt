package com.ismartcoding.plain.features.audio

import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.graphql.models.AudioPlayHistory
import com.ismartcoding.plain.rust.bridge.*
import com.ismartcoding.plain.rust.content.*
import com.ismartcoding.plain.rust.event.*
import com.ismartcoding.plain.rust.host.*
import com.ismartcoding.plain.rust.http.*
import com.ismartcoding.plain.rust.request.*
import kotlinx.serialization.json.*

object AudioPlayHistoryManager {
    suspend fun artistPlayCounts(): Map<String, Long> = PlainRust.privateQuery("audioArtistPlayCounts { artist count }")
        .getValue("audioArtistPlayCounts").jsonArray.associate { r -> r.jsonObject.string("artist") to r.jsonObject.getValue("count").jsonPrimitive.long }
    suspend fun recentPage(limit: Int, offset: Int): List<AudioPlayHistory> = recentPageFiltered("",limit,offset)
    suspend fun recentPageFiltered(text: String, limit: Int, offset: Int): List<AudioPlayHistory> =
        RustContentApi.query("audioPlayHistory(offset: $offset, limit: $limit, query: ${gql(text)}) { $AUDIO_HISTORY_FIELDS }")
            .getValue("audioPlayHistory").jsonArray.map { it.audioHistory() }
}
