package com.ismartcoding.plain.ui.feeds

import com.ismartcoding.plain.features.feeds.feed

import com.ismartcoding.plain.ui.state.ISelectableViewModel
import com.ismartcoding.plain.ui.state.launchSafe

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ismartcoding.plain.platform.system.DataType
import com.ismartcoding.plain.features.tags.TagsClient
import com.ismartcoding.plain.features.feeds.FeedEntriesClient
import com.ismartcoding.plain.features.feeds.FeedsClient
import com.ismartcoding.plain.api.graphql.models.Feed
import com.ismartcoding.plain.i18n.Res
import com.ismartcoding.plain.i18n.already_added
import com.ismartcoding.plain.i18n.error
import com.ismartcoding.plain.i18n.invalid_url
import com.ismartcoding.plain.lib.extensions.isUrl
import com.ismartcoding.plain.lib.rss.model.RssChannel
import com.ismartcoding.plain.ui.i18n.LocaleHelper
import com.ismartcoding.plain.features.feeds.fetchRssChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class FeedsViewModel : ISelectableViewModel<Feed>, ViewModel() {
    private val _itemsFlow = MutableStateFlow<List<Feed>>(emptyList())
    override val itemsFlow: StateFlow<List<Feed>> = _itemsFlow
    var showLoading = mutableStateOf(true)
    var showAddDialog = mutableStateOf(false)
    var showEditDialog = mutableStateOf(false)
    var selectedItem = mutableStateOf<Feed?>(null)
    internal var editId = mutableStateOf("")
    var editUrl = mutableStateOf("")
    var editName = mutableStateOf("")
    var editFetchContent = mutableStateOf(false)
    var editUrlError = mutableStateOf("")
    var rssChannel = mutableStateOf<RssChannel?>(null)

    override var selectMode = mutableStateOf(false)
    override val selectedIds = mutableStateListOf<String>()

    fun loadAsync(withCount: Boolean = false) {
        viewModelScope.launchSafe {
            val countMap = if (withCount) {
                FeedsClient.getFeedCounts().associate { it.id to it.count }
            } else {
                emptyMap()
            }
            _itemsFlow.value = FeedsClient.getAll().map {
                it.entryCount = countMap[it.id] ?: 0
                it
            }
            showLoading.value = false
        }
    }

    fun updateFetchContent(id: String, value: Boolean) {
        viewModelScope.launchSafe {
            FeedsClient.updateAsync(id) {
                this.fetchContent = value
            }
        }
    }

    /** Reloads the feed shown in ViewFeedBottomSheet so its sync status card stays fresh. */
    fun refreshSelectedItemAsync() {
        val id = selectedItem.value?.id ?: return
        viewModelScope.launchSafe {
            val fresh = FeedsClient.getById(id) ?: return@launchSafe
            fresh.entryCount = selectedItem.value?.entryCount ?: 0
            selectedItem.value = fresh
        }
    }

    fun delete(ids: Set<String>) {
        viewModelScope.launchSafe { deleteAsync(ids) }
    }

    suspend fun deleteAsync(ids: Set<String>) {
        FeedsClient.deleteAsync(ids)
        _itemsFlow.update { it.filterNot { i -> ids.contains(i.id) } }
    }

    fun add() {
        editUrlError.value = ""
        viewModelScope.launchSafe {
            val id = FeedsClient.addAsync {
                this.url = editUrl.value
                this.name = editName.value
                this.fetchContent = editFetchContent.value
            }
            loadAsync(withCount = true)
            showAddDialog.value = false
        }
    }

    fun fetchChannel() {
        editUrlError.value = ""
        if (!editUrl.value.isUrl()) {
            editUrlError.value = LocaleHelper.getString(Res.string.invalid_url)
            return
        }
        viewModelScope.launchSafe {
            if (FeedsClient.getByUrl(editUrl.value) != null) {
                editUrlError.value = LocaleHelper.getStringAsync(Res.string.already_added)
                return@launchSafe
            }
            try {
                rssChannel.value = fetchRssChannel(editUrl.value)
                rssChannel.value?.let {
                    editName.value = it.title ?: ""
                }
            } catch (e: Exception) {
                editUrlError.value = e.message ?: LocaleHelper.getStringAsync(Res.string.error)
            }
        }
    }

    fun edit() {
        editUrlError.value = ""
        if (!editUrl.value.isUrl()) {
            editUrlError.value = LocaleHelper.getString(Res.string.invalid_url)
            return
        }
        viewModelScope.launchSafe {
            val a = FeedsClient.getByUrl(editUrl.value)
            if (a != null && a.id != editId.value) {
                editUrlError.value = LocaleHelper.getStringAsync(Res.string.already_added)
                return@launchSafe
            }
            FeedsClient.updateAsync(editId.value) {
                this.name = editName.value
                this.url = editUrl.value
                this.fetchContent = editFetchContent.value
            }
            loadAsync(withCount = true)
            showEditDialog.value = false
        }
    }

    fun showAddDialog() {
        rssChannel.value = null
        editUrlError.value = ""
        editUrl.value = ""
        editName.value = ""
        editFetchContent.value = false
        showAddDialog.value = true
    }

    fun showEditDialog(item: Feed) {
        editUrlError.value = ""
        editId.value = item.id
        editUrl.value = item.url
        editName.value = item.name
        editFetchContent.value = item.fetchContent
        showEditDialog.value = true
    }

}
