package com.ismartcoding.plain.platform




expect suspend fun audioLibraryResolve(ids: List<String>, paths: List<String>): List<com.ismartcoding.plain.features.audio.AudioMedia>
