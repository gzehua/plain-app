package com.ismartcoding.plain.features.audio

import com.ismartcoding.plain.features.file.FileSortBy
import com.ismartcoding.plain.graphql.models.AudioHostTrack

interface AudioLibraryProvider {
    suspend fun count(): Int
    suspend fun page(offset: Int, limit: Int, sort: FileSortBy): List<AudioHostTrack>
    suspend fun metadata(path: String): AudioHostTrack = com.ismartcoding.plain.platform.audioLibraryMetadata(path)
    suspend fun contains(path: String): Boolean
}
