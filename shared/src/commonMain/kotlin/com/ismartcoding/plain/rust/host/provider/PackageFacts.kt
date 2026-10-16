package com.ismartcoding.plain.rust.host.provider

import kotlinx.serialization.Serializable

@Serializable
internal data class PackageFacts(
    val item: com.ismartcoding.plain.api.graphql.models.Package,
    val nameSortKey: String,
)
