package com.ismartcoding.plain.platform.media




expect suspend fun audioLibraryResolve(ids: List<String>, paths: List<String>): List<com.ismartcoding.plain.features.audio.AudioMedia>
