package com.ismartcoding.plain.platform

import com.ismartcoding.plain.graphql.models.AudioItem


actual suspend fun audioLibraryMetadata(path: String): AudioItem {
    check(platform.Foundation.NSFileManager.defaultManager.fileExistsAtPath(path)) { "Audio file not found" }
    return playlistAudioFromPath(path)
}

actual suspend fun audioLibraryResolve(ids: List<String>, paths: List<String>): List<com.ismartcoding.plain.features.audio.AudioMedia> = emptyList()
