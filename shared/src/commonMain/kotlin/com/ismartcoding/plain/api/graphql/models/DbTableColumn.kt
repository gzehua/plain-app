package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class DbTableColumn(
    var name: String,
    var dataType: DbColumnType,
    var notNull: Boolean,
    var defaultValue: String?,
    var primaryKey: Boolean,
)
