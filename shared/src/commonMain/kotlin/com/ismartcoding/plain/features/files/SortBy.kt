package com.ismartcoding.plain.features.files

enum class SortDirection {
    ASC,
    DESC,
}

data class SortBy(val field: String, val direction: SortDirection)
