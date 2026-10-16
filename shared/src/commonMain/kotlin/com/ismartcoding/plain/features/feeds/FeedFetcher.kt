package com.ismartcoding.plain.features.feeds

import com.ismartcoding.plain.rust.request.RustContentApi
import com.ismartcoding.plain.platform.network.NetworkType
import com.ismartcoding.plain.platform.network.getNetworkType
import com.ismartcoding.plain.preferences.UserPrefs

object FeedFetcher {
    suspend fun fetchOne(feedId: String) = RustContentApi.sync(feedId)
    suspend fun fetchAll(autoRefresh: Boolean = false) {
        if (shouldSkipAutoRefresh(autoRefresh, UserPrefs.feedAutoRefreshOnlyWifi.value, getNetworkType())) return
        RustContentApi.sync("")
    }
}
internal fun shouldSkipAutoRefresh(autoRefresh: Boolean, onlyWifi: Boolean, networkType: NetworkType): Boolean = autoRefresh && onlyWifi && networkType != NetworkType.WIFI
