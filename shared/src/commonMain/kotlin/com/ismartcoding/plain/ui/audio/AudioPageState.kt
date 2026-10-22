package com.ismartcoding.plain.ui.audio

import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.getValue
import com.ismartcoding.plain.platform.system.AppFeatureType
import com.ismartcoding.plain.platform.system.has

import com.ismartcoding.plain.models.Audio as AudioTrack
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import com.ismartcoding.plain.models.Audio
import com.ismartcoding.plain.models.MediaBucket
import com.ismartcoding.plain.models.Tag
import com.ismartcoding.plain.models.TagRelation
import com.ismartcoding.plain.ui.base.dragselect.DragSelectState
import com.ismartcoding.plain.ui.base.dragselect.rememberListDragSelectState
import com.ismartcoding.plain.ui.audio.AudioViewModel
import com.ismartcoding.plain.ui.files.MediaFoldersViewModel
import com.ismartcoding.plain.ui.tags.TagsViewModel

@OptIn(ExperimentalMaterial3Api::class)
data class AudioPageState(
    val itemsState: List<AudioTrack>,
    val dragSelectState: DragSelectState,
    val scrollBehavior: TopAppBarScrollBehavior,
    val tagsState: List<Tag>,
    val tagsMapState: Map<String, List<TagRelation>>,
    val scrollState: LazyListState,
    val bucketsMap: Map<String, MediaBucket>
) {
    companion object {
        @OptIn(ExperimentalMaterial3Api::class)
        @Composable
        fun create(
            audioVM: AudioViewModel,
            tagsVM: TagsViewModel,
            mediaFoldersVM: MediaFoldersViewModel,
        ): AudioPageState {
            LaunchedEffect(Unit) {
                tagsVM.dataType.value = audioVM.dataType
                mediaFoldersVM.dataType.value = audioVM.dataType
            }

            val tagsState by tagsVM.itemsFlow.collectAsState()
            val itemsState by audioVM.itemsFlow.collectAsState()
            val scrollState = rememberLazyListState()
            val dragSelectState = rememberListDragSelectState({ scrollState })
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(canScroll = {
                scrollState.firstVisibleItemIndex > 0 && !dragSelectState.selectMode
            })

            val tagsMapState by tagsVM.tagsMapFlow.collectAsState()
            val bucketsMap by mediaFoldersVM.bucketsMapFlow.collectAsState()

            return AudioPageState(
                itemsState = itemsState,
                dragSelectState = dragSelectState,
                scrollBehavior = scrollBehavior,
                tagsState = tagsState,
                tagsMapState = tagsMapState,
                scrollState = scrollState,
                bucketsMap = bucketsMap
            )
        }
    }
}
