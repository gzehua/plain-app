package com.ismartcoding.plain.extensions

import com.ismartcoding.plain.features.files.extension

import com.ismartcoding.plain.api.graphql.models.File as FileEntry

import com.ismartcoding.plain.features.files.FileSortBy

fun List<FileEntry>.sorted(sortBy: FileSortBy): List<FileEntry> {
    val comparator = compareBy<FileEntry> { if (it.isDir) 0 else 1 }
    return when (sortBy) {
        FileSortBy.NAME_ASC -> {
            this.sortedWith(comparator.thenBy { it.name.lowercase() })
        }

        FileSortBy.NAME_DESC -> {
            this.sortedWith(comparator.thenByDescending { it.name.lowercase() })
        }

        FileSortBy.SIZE_ASC -> {
            this.sortedWith(comparator.thenBy { it.size })
        }

        FileSortBy.SIZE_DESC -> {
            this.sortedWith(comparator.thenByDescending { it.size })
        }

        FileSortBy.DATE_ASC -> {
            this.sortedWith(comparator.thenBy { it.updatedAt })
        }

        FileSortBy.DATE_DESC -> {
            this.sortedWith(comparator.thenByDescending { it.updatedAt })
        }

        else -> {
            this.sortedWith(comparator.thenBy { it.name.lowercase() })
        }
    }
}
