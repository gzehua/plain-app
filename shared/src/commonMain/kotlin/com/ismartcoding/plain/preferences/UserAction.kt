package com.ismartcoding.plain.preferences

import com.ismartcoding.plain.rust.graphql.gql

internal sealed class UserAction {
    data class RecordSearch(val value: String) : UserAction()
    data class RemoveSearch(val value: String) : UserAction()
    data class RecordSaveDirectory(val value: String) : UserAction()
    data class SetNotificationMode(val value: String) : UserAction()
    data object ClearNotificationApps : UserAction()

    fun selection(): String = when (this) {
        is RecordSearch -> "recordRecentSearch(query: ${gql(value)})"
        is RemoveSearch -> "removeRecentSearch(query: ${gql(value)})"
        is RecordSaveDirectory -> "recordRecentSaveDirectory(path: ${gql(value)})"
        is SetNotificationMode -> "setNotificationFilterMode(mode: ${gql(value)})"
        ClearNotificationApps -> "clearNotificationFilterApps"
    } + " { affectedCount }"
}
