package com.ismartcoding.plain.preferences

import com.ismartcoding.plain.api.graphql.models.UpdateInfo

import kotlinx.serialization.Serializable

@Serializable
data class SystemSettings(
    val password: String,
    val passwordType: Int,
    val authTwoFactor: Boolean,
    val rotateUrlTokenOnRestart: Boolean,
    val adbToken: String,
    val updateInfo: UpdateInfo,
    val appUpdateAvailable: Boolean = false,
    val urlToken: String,
    val apiPermissions: Set<String>,
    val onboardingCompleted: Boolean,
    val clientId: String,
    val mdnsHostname: String,
    val signaturePublicKey: String,
)
