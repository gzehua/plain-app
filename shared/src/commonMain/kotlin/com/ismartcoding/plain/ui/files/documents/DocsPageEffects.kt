package com.ismartcoding.plain.ui.files.documents

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.ismartcoding.plain.Prefs
import com.ismartcoding.plain.lib.Channel
import com.ismartcoding.plain.ui.platform.system.isGestureInteractionMode
import com.ismartcoding.plain.platform.system.AppFeatureType
import com.ismartcoding.plain.platform.system.hasPermission
import com.ismartcoding.plain.events.PermissionsResultEvent
import com.ismartcoding.plain.ui.components.StoragePermissionResumeEffect
import com.ismartcoding.plain.ui.components.refreshStoragePermission
import com.ismartcoding.plain.ui.media.extensions.reset
import com.ismartcoding.plain.ui.files.MediaFoldersViewModel
import com.ismartcoding.plain.ui.tags.TagsViewModel
import com.ismartcoding.plain.ui.navigation.NavLoadGate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DocsPageEffects(
    docsState: DocsPageState,
    docsVM: DocsViewModel,
    tagsVM: TagsViewModel,
    mediaFoldersVM: MediaFoldersViewModel,
    initialLoadGate: NavLoadGate? = null,
) {
    val scope = rememberCoroutineScope()
    val sharedFlow = Channel.sharedFlow
    val reloadAfterGrant: () -> Unit = {
        scope.launch(Dispatchers.Default) {
            docsVM.sortBy.value = Prefs.docSortBy.value
            tagsVM.loadAsync()
            mediaFoldersVM.loadAsync()
            docsVM.loadAsync(tagsVM)
        }
    }

    LaunchedEffect(Unit) {
        docsVM.hasPermission.value = AppFeatureType.FILES.hasPermission()
        if (docsVM.hasPermission.value) {
            initialLoadGate?.await()
            reloadAfterGrant()
        }
    }

    StoragePermissionResumeEffect(docsVM.hasPermission, reloadAfterGrant)

    LaunchedEffect(sharedFlow) {
        sharedFlow.collect { event ->
            if (event is PermissionsResultEvent) {
                refreshStoragePermission(docsVM.hasPermission, reloadAfterGrant)
            }
        }
    }

    LaunchedEffect(docsState.dragSelectState.selectMode, !isGestureInteractionMode()) {
        if (docsState.dragSelectState.selectMode || !isGestureInteractionMode()) {
            docsState.scrollBehavior.reset()
        }
    }
}
