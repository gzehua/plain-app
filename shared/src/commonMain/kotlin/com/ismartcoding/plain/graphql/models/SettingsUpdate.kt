package com.ismartcoding.plain.graphql.models

import kotlinx.serialization.Serializable

@Serializable
data class SettingsUpdate(
    var result: ActionResult,
    var systemSettings: SystemSettings,
    var userSettings: UserSettings,
)
