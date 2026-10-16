package com.ismartcoding.plain.ui.search

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import com.ismartcoding.plain.ui.base.dragselect.DragSelectState
import com.ismartcoding.plain.ui.base.dragselect.rememberDragSelectState
import com.ismartcoding.plain.ui.media.viewer.PreviewItem
import com.ismartcoding.plain.ui.media.viewer.previewer.MediaPreviewerState
import com.ismartcoding.plain.ui.media.viewer.previewer.TransformItemState
import com.ismartcoding.plain.ui.media.viewer.previewer.rememberPreviewerState
import com.ismartcoding.plain.ui.media.viewer.previewer.rememberTransformItemState
import com.ismartcoding.plain.ui.audio.AudioQueueViewModel
import com.ismartcoding.plain.ui.audio.AudioViewModel
import com.ismartcoding.plain.ui.casting.CastViewModel
import com.ismartcoding.plain.ui.files.documents.DocsViewModel
import com.ismartcoding.plain.ui.feeds.FeedEntriesViewModel
import com.ismartcoding.plain.ui.search.GlobalSearchHit
import com.ismartcoding.plain.ui.search.GlobalSearchSource
import com.ismartcoding.plain.ui.tags.TagsViewModel

/** Per-list dependencies for rows that reuse the source page's own list item component. */
class GlobalSearchRowContext(
    val navController: NavHostController,
    val audioQueueVM: AudioQueueViewModel,
    val audioVM: AudioViewModel,
    val tagsVM: TagsViewModel,
    val castVM: CastViewModel,
    val docsVM: DocsViewModel,
    val feedEntriesVM: FeedEntriesViewModel,
    val dragSelectState: DragSelectState,
    val itemState: TransformItemState,
    val previewerState: MediaPreviewerState,
)

@Composable
fun rememberGlobalSearchRowContext(
    navController: NavHostController,
    audioQueueVM: AudioQueueViewModel,
    audioVM: AudioViewModel,
    tagsVM: TagsViewModel,
    castVM: CastViewModel,
    docsVM: DocsViewModel,
    feedEntriesVM: FeedEntriesViewModel,
): GlobalSearchRowContext {
    val dragSelectState = rememberDragSelectState()
    val itemState = rememberTransformItemState()
    val previewerState = rememberPreviewerState()
    return remember(
        navController, audioQueueVM, audioVM, tagsVM, castVM, docsVM, feedEntriesVM,
        dragSelectState, itemState, previewerState,
    ) {
        GlobalSearchRowContext(
            navController, audioQueueVM, audioVM, tagsVM, castVM, docsVM, feedEntriesVM,
            dragSelectState, itemState, previewerState,
        )
    }
}

/** Preview payload of an image/video hit, or null for other domains. */
fun GlobalSearchHit.previewItem(): PreviewItem? = when (val s = source) {
    is GlobalSearchSource.Image -> PreviewItem(s.image.id, s.image.path, s.image.size, mediaId = s.image.id, data = s.image)
    is GlobalSearchSource.Video -> PreviewItem(s.video.id, s.video.path, s.video.size, mediaId = s.video.id, data = s.video)
    else -> null
}
