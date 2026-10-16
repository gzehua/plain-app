package com.ismartcoding.plain.ui.feeds

import com.ismartcoding.plain.features.feeds.feed

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ismartcoding.plain.features.feeds.CatalogCategory
import com.ismartcoding.plain.features.feeds.CatalogFeed
import com.ismartcoding.plain.features.feeds.FeedsCatalog
import com.ismartcoding.plain.features.feeds.FeedsClient
import com.ismartcoding.plain.ui.state.launchSafe
import com.ismartcoding.plain.lib.withIO

class FeedCatalogViewModel : ViewModel() {
    val categories = mutableStateOf<List<CatalogCategory>>(emptyList())
    val loading = mutableStateOf(true)
    val loadFailed = mutableStateOf(false)
    val busyUrls = mutableStateListOf<String>()

    suspend fun loadAsync() = withIO {
        loading.value = true
        loadFailed.value = try {
            categories.value = FeedsCatalog.loadAsync()
            false
        } catch (e: Exception) {
            true
        }
        loading.value = false
    }

    // Subscribes to a single catalog feed (one-time sync kicked off) or
    // unsubscribes the feed with the same URL, then reloads feedsVM so callers
    // see the updated subscribed set.
    fun toggleSubscribeAsync(feed: CatalogFeed, feedsVM: FeedsViewModel) {
        if (busyUrls.contains(feed.url)) return
        busyUrls.add(feed.url)
        viewModelScope.launchSafe {
            try {
                val existing = FeedsClient.getByUrl(feed.url)
                if (existing == null) {
                    val id = FeedsClient.addAsync {
                        this.url = feed.url
                        this.name = feed.name
                    }
                    FeedsClient.sync(id)
                } else {
                    feedsVM.deleteAsync(setOf(existing.id))
                }
                feedsVM.loadAsync(withCount = true)
            } finally {
                busyUrls.remove(feed.url)
            }
        }
    }
}
