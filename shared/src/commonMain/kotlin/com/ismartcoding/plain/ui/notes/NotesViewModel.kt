package com.ismartcoding.plain.ui.notes

import com.ismartcoding.plain.ui.tags.TagsViewModel
import com.ismartcoding.plain.ui.state.ISearchableViewModel
import com.ismartcoding.plain.ui.state.ISelectableViewModel
import com.ismartcoding.plain.ui.state.launchSafe

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.ismartcoding.plain.platform.system.DataType
import com.ismartcoding.plain.features.notes.NotesClient
import com.ismartcoding.plain.features.tags.TagsClient
import com.ismartcoding.plain.models.Note
import com.ismartcoding.plain.models.Tag
import com.ismartcoding.plain.helpers.SearchHelper
import com.ismartcoding.plain.lib.withIO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

// App-lifetime singleton so the HTTP server (GraphQL mutations) and the UI share
// one notes list state: server writes call [reloadAsync]/[updateItem] and every
// collector sees the change without event passing.
object NotesViewModel : ISearchableViewModel<Note>, ISelectableViewModel<Note> {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _itemsFlow = MutableStateFlow<List<Note>>(emptyList())
    override val itemsFlow: StateFlow<List<Note>> = _itemsFlow
    var showLoading = mutableStateOf(true)
    var offset = mutableIntStateOf(0)
    var limit = mutableIntStateOf(200)
    var noMore = mutableStateOf(false)
    var trash = mutableStateOf(false)
    var total = mutableIntStateOf(0)
    var totalTrash = mutableIntStateOf(0)
    var tag = mutableStateOf<Tag?>(null)
    val dataType = DataType.NOTE
    var selectedItem = mutableStateOf<Note?>(null)
    val showTagsDialog = mutableStateOf(false)

    override val showSearchBar = mutableStateOf(false)
    override val searchActive = mutableStateOf(false)
    override val queryText = mutableStateOf("")

    override var selectMode = mutableStateOf(false)
    override val selectedIds = mutableStateListOf<String>()

    suspend fun moreAsync(tagsVM: TagsViewModel) = withIO {
        offset.value += limit.intValue
        val items = NotesClient.search(getQuery(), limit.intValue, offset.value)
        _itemsFlow.update { it + items }
        tagsVM.loadMoreAsync(items.map { it.id }.toSet())
        showLoading.value = false
        noMore.value = items.size < limit.intValue
    }

    // Re-queries the notes list from the DB. Called by the UI and by GraphQL
    // mutations so both write paths converge on this single state.
    suspend fun reloadAsync() = withIO {
        offset.intValue = 0
        _itemsFlow.value = NotesClient.search(getQuery(), limit.intValue, offset.intValue)
        total.intValue = NotesClient.count(getTotalQuery())
        totalTrash.intValue = NotesClient.count(getTrashQuery())
        noMore.value = _itemsFlow.value.size < limit.intValue
        showLoading.value = false
    }

    suspend fun loadAsync(tagsVM: TagsViewModel) {
        reloadAsync()
        tagsVM.loadAsync(_itemsFlow.value.map { it.id }.toSet())
    }

    fun trash(tagsVM: TagsViewModel, ids: Set<String>) {
        scope.launchSafe {
            NotesClient.trashAsync(ids)
            loadAsync(tagsVM)
        }
    }

    fun updateItem(item: Note) {
        _itemsFlow.update {
            val index = it.indexOfFirst { i -> i.id == item.id }
            if (index != -1) {
                it.toMutableList().also { list -> list[index] = item }
            } else {
                listOf(item) + it
            }
        }
    }

    fun restore(tagsVM: TagsViewModel, ids: Set<String>) {
        scope.launchSafe {
            NotesClient.restoreAsync(ids)
            loadAsync(tagsVM)
        }
    }

    fun delete(tagsVM: TagsViewModel, ids: Set<String>) {
        scope.launchSafe {
            NotesClient.deleteAsync(ids)
            loadAsync(tagsVM)
        }
    }

    private fun getTotalQuery(): String {
        return "${SearchHelper.buildTextFilter(queryText.value)} trash:false"
    }

    private fun getTrashQuery(): String {
        return "${SearchHelper.buildTextFilter(queryText.value)} trash:true"
    }

    private suspend fun getQuery(): String {
        var query = "${SearchHelper.buildTextFilter(queryText.value)} trash:${trash.value}"
        if (tag.value != null) {
            val tagId = tag.value!!.id
            val ids = TagsClient.getKeysByTagId(tagId)
            query += " ids:${ids.joinToString(",")}"
        }

        return query
    }
}
