package com.ismartcoding.plain.platform

import com.ismartcoding.plain.features.file.FileSortBy
import com.ismartcoding.plain.graphql.models.AudioItem

actual suspend fun audioLibraryCount(): Int = error("System audio catalog is unavailable on iOS")
actual suspend fun audioLibraryPage(offset: Int, limit: Int, sort: FileSortBy): List<AudioItem> = error("System audio catalog is unavailable on iOS")
actual suspend fun audioLibraryContains(path: String): Boolean = error("System audio catalog is unavailable on iOS")

actual suspend fun audioLibraryMetadata(path: String): AudioItem {
    check(platform.Foundation.NSFileManager.defaultManager.fileExistsAtPath(path)) { "Audio file not found" }
    return playlistAudioFromPath(path)
}

actual suspend fun audioLibraryResolve(ids: List<String>, paths: List<String>): List<com.ismartcoding.plain.features.audio.AudioMedia> = emptyList()
