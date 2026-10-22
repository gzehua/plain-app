package com.ismartcoding.plain.ui.feeds

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.ismartcoding.plain.models.Tag
import com.ismartcoding.plain.i18n.*
import com.ismartcoding.plain.ui.base.BottomActionButtons
import com.ismartcoding.plain.ui.components.IconTextSmallButtonDelete
import com.ismartcoding.plain.ui.components.IconTextSmallButtonLabel
import com.ismartcoding.plain.ui.components.IconTextSmallButtonLabelOff
import com.ismartcoding.plain.ui.base.PBottomAppBar
import com.ismartcoding.plain.ui.base.PIconTextSmallButton
import com.ismartcoding.plain.ui.feeds.FeedEntriesViewModel
import com.ismartcoding.plain.ui.tags.TagsViewModel
import com.ismartcoding.plain.ui.state.exitSelectMode
import com.ismartcoding.plain.ui.state.getSelectedItems
import com.ismartcoding.plain.ui.tags.BatchSelectTagsDialog
import com.ismartcoding.plain.ui.resources.Res as UiRes
import com.ismartcoding.plain.ui.resources.circle_check as ui_drawable_circle_check
import com.ismartcoding.plain.ui.resources.circle_dot as ui_drawable_circle_dot
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FeedEntriesSelectModeBottomActions(
    feedEntriesVM: FeedEntriesViewModel,
    tagsVM: TagsViewModel,
    tagsState: List<Tag>,
) {
    var showSelectTagsDialog by remember {
        mutableStateOf(false)
    }
    var removeFromTags by remember {
        mutableStateOf(false)
    }

    if (showSelectTagsDialog) {
        BatchSelectTagsDialog(tagsVM, tagsState, feedEntriesVM.getSelectedItems(), removeFromTags) {
            showSelectTagsDialog = false
            feedEntriesVM.exitSelectMode()
        }
    }

    PBottomAppBar {
        BottomActionButtons {
            IconTextSmallButtonLabel {
                showSelectTagsDialog = true
                removeFromTags = false
            }
            IconTextSmallButtonLabelOff {
                showSelectTagsDialog = true
                removeFromTags = true
            }
            val selectedIds = feedEntriesVM.selectedIds.toSet()
            val anyUnread = feedEntriesVM.itemsFlow.collectAsState().value.any { it.id in selectedIds && !it.read }
            // Short Gmail-style labels: the action bar scrolls horizontally and
            // 4-5 char labels overflow it.
            PIconTextSmallButton(
                icon = if (anyUnread) UiRes.drawable.ui_drawable_circle_check else UiRes.drawable.ui_drawable_circle_dot,
                text = stringResource(if (anyUnread) Res.string.read else Res.string.unread),
                onClick = {
                    feedEntriesVM.markRead(selectedIds, anyUnread)
                    feedEntriesVM.exitSelectMode()
                },
            )
            IconTextSmallButtonDelete {
                feedEntriesVM.delete(tagsVM, feedEntriesVM.selectedIds.toSet())
                feedEntriesVM.exitSelectMode()
            }
        }
    }
}
