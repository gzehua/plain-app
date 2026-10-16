package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
enum class DbColumnType {
    TEXT,
    INTEGER,
    REAL,
    BLOB,
    NUMERIC,
    UNKNOWN,
}
