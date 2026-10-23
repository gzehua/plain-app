package com.ismartcoding.plain.ui.base

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import org.jetbrains.compose.resources.DrawableResource
import androidx.compose.ui.semantics.Role
@Composable
fun PSheetActionRow(
    icon: DrawableResource? = null,
    title: String,
    start: (@Composable RowScope.() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    PListItem(
        modifier = modifier.clip(MaterialTheme.shapes.small).clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        icon = icon,
        start = start,
        title = title,
        action = trailing,
    )
}
