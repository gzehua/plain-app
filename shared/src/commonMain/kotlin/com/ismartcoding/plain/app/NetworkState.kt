package com.ismartcoding.plain.app

import com.ismartcoding.plain.api.graphql.Query
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.models.NetworkStatus
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable

object NetworkState {
    private val current = MutableStateFlow(NetworkStatus(emptyList(), ""))
    val network = current.asStateFlow()
    private val refreshLock = Mutex()

    suspend fun refresh() = refreshLock.withLock {
        try {
            current.value = JsonHelper.jsonDecodeFromElement<AppNetwork>(
                RustGraphqlApi.graphql(Query.appNetwork()).getValue("app")
            ).network
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            LogCat.e("Network refresh failed", error)
        }
    }
}

@Serializable
private data class AppNetwork(val network: NetworkStatus)
