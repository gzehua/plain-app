package com.ismartcoding.plain.preferences

import com.ismartcoding.plain.models.UpdateInfo

import kotlinx.serialization.Serializable

@Serializable
data class SecuritySettings(
    val password: String,
    val passwordType: Int,
    val loginRequireApproval: Boolean,
    val rotateUrlTokenOnRestart: Boolean,
    val adbToken: String,
    val updateInfo: UpdateInfo,
    val autoCheckUpdate: Boolean,
    val skipVersion: String,
    val appUpdateAvailable: Boolean,
    val urlToken: String,
    val apiPermissions: Set<String>,
    val onboardingCompleted: Boolean,
    val clientId: String,
    val mdnsHostname: String,
    val signaturePublicKey: String,
)
