package com.ismartcoding.plain.preferences

import com.ismartcoding.plain.rust.request.gql
import com.ismartcoding.plain.rust.request.gqlIds

internal sealed class UserAction {
    data class RecordSearch(val value: String) : UserAction()
    data class RemoveSearch(val value: String) : UserAction()
    data object ClearSearches : UserAction()
    data class RecordSaveDirectory(val value: String) : UserAction()
    data class ToggleNotificationApp(val value: String) : UserAction()
    data class SetNotificationMode(val value: String) : UserAction()
    data class DeleteVideos(val value: List<String>) : UserAction()
    data object ClearNotificationApps : UserAction()

    fun selection(): String = when (this) {
        is RecordSearch -> "recordRecentSearch(query: ${gql(value)})"
        is RemoveSearch -> "removeRecentSearch(query: ${gql(value)})"
        ClearSearches -> "clearRecentSearches"
        is RecordSaveDirectory -> "recordRecentSaveDirectory(path: ${gql(value)})"
        is ToggleNotificationApp -> "toggleNotificationApp(packageName: ${gql(value)})"
        is SetNotificationMode -> "setNotificationFilterMode(mode: ${gql(value)})"
        is DeleteVideos -> "deletePlaylistVideos(paths: ${gqlIds(value)})"
        ClearNotificationApps -> "clearNotificationFilterApps"
    } + " { affectedCount }"
}
