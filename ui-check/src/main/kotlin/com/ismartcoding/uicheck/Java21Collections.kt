package com.ismartcoding.uicheck

import java.io.File

internal fun java21CollectionCalls(source: String): List<String> {
    val forbidden = Regex(
        """\.(removeLast|removeFirst|getFirst|getLast|addFirst|addLast|reversed|putFirst|putLast|firstEntry|lastEntry|pollFirstEntry|pollLastEntry|sequencedKeySet|sequencedValues|sequencedEntry)\("""
    )
    val blockingDeques = Regex("""\b(?:val|var)\s+(\w+)\s*=\s*(?:java\.util\.concurrent\.)?LinkedBlockingDeque\b""")
        .findAll(source).map { it.groupValues[1] }.toSet()
        .takeIf { source.contains("import java.util.concurrent.LinkedBlockingDeque") || source.contains("java.util.concurrent.LinkedBlockingDeque") }
        .orEmpty()
    val dequeMethods = setOf("removeLast", "removeFirst", "getFirst", "getLast", "addFirst", "addLast", "putFirst", "putLast")
    return source.lineSequence().mapIndexedNotNull { index, line ->
        if (line.contains("LockFreeLinkedListHead")) return@mapIndexedNotNull null
        val invalid = forbidden.findAll(line).any { match ->
            val receiver = line.substring(0, match.range.first).takeLastWhile { it.isLetterOrDigit() || it == '_' }
            receiver !in blockingDeques || match.groupValues[1] !in dequeMethods
        }
        if (invalid) "${index + 1}: ${line.trim()}" else null
    }.toList()
}

internal fun checkJava21Collections(root: File) {
    require(root.isDirectory) { "Source directory not found: $root" }
    val offenders = root.walkTopDown().filter { it.isFile && it.extension == "kt" }.flatMap { file ->
        java21CollectionCalls(file.readText()).map { "${file.relativeTo(root)}:$it" }
    }.toList()
    check(offenders.isEmpty()) {
        "Java 21-only collection calls found (API 35+, minSdk is 28):\n" +
            offenders.joinToString("\n") +
            "\nUse removeAt(index) / first() / last() / add(0, e) / asReversed() instead."
    }
}
