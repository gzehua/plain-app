package com.ismartcoding.plain.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ismartcoding.plain.IData
import com.ismartcoding.plain.models.Tag
import com.ismartcoding.plain.models.TagRelation
import com.ismartcoding.plain.ui.tags.TagsViewModel
import com.ismartcoding.plain.ui.state.launchSafe

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagSelector(
    data: IData,
    tagsVM: TagsViewModel,
    tagsMap: Map<String, List<TagRelation>>,
    tagsState: List<Tag>,
    onChangedAsync: suspend () -> Unit
) {
    val tagIds = remember {
        mutableStateListOf<String>()
    }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        tagIds.addAll(tagsMap[data.id]?.map { it.tagId } ?: emptyList())
    }
    TagNameDialog(tagsVM, onChangedAsync)
    FlowRow(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
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
                        onChangedAsync()
                    }
                },
                text = tag.name
            )
        }
        NewTagButton(click = {
            tagsVM.showAddDialog()
        })
    }
}
