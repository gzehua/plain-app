package com.ismartcoding.plain.ui.notes

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.ismartcoding.plain.models.Tag
import com.ismartcoding.plain.ui.base.BottomActionButtons
import com.ismartcoding.plain.ui.components.IconTextSmallButtonDelete
import com.ismartcoding.plain.ui.components.IconTextSmallButtonLabel
import com.ismartcoding.plain.ui.components.IconTextSmallButtonLabelOff
import com.ismartcoding.plain.ui.components.IconTextSmallButtonRestore
import com.ismartcoding.plain.ui.components.IconTrashButton
import com.ismartcoding.plain.ui.base.PBottomAppBar
import com.ismartcoding.plain.ui.notes.NotesViewModel
import com.ismartcoding.plain.ui.tags.TagsViewModel
import com.ismartcoding.plain.ui.state.exitSelectMode
import com.ismartcoding.plain.ui.state.getSelectedItems
import com.ismartcoding.plain.ui.tags.BatchSelectTagsDialog

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NotesSelectModeBottomActions(
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
        BatchSelectTagsDialog(tagsVM, tagsState, NotesViewModel.getSelectedItems(), removeFromTags) {
            showSelectTagsDialog = false
            NotesViewModel.exitSelectMode()
        }
    }

    PBottomAppBar {
        BottomActionButtons {
            if (NotesViewModel.trash.value) {
                IconTextSmallButtonRestore {
                    NotesViewModel.restore(tagsVM, NotesViewModel.selectedIds.toSet())
                    NotesViewModel.exitSelectMode()
                }
                IconTextSmallButtonDelete {
                    NotesViewModel.delete(tagsVM, NotesViewModel.selectedIds.toSet())
                    NotesViewModel.exitSelectMode()
                }
            } else {
                IconTextSmallButtonLabel {
                    showSelectTagsDialog = true
                    removeFromTags = false
                }
                IconTextSmallButtonLabelOff {
                    showSelectTagsDialog = true
                    removeFromTags = true
                }
                IconTrashButton {
                    NotesViewModel.trash(tagsVM, NotesViewModel.selectedIds.toSet())
                    NotesViewModel.exitSelectMode()
                }
            }
        }
    }
}
