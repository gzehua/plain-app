package com.ismartcoding.plain.features.system

import kotlinx.serialization.Serializable

@Serializable
internal data class PackageFacts(
    val item: com.ismartcoding.plain.graphql.models.Package,
    val nameSortKey: String,
)
