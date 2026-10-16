package com.ismartcoding.plain.ui.tags

import com.ismartcoding.plain.ui.state.launchSafe

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ismartcoding.plain.features.tags.TagRelationStub
import com.ismartcoding.plain.db.IData
import com.ismartcoding.plain.platform.system.DataType
import com.ismartcoding.plain.features.tags.TagsClient
import com.ismartcoding.plain.api.graphql.models.TagHostRecord
import com.ismartcoding.plain.api.graphql.models.TagHostRelation
import com.ismartcoding.plain.lib.TimeHelper
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.ui.components.LoadingHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TagsViewModel : ViewModel() {
    private val _itemsFlow = MutableStateFlow<List<TagHostRecord>>(emptyList())
    val itemsFlow: StateFlow<List<TagHostRecord>> = _itemsFlow
    private val _tagsMapFlow = MutableStateFlow(mutableMapOf<String, List<TagHostRelation>>())
    val tagsMapFlow = _tagsMapFlow.asStateFlow()
    var showLoading = mutableStateOf(true)
    var tagNameDialogVisible = mutableStateOf(false)
    var editItem = mutableStateOf<TagHostRecord?>(null)
    var editTagName = mutableStateOf("")
    var dataType = mutableStateOf(DataType.DEFAULT)

    fun updateTagsMap(map: Map<String, List<TagHostRelation>>) {
        _tagsMapFlow.value = map.toMutableMap()
    }

    suspend fun loadAsync(keys: Set<String> = emptySet()) = withIO {
        val startTime = TimeHelper.now().toEpochMilliseconds()
        _itemsFlow.value = TagsClient.getAll(dataType.value)
        if (keys.isNotEmpty()) {
            refreshRelations(keys)
        }
        LoadingHelper.ensureMinimumLoadingTime(
            viewModel = this@TagsViewModel,
            startTime = startTime,
            updateLoadingState = { isLoading -> showLoading.value = isLoading }
        )
    }

    fun loadMoreAsync(keys: Set<String>) {
        if (keys.isNotEmpty()) {
            viewModelScope.launchSafe {
                refreshRelations(keys)
            }
        }
    }

    suspend fun addTagAsync(name: String) = withIO {
        val id = TagsClient.addOrUpdate("") {
            this.name = name
            type = dataType.value.value
        }
        val created = checkNotNull(TagsClient.get(id))
        _itemsFlow.update { it + created }
        tagNameDialogVisible.value = false
    }

    suspend fun editTagAsync(name: String) = withIO {
        val id = TagsClient.addOrUpdate(editItem.value!!.id) {
            this.name = name
        }
        _itemsFlow.update { list ->
            list.map { if (it.id == id) it.copy(name = name) else it }
        }
        tagNameDialogVisible.value = false
    }

    fun deleteTag(id: String) {
        viewModelScope.launchSafe {
            TagsClient.delete(id)
            _itemsFlow.update { it.filterNot { i -> i.id == id } }
            for (key in _tagsMapFlow.value.keys) {
                _tagsMapFlow.value[key] = _tagsMapFlow.value[key]?.filter { it.tagId != id } ?: emptyList()
            }
        }
    }

    fun showAddDialog() {
        editTagName.value = ""
        editItem.value = null
        tagNameDialogVisible.value = true
    }

    fun showEditDialog(tag: TagHostRecord) {
        editTagName.value = tag.name
        editItem.value = tag
        tagNameDialogVisible.value = true
    }

    fun removeFromTags(ids: Set<String>, tagIds: Set<String>) {
        viewModelScope.launchSafe {
            TagsClient.deleteTagRelationByKeysTagIds(ids, tagIds)
            for (id in ids) {
                tagsMapFlow.value.toMutableMap().let { map ->
                    map[id] = map[id]?.filter { !tagIds.contains(it.tagId) } ?: emptyList()
                    updateTagsMap(map)
                }
            }
            loadAsync()
        }
    }

    fun addToTags(items: List<IData>, tagIds: Set<String>) {
        viewModelScope.launchSafe {
            TagsClient.addTagRelations(items.flatMap { item ->
                tagIds.map { TagRelationStub.create(item).toTagRelation(it, dataType.value) }
            })
            refreshRelations(items.map { it.id }.toSet())
            loadAsync()
        }
    }

    suspend fun toggleTagAsync(data: IData, tagId: String) = withIO {
        val tagIds = tagsMapFlow.value[data.id]?.map { it.tagId } ?: emptyList()
        if (tagIds.contains(tagId)) {
            TagsClient.deleteTagRelationByKeysTagId(setOf(data.id), tagId)
        } else {
            TagsClient.addTagRelations(listOf(TagRelationStub.create(data).toTagRelation(tagId, dataType.value)))
        }
        refreshRelations(setOf(data.id))
        loadAsync()
    }

    private suspend fun refreshRelations(keys: Set<String>) {
        val fresh = TagsClient.getTagRelationsByKeysMap(keys, dataType.value)
        _tagsMapFlow.update { current -> current.toMutableMap().apply {
            keys.forEach { this[it] = fresh[it].orEmpty() }
        } }
    }
}
