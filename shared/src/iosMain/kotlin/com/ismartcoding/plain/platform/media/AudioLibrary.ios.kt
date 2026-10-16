package com.ismartcoding.plain.platform.media

actual suspend fun audioLibraryResolve(ids: List<String>, paths: List<String>): List<AudioMedia> =
    error("System audio catalog is unavailable")
