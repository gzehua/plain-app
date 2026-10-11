package com.ismartcoding.plain.features.audio

import com.ismartcoding.plain.features.file.FileSortBy
import com.ismartcoding.plain.graphql.models.AudioItem
import com.ismartcoding.plain.platform.audioLibraryContains
import com.ismartcoding.plain.platform.audioLibraryCount
import com.ismartcoding.plain.platform.audioLibraryPage

object PlatformAudioLibraryProvider : AudioLibraryProvider {
    override suspend fun count(): Int = audioLibraryCount()
    override suspend fun page(offset: Int, limit: Int, sort: FileSortBy): List<AudioItem> = audioLibraryPage(offset, limit, sort)
    override suspend fun contains(path: String): Boolean = audioLibraryContains(path)
}
