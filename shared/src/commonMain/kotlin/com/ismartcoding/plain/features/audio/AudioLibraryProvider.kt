package com.ismartcoding.plain.features.audio


interface AudioLibraryProvider {
    suspend fun resolve(ids: List<String>, paths: List<String>): List<AudioMedia> = com.ismartcoding.plain.platform.audioLibraryResolve(ids, paths)
}
