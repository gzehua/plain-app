package com.ismartcoding.plain.ui.feeds

import kotlinx.coroutines.flow.collect

import com.ismartcoding.plain.ui.state.launchSafe

import com.ismartcoding.plain.preferences.*

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.app.Constants
import com.ismartcoding.plain.platform.system.DataType
import com.ismartcoding.plain.features.tags.TagsClient
import com.ismartcoding.plain.features.feeds.FeedEntriesClient
import com.ismartcoding.plain.lib.TimeHelper
import kotlinx.coroutines.launch
import kotlin.time.Instant

class FeedSettingsViewModel : ViewModel() {
    var showIntervalDialog = mutableStateOf(false)
    var autoRefresh = mutableStateOf(true)
    var autoRefreshInterval = mutableIntStateOf(7200)
    var autoRefreshOnlyWifi = mutableStateOf(false)
    var showClearFeedsDialog = mutableStateOf(false)
    var clearFeedItemsTs = mutableLongStateOf(Constants.ONE_DAY * 7)

    init {
        viewModelScope.launch { UserPrefs.feedAutoRefresh.collect { autoRefresh.value = it } }
        viewModelScope.launch { UserPrefs.feedAutoRefreshInterval.collect { autoRefreshInterval.intValue = it } }
        viewModelScope.launch { UserPrefs.feedAutoRefreshOnlyWifi.collect { autoRefreshOnlyWifi.value = it } }
    }

    fun setAutoRefresh(value: Boolean) {
        viewModelScope.launchSafe {
            UserPrefs.feedAutoRefresh.value = value
        }
    }

    fun setAutoRefreshInterval(value: Int) {
        viewModelScope.launchSafe {
            UserPrefs.feedAutoRefreshInterval.value = value
        }
    }

    fun setAutoRefreshOnlyWifi(value: Boolean) {
        viewModelScope.launchSafe {
            UserPrefs.feedAutoRefreshOnlyWifi.value = value
        }
    }

    suspend fun clearByFeedIdAsync(feedId: String) = withIO {
        FeedEntriesClient.deleteQuery("feed_id:$feedId")
    }

    fun clearAllAsync() {
        viewModelScope.launchSafe {
            FeedEntriesClient.deleteAllAsync()
        }
    }

    suspend fun clearByTimeAsync(ts: Long) = withIO {
        val time = TimeHelper.now().epochSeconds - ts
        FeedEntriesClient.deleteQuery("created_at:<${Instant.fromEpochSeconds(time)}")
    }
}
