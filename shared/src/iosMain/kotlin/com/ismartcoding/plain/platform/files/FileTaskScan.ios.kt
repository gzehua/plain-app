package com.ismartcoding.plain.platform.files

actual suspend fun scanFileTaskPaths(paths: List<String>): Unit = error("System media catalog is unavailable")
