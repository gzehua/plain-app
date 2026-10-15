package com.ismartcoding.plain.platform

import com.ismartcoding.plain.graphql.models.AudioItem


expect suspend fun audioLibraryMetadata(path: String): AudioItem

expect suspend fun audioLibraryResolve(ids: List<String>, paths: List<String>): List<com.ismartcoding.plain.features.audio.AudioMedia>
