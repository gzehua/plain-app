package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class SystemSettingsInput(
    var action: SystemSettingsAction,
    var password: String?,
    var value: Int?,
    var enabled: Boolean?,
    var permission: String?,
    var permissions: List<String>?,
    var hostname: String?,
    var patch: JsonElement?,
)
