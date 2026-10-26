package com.ismartcoding.plain.ui.audio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.ismartcoding.plain.lib.Channel
import com.ismartcoding.plain.ui.platform.system.isGestureInteractionMode
import com.ismartcoding.plain.platform.system.AppFeatureType
import com.ismartcoding.plain.platform.system.hasPermission
import com.ismartcoding.plain.events.PermissionsResultEvent
import com.ismartcoding.plain.ui.components.StoragePermissionResumeEffect
import com.ismartcoding.plain.ui.components.refreshStoragePermission
import com.ismartcoding.plain.ui.media.extensions.reset
import androidx.compose.material3.ExperimentalMaterial3Api
import com.ismartcoding.plain.Prefs
import com.ismartcoding.plain.ui.files.MediaFoldersViewModel
import com.ismartcoding.plain.ui.tags.TagsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AudioPageEffects(
    audioState: AudioPageState,
    audioVM: AudioViewModel,
    audioQueueVM: AudioQueueViewModel,
    tagsVM: TagsViewModel,
    mediaFoldersVM: MediaFoldersViewModel,
) {
    val scope = rememberCoroutineScope()
    val sharedFlow = Channel.sharedFlow
    val reloadAfterGrant: () -> Unit = {
        scope.launch(Dispatchers.Default) {
            audioVM.sortBy.value = Prefs.audioSortBy.value
            audioVM.loadAsync(tagsVM)
            audioQueueVM.loadAsync()
            mediaFoldersVM.loadAsync()
        }
    }

    LaunchedEffect(Unit) {
        audioVM.hasPermission.value = AppFeatureType.FILES.hasPermission()
        if (audioVM.hasPermission.value) {
            reloadAfterGrant()
        }
    }

    StoragePermissionResumeEffect(audioVM.hasPermission, reloadAfterGrant)

    LaunchedEffect(sharedFlow) {
        sharedFlow.collect { event ->
            if (event is PermissionsResultEvent) {
                refreshStoragePermission(audioVM.hasPermission, reloadAfterGrant)
            }
        }
    }

    LaunchedEffect(audioState.dragSelectState.selectMode, !isGestureInteractionMode()) {
        if (audioState.dragSelectState.selectMode || !isGestureInteractionMode()) {
            audioState.scrollBehavior.reset()
        }
    }
}
