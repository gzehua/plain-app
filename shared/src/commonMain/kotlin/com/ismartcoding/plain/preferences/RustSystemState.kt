package com.ismartcoding.plain.preferences

import com.ismartcoding.plain.rust.native.RustNativeApi



import com.ismartcoding.plain.features.settings.PasswordType
import com.ismartcoding.plain.platform.permissions.Permission
import kotlinx.coroutines.flow.StateFlow
import com.ismartcoding.plain.lib.JsonHelper

object RustSystemState {
    val state: StateFlow<SystemSettings> get() = SystemPrefsClient.local.system
    private suspend inline fun <reified T> set(key: String, value: T) = SystemPrefsClient.local.set(key, JsonHelper.jsonEncodeToElement(value))

    suspend fun refresh() = Prefs.refresh()
    suspend fun setPassword(password: String) = set("password", password)
    suspend fun setPasswordType(value: PasswordType) = set("password_type", value.value)
    suspend fun setTwoFactor(enabled: Boolean) = set("auth_two_factor", enabled)
    suspend fun setRotateUrlToken(enabled: Boolean) = set("rotate_url_token_on_restart", enabled)
    suspend fun resetPassword(): String { SystemPrefsClient.local.resetPassword(); return state.value.password }
    suspend fun resetUrlToken() = SystemPrefsClient.local.resetUrlToken()
    suspend fun resetAdbToken() = SystemPrefsClient.local.resetAdbToken()
    suspend fun setApiPermission(permission: Permission, enabled: Boolean) = set("api_permissions", mapOf(permission.name to enabled))
    suspend fun setApiPermissions(permissions: Set<String>) = set("api_permissions", permissions)
    suspend fun completeOnboarding() = set("onboarding_completed", true)
    suspend fun setMdnsHostname(hostname: String) = set("mdns_hostname", hostname)
    suspend fun patchUpdate(patch: UpdateInfoPatch) = set("update_info", patch)
    suspend fun verifyAdbToken(token: String): Boolean = RustNativeApi.verifyAdbToken(token)
}
