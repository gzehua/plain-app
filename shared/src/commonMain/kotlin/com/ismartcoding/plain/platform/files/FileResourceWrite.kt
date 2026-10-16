package com.ismartcoding.plain.platform.files

internal expect suspend fun writeResourceFile(source: String, destination: String): Boolean
