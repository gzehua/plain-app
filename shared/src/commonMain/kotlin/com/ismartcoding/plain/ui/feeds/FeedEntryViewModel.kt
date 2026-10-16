package com.ismartcoding.plain.ui.feeds

import com.ismartcoding.plain.features.feeds.feed

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ismartcoding.plain.features.feeds.FeedFontScale
import com.ismartcoding.plain.api.graphql.models.Feed
import com.ismartcoding.plain.api.graphql.models.FeedEntry
import com.ismartcoding.plain.preferences.*
import kotlinx.coroutines.launch

class FeedEntryViewModel : ViewModel() {
    val showSelectTagsDialog = mutableStateOf(false)
    val item = mutableStateOf<FeedEntry?>(null)
    val feed = mutableStateOf<Feed?>(null)
    val content = mutableStateOf("")
    val fetchingContent = mutableStateOf(false)

    // Article text scale: index into FeedFontScale.values, persisted.
    val fontScaleIndex = mutableIntStateOf(UserPrefs.feedFontScale.value)

    init {
        viewModelScope.launch {
            fontScaleIndex.intValue = UserPrefs.feedFontScale.value
        }
    }

    fun setFontScaleIndex(index: Int) {
        fontScaleIndex.intValue = index
        viewModelScope.launch {
            UserPrefs.feedFontScale.value = index
        }
    }
}
