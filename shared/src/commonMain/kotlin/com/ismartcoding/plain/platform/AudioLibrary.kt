package com.ismartcoding.plain.platform

import com.ismartcoding.plain.features.file.FileSortBy
import com.ismartcoding.plain.graphql.models.AudioItem

expect suspend fun audioLibraryCount(): Int
expect suspend fun audioLibraryPage(offset: Int, limit: Int, sort: FileSortBy): List<AudioItem>
expect suspend fun audioLibraryContains(path: String): Boolean

expect suspend fun audioLibraryMetadata(path: String): AudioItem
