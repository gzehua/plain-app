package com.ismartcoding.plain.preferences

import com.ismartcoding.plain.features.settings.PasswordType
import com.ismartcoding.plain.platform.permissions.Permission
import kotlinx.coroutines.flow.StateFlow

object RustSystemState {
    val state: StateFlow<SystemState> get() = SystemPrefsClient.local.system
    private suspend fun update(command: SystemCommand) = SystemPrefsClient.local.updateSystem(command)

    suspend fun refresh() = Prefs.refresh()
    suspend fun setPassword(password: String) = update(SystemCommand.SetPassword(password))
    suspend fun setPasswordType(value: PasswordType) = update(SystemCommand.SetPasswordType(value.value))
    suspend fun setTwoFactor(enabled: Boolean) = update(SystemCommand.SetTwoFactor(enabled))
    suspend fun setRotateUrlToken(enabled: Boolean) = update(SystemCommand.SetRotateUrlToken(enabled))
    suspend fun resetPassword(): String { update(SystemCommand.ResetPassword); return state.value.password }
    suspend fun resetUrlToken() = update(SystemCommand.ResetUrlToken)
    suspend fun resetAdbToken() = update(SystemCommand.ResetAdbToken)
    suspend fun setApiPermission(permission: Permission, enabled: Boolean) = update(SystemCommand.SetApiPermission(permission.name, enabled))
    suspend fun setApiPermissions(permissions: Set<String>) = update(SystemCommand.SetApiPermissions(permissions))
    suspend fun completeOnboarding() = update(SystemCommand.CompleteOnboarding)
    suspend fun setMdnsHostname(hostname: String) = update(SystemCommand.SetMdnsHostname(hostname))
    suspend fun patchUpdate(patch: UpdateInfoPatch) = update(SystemCommand.PatchUpdate(patch))
    suspend fun verifyAdbToken(token: String): Boolean = SystemPrefsClient.local.verifyAdbToken(token)
}
