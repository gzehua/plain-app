package com.ismartcoding.plain.preferences

import com.ismartcoding.plain.rust.graphql.gql

internal sealed class PrefAction {
    data class SetNotificationMode(val value: String) : PrefAction()
    data object ClearNotificationApps : PrefAction()

    fun selection(): String = when (this) {
        is SetNotificationMode -> "setNotificationFilterMode(mode: ${gql(value)})"
        ClearNotificationApps -> "clearNotificationFilterApps"
    } + " { affectedCount }"
}
