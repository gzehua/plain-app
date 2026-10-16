package com.ismartcoding.plain.api.graphql.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class SystemSettings(
    var password: String,
    var passwordType: Int,
    var authTwoFactor: Boolean,
    var rotateUrlTokenOnRestart: Boolean,
    var adbToken: String,
    var updateInfo: JsonElement,
    var urlToken: String,
    var apiPermissions: List<String>,
    var onboardingCompleted: Boolean,
    var clientId: String,
    var mdnsHostname: String,
    var signaturePublicKey: String,
    var appUpdateAvailable: Boolean = false,
)
