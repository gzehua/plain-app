package com.ismartcoding.plain.models

import kotlinx.serialization.Serializable

@Serializable
data class DbTableColumn(
    var name: String,
    var dataType: DbColumnType,
    var notNull: Boolean,
    var defaultValue: String?,
    var primaryKey: Boolean,
)
