@file:OptIn(ExperimentalMaterial3Api::class)

package com.ismartcoding.plain.ui.shares

import com.ismartcoding.plain.features.files.FilesClient

import com.ismartcoding.plain.features.files.extension

import com.ismartcoding.plain.models.File as FileEntry

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ismartcoding.plain.features.files.FileSortBy
import com.ismartcoding.plain.i18n.*
import com.ismartcoding.plain.lib.extensions.formatBytes
import com.ismartcoding.plain.lib.extensions.getFilenameFromPath
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.platform.files.getInternalStoragePath
import com.ismartcoding.plain.ui.base.BottomActionButtons
import com.ismartcoding.plain.ui.base.PBottomSheetTopAppBar
import com.ismartcoding.plain.ui.base.PFilledButton
import com.ismartcoding.plain.ui.base.PModalBottomSheet
import com.ismartcoding.plain.ui.components.DirBrowserRow
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.material3.ExperimentalMaterial3Api

@Composable
fun ShareItemsPickerSheet(
    initialSelected: Map<String, Boolean>,
    onDismiss: () -> Unit,
    onConfirm: (Map<String, Boolean>) -> Unit,
) {
    val rootPath = remember { getInternalStoragePath() }
    var currentPath by remember { mutableStateOf(rootPath) }
    var entries by remember { mutableStateOf<List<FileEntry>>(emptyList()) }
    val selected = remember { mutableStateMapOf<String, Boolean>().apply { putAll(initialSelected) } }

    LaunchedEffect(currentPath) {
        withIO {
            entries = FilesClient.list(currentPath, showHidden = false, sortBy = FileSortBy.NAME_ASC)
        }
    }

    fun toggle(entry: FileEntry) {
        if (selected.containsKey(entry.path)) selected.remove(entry.path) else selected[entry.path] = entry.isDir
    }

    PModalBottomSheet(onDismissRequest = onDismiss, modifier = Modifier.fillMaxWidth()) {
        PBottomSheetTopAppBar(
            title = stringResource(Res.string.add_items),
        )
        Text(
            text = currentPath,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 16.dp),
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
        ) {
            if (currentPath != rootPath) {
                item {
                    DirBrowserRow(
                        name = "..",
                        subtitle = stringResource(Res.string.back),
                        onClick = { currentPath = currentPath.substringBeforeLast('/', missingDelimiterValue = rootPath).ifEmpty { rootPath } },
                    )
                }
            }
            items(entries, key = { it.path }) { entry ->
                DirBrowserRow(
                    name = entry.name,
                    subtitle = if (entry.isDir) "" else entry.size.formatBytes(),
                    isDir = entry.isDir,
                    selected = selected.containsKey(entry.path),
                    showCheckbox = true,
                    onClick = {
                        if (entry.isDir) currentPath = entry.path else toggle(entry)
                    },
                    onCheckedChange = { toggle(entry) },
                )
            }
        }
        BottomActionButtons {
            PFilledButton(
                text = stringResource(Res.string.done) + " (${selected.size})",
                onClick = { onConfirm(selected.toMap()) },
                enabled = selected.isNotEmpty(),
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 8.dp),
            )
        }
    }
}
