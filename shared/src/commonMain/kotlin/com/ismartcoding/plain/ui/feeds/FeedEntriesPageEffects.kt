package com.ismartcoding.plain.ui.feeds

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.ismartcoding.plain.app.events.EventType
import com.ismartcoding.plain.app.events.HFeedStatusEvent
import com.ismartcoding.plain.features.feeds.FeedWorkerStatus
import com.ismartcoding.plain.models.Feed
import com.ismartcoding.plain.lib.Channel
import com.ismartcoding.plain.platform.IODispatcher
import com.ismartcoding.plain.ui.navigation.PBackHandler
import com.ismartcoding.plain.ui.base.pullrefresh.RefreshContentState
import com.ismartcoding.plain.ui.base.pullrefresh.RefreshLayoutState
import com.ismartcoding.plain.ui.base.pullrefresh.setRefreshState
import com.ismartcoding.plain.ui.feeds.FeedEntriesViewModel
import com.ismartcoding.plain.ui.feeds.FeedsViewModel
import com.ismartcoding.plain.ui.tags.TagsViewModel
import com.ismartcoding.plain.ui.state.exitSearchMode
import com.ismartcoding.plain.ui.state.exitSelectMode
import com.ismartcoding.plain.ui.state.launchSafe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
        scope.launch(IODispatcher) { feedEntriesVM.loadAsync(tagsVM) }
    }

    LaunchedEffect(feedEntriesVM, feedsVM, tagsVM) {
        com.ismartcoding.plain.rust.RustEventHandler.events.collect { event ->
            if (event.type == EventType.CONTENT_CHANGED.name) {
                feedsVM.loadAsync(withCount = true)
                feedsVM.refreshSelectedItemAsync()
                scope.launch(IODispatcher) { feedEntriesVM.loadAsync(tagsVM) }
            }
        }
    }

    LaunchedEffect(Channel.sharedFlow) {
        Channel.sharedFlow.collect { event ->
            if (event is HFeedStatusEvent) {
                if (event.status == FeedWorkerStatus.COMPLETED || event.status == FeedWorkerStatus.ERROR || event.status == FeedWorkerStatus.CANCELLED) {
                    topRefreshLayoutState.setRefreshState(
                        if (event.status == FeedWorkerStatus.ERROR) RefreshContentState.Failed else RefreshContentState.Finished,
                    )
                    // Sync outcomes are persisted on Feed by Rust; reload so the
                    // banner, drawer dots and the open feed sheet reflect the new state.
                    feedsVM.loadAsync(withCount = true)
                    feedsVM.refreshSelectedItemAsync()
                    scope.launch(IODispatcher) { feedEntriesVM.loadAsync(tagsVM) }
                }
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
