package com.ismartcoding.plain.ui.chat

import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.filter
import com.ismartcoding.plain.rust.graphql.AppSubscriptions
import com.ismartcoding.plain.rust.graphql.changes
import com.ismartcoding.plain.ui.state.launchSafe

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateSetOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ismartcoding.plain.features.chat.channel.ChannelManager
import com.ismartcoding.plain.features.chat.channel.snapshotChannelsGraphql
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.models.ChatChannel
import com.ismartcoding.plain.ui.components.ToastManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ChannelViewModel : ViewModel() {
    private val lock = Mutex()
    val channels = MutableStateFlow<List<ChatChannel>>(emptyList())

    val pendingIds = mutableStateSetOf<String>()

    val showCreateChannelDialog = mutableStateOf(false)
    val renameChannelId = mutableStateOf("")

    suspend fun observe() = coroutineScope {
        launch(start = CoroutineStart.UNDISPATCHED) {
            merge(
                AppSubscriptions.contentChanged.changes(),
                AppSubscriptions.channelsChanged.changes(),
            ).collect { refreshSafely() }
        }
        refreshSafely()
    }

    private suspend fun refresh() = lock.withLock {
        channels.value = snapshotChannelsGraphql()
    }

    private suspend fun refreshSafely() {
        try { refresh() }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (error: Exception) { LogCat.e("Load channels", error) }
    }

    fun load() {
        viewModelScope.launch { refreshSafely() }
    }

    fun createChannel(name: String) {
        viewModelScope.launchSafe {
            ChannelManager.createChannel(name)
            showCreateChannelDialog.value = false
        }
    }

    fun renameChannel(channelId: String, newName: String) {
        viewModelScope.launchSafe {
            ChannelManager.renameChannel(channelId, newName)
            renameChannelId.value = ""
        }
    }

    fun removeChannel(channelId: String) {
        viewModelScope.launchSafe {
            ChannelManager.deleteChannel(channelId)
        }
    }

    fun leaveChannel(channelId: String) {
        viewModelScope.launchSafe {
            ChannelManager.leaveChannel(channelId)
        }
    }

    fun inviteMember(channelId: String, peerId: String) {
        viewModelScope.launchSafe(onDone = {
            pendingIds.remove(peerId)
        }) {
            pendingIds.add(peerId)
            ChannelManager.inviteMember(channelId, peerId)
        }
    }

    fun resendInvite(channelId: String, peerId: String) {
        viewModelScope.launchSafe {
            ChannelManager.resendInvite(channelId, peerId)
        }
    }

    fun kickMember(channelId: String, peerId: String) {
        viewModelScope.launchSafe(onDone = {
            pendingIds.remove(peerId)
        }) {
            pendingIds.add(peerId)
            ChannelManager.kickMember(channelId, peerId)
        }
    }

    fun acceptInvite(
        channelId: String,
        onSuccess: () -> Unit = {},
        onDone: () -> Unit = {},
    ) {
        viewModelScope.launchSafe(onDone = onDone) {
            val r = ChannelManager.acceptInvite(channelId)
            if (r.isSuccess) {
                onSuccess()
            } else {
                ToastManager.showErrorToast(r.getError())
            }
        }
    }

    fun declineInvite(
        channelId: String,
        onDone: () -> Unit = {},
    ) {
        viewModelScope.launchSafe(onDone = onDone) {
            ChannelManager.declineInvite(channelId)
        }
    }
}
