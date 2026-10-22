package com.ismartcoding.plain.preferences

import com.ismartcoding.plain.app.events.HostEvent
import com.ismartcoding.plain.app.events.EventType
import com.ismartcoding.plain.lib.sendEvent
import com.ismartcoding.plain.features.permissions.getGrantedWebPermissionsAsync
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.JsonObject

import com.ismartcoding.plain.rust.native.RustNativeApi

import com.ismartcoding.plain.features.settings.PasswordType
import com.ismartcoding.plain.platform.permissions.Permission
import kotlinx.coroutines.flow.StateFlow
import com.ismartcoding.plain.lib.JsonHelper

object SecuritySettingsState {
    private var snapshot: MutableStateFlow<SecuritySettings>? = null
    val state: StateFlow<SecuritySettings> get() = checkNotNull(snapshot)
    internal suspend fun apply(value: JsonObject) {
        val next = JsonHelper.jsonDecodeFromElement<SecuritySettings>(value)
        val previous = snapshot?.value
        snapshot?.let { it.value = next } ?: run { snapshot = MutableStateFlow(next) }
        if (previous != null && previous.apiPermissions != next.apiPermissions) {
            sendEvent(HostEvent(
                EventType.PERMISSIONS_UPDATED,
                JsonHelper.jsonEncode(getGrantedWebPermissionsAsync()),
            ))
        }
    }
    private suspend inline fun <reified T> set(key: String, value: T) = PrefStore.write(key, JsonHelper.jsonEncodeToElement(value))

    suspend fun setPassword(password: String) = set("password", password)
    suspend fun setPasswordType(value: PasswordType) = set("password_type", value.value)
    suspend fun setLoginRequireApproval(enabled: Boolean) = set("login_require_approval", enabled)
    suspend fun setRotateUrlToken(enabled: Boolean) = set("rotate_url_token_on_restart", enabled)
    suspend fun resetPassword() = RustNativeApi.resetPassword()
    suspend fun resetUrlToken() = RustNativeApi.resetUrlToken()
    suspend fun resetAdbToken() = RustNativeApi.resetAdbToken()
    suspend fun setApiPermission(permission: Permission, enabled: Boolean) = set("api_permissions", mapOf(permission.name to enabled))
    suspend fun setApiPermissions(permissions: Set<String>) = set("api_permissions", permissions)
    suspend fun completeOnboarding() = RustNativeApi.completeOnboarding()
    suspend fun setMdnsHostname(hostname: String) = set("mdns_hostname", hostname)
    suspend fun setAutoCheckUpdate(value: Boolean) = set("auto_check_update", value)
    suspend fun setSkipVersion(value: String) = set("skip_version", value)
    suspend fun verifyAdbToken(token: String): Boolean = RustNativeApi.verifyAdbToken(token)
}
