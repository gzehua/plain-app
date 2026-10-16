package com.ismartcoding.plain.platform.files

internal actual suspend fun writeResourceFile(source: String, destination: String): Boolean =
    error("Unsupported writable system resource")
