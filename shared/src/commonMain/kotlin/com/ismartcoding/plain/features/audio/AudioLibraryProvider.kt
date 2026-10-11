package com.ismartcoding.plain.features.audio

import com.ismartcoding.plain.features.file.FileSortBy
import com.ismartcoding.plain.graphql.models.AudioItem

interface AudioLibraryProvider {
    suspend fun count(): Int
    suspend fun page(offset: Int, limit: Int, sort: FileSortBy): List<AudioItem>
    suspend fun metadata(path: String): AudioItem = com.ismartcoding.plain.platform.audioLibraryMetadata(path)
    suspend fun contains(path: String): Boolean
}
