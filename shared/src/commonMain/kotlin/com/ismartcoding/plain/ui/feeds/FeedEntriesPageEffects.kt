package com.ismartcoding.plain.ui.feeds

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.ismartcoding.plain.events.EventType
import com.ismartcoding.plain.ui.navigation.PBackHandler
import com.ismartcoding.plain.ui.base.pullrefresh.RefreshContentState
import com.ismartcoding.plain.ui.base.pullrefresh.RefreshLayoutState
import com.ismartcoding.plain.ui.base.pullrefresh.setRefreshState
import com.ismartcoding.plain.ui.tags.TagsViewModel
import com.ismartcoding.plain.ui.state.exitSearchMode
import com.ismartcoding.plain.ui.state.exitSelectMode
import com.ismartcoding.plain.ui.state.launchSafe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.ismartcoding.plain.rust.RustEventHandler
import com.ismartcoding.plain.lib.JsonHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FeedEntriesPageEffects(
    feedEntriesVM: FeedEntriesViewModel, feedsVM: FeedsViewModel, tagsVM: TagsViewModel,
    feedId: String, scope: CoroutineScope,
    topRefreshLayoutState: RefreshLayoutState,
    onSearch: (String) -> Unit,
) {
    LaunchedEffect(Unit) {
        tagsVM.dataType.value = feedEntriesVM.dataType
        if (!feedEntriesVM.routeFeedIdApplied) {
            feedEntriesVM.routeFeedIdApplied = true
            feedEntriesVM.feedId.value = feedId
        }
        feedsVM.loadAsync(withCount = true)
        scope.launchSafe { FeedWorkerState.refresh() }
        scope.launch(Dispatchers.IO) { feedEntriesVM.loadAsync(tagsVM) }
    }

    LaunchedEffect(feedEntriesVM, feedsVM, tagsVM) {
        RustEventHandler.events.collect { event ->
            val completed = if (event.type == EventType.FEED_SYNC_UPDATED) {
                JsonHelper.jsonDecode<FeedSyncUpdate>(event.payload).states.lastOrNull { state ->
                    state.status in setOf("COMPLETED", "ERROR", "CANCELLED") &&
                        state.revision >= (FeedWorkerState.revision(state.feedId) ?: -1L)
                }
            } else null
            if (completed != null) {
                topRefreshLayoutState.setRefreshState(
                    if (completed.status == "ERROR") RefreshContentState.Failed else RefreshContentState.Finished,
                )
            }
            if (event.type == EventType.CONTENT_CHANGED || completed != null) {
                feedsVM.loadAsync(withCount = true)
                feedsVM.refreshSelectedItemAsync()
                scope.launch(Dispatchers.IO) { feedEntriesVM.loadAsync(tagsVM) }
            }
        }
    }

    PBackHandler(enabled = feedEntriesVM.selectMode.value || feedEntriesVM.showSearchBar.value) {
        if (feedEntriesVM.selectMode.value) {
            feedEntriesVM.exitSelectMode()
        } else if (feedEntriesVM.showSearchBar.value) {
            if (!feedEntriesVM.searchActive.value || feedEntriesVM.queryText.value.isEmpty()) {
                feedEntriesVM.exitSearchMode(); onSearch("")
            }
        }
    }
}
