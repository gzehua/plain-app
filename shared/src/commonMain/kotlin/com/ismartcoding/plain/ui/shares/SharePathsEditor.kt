package com.ismartcoding.plain.ui.shares

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ismartcoding.plain.i18n.Res
import com.ismartcoding.plain.i18n.add_items
import com.ismartcoding.plain.i18n.delete
import com.ismartcoding.plain.i18n.shared_items
import com.ismartcoding.plain.lib.extensions.getFilenameFromPath
import com.ismartcoding.plain.ui.base.POutlinedButton
import com.ismartcoding.plain.ui.base.VerticalSpace
import com.ismartcoding.plain.ui.components.PIconButton
import com.ismartcoding.plain.ui.resources.Res as UiRes
import com.ismartcoding.plain.ui.resources.file_text as ui_drawable_file_text
import com.ismartcoding.plain.ui.resources.folder as ui_drawable_folder
import com.ismartcoding.plain.ui.resources.folder_plus as ui_drawable_folder_plus
import com.ismartcoding.plain.ui.resources.x as ui_drawable_x
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SharePathsEditor(paths: List<String>, dirFlags: Map<String, Boolean>, onRemove: (String) -> Unit, onAdd: () -> Unit) {
    Text(stringResource(Res.string.shared_items), style = MaterialTheme.typography.titleSmall)
    VerticalSpace(8.dp)
    paths.forEach { path ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(if (dirFlags[path] == true) UiRes.drawable.ui_drawable_folder else UiRes.drawable.ui_drawable_file_text),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 16.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = path.getFilenameFromPath(),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = path,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            PIconButton(
                icon = UiRes.drawable.ui_drawable_x,
                contentDescription = stringResource(Res.string.delete),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                enabled = paths.size > 1,
            ) {
                onRemove(path)
            }
        }
    }
    VerticalSpace(8.dp)
    POutlinedButton(
        text = stringResource(Res.string.add_items),
        icon = painterResource(UiRes.drawable.ui_drawable_folder_plus),
        onClick = onAdd,
    )
}
