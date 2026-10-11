package com.ismartcoding.plain.platform

import com.ismartcoding.plain.features.file.FileSortBy
import com.ismartcoding.plain.graphql.models.AudioHostTrack

actual suspend fun audioLibraryCount(): Int = error("System audio catalog is unavailable on iOS")
actual suspend fun audioLibraryPage(offset: Int, limit: Int, sort: FileSortBy): List<AudioHostTrack> = error("System audio catalog is unavailable on iOS")
actual suspend fun audioLibraryContains(path: String): Boolean = error("System audio catalog is unavailable on iOS")

actual suspend fun audioLibraryMetadata(path: String): AudioHostTrack {
    check(platform.Foundation.NSFileManager.defaultManager.fileExistsAtPath(path)) { "Audio file not found" }
    return playlistAudioFromPath(path)
}
