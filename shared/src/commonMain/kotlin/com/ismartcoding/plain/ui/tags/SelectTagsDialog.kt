package com.ismartcoding.plain.ui.tags

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ismartcoding.plain.IData
import com.ismartcoding.plain.ui.components.ButtonSize
import com.ismartcoding.plain.models.Tag
import com.ismartcoding.plain.models.TagRelation
import com.ismartcoding.plain.i18n.*
import com.ismartcoding.plain.ui.components.PSelectionChip
import com.ismartcoding.plain.ui.base.PTextButton
import com.ismartcoding.plain.ui.components.NewTagButton
import com.ismartcoding.plain.ui.components.TagNameDialog
import com.ismartcoding.plain.ui.state.launchSafe
import com.ismartcoding.plain.ui.theme.dialogSheetBackground
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SelectTagsDialog(
    tagsVM: TagsViewModel,
    tagsState: List<Tag>,
    tagsMap: Map<String, List<TagRelation>>,
    data: IData,
    onDismiss: () -> Unit,
) {
    val tagIds = remember {
        mutableStateListOf<String>()
    }
    val scope = rememberCoroutineScope()

    TagNameDialog(tagsVM)

    LaunchedEffect(Unit) {
        tagIds.addAll(tagsMap[data.id]?.map { it.tagId } ?: emptyList())
    }

    AlertDialog(
        containerColor = MaterialTheme.colorScheme.dialogSheetBackground,
        onDismissRequest = {
            onDismiss()
        },
        title = {
            Text(text = stringResource(Res.string.select_tags),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            FlowRow(
                modifier =
                Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                tagsState.forEach { tag ->
                    PSelectionChip(
                        selected = tagIds.contains(tag.id),
                        onClick = {
                            scope.launchSafe {
                                tagsVM.toggleTagAsync(data, tag.id)
                                if (tagIds.contains(tag.id)) {
                                    tagIds.remove(tag.id)
                                } else {
                                    tagIds.add(tag.id)
                                }
                            }
                        },
                        text = tag.name
                    )
                }
                NewTagButton(click = {
                    tagsVM.showAddDialog()
                })
            }
        }, confirmButton = {
            PTextButton(
                text = stringResource(Res.string.close),
                buttonSize = ButtonSize.MEDIUM,
                onClick = {
                    onDismiss()
                },
            )
        }
    )
}
