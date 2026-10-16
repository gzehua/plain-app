package com.ismartcoding.plain.ui.permissions
import com.ismartcoding.plain.platform.permissions.isGranted
import com.ismartcoding.plain.platform.permissions.Permission

import org.jetbrains.compose.resources.DrawableResource

data class PermissionItem(
    val icon: DrawableResource?,
    val permission: Permission,
    val permissions: Set<Permission>,
    var granted: Boolean = false,
) {
    companion object {
        fun create(
            icon: DrawableResource?,
            permission: Permission,
            permissions: Set<Permission> = setOf(permission),
        ): PermissionItem {
            return PermissionItem(icon, permission, permissions).apply {
                granted = permissions.all { it.isGranted() }
            }
        }
    }
}
