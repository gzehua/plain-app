package com.ismartcoding.plain.ui.base

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun PListItem(
    modifier: Modifier = Modifier,
    dimmed: Boolean = false,
    contentColor: Color? = null,
    title: String,
    subtitle: String = "",
    value: String? = null,
    icon: DrawableResource? = null,
    start: (@Composable RowScope.() -> Unit)? = null,
    titleTrailing: (@Composable () -> Unit)? = null,
    titleSuffix: (@Composable () -> Unit)? = null,
    separatedActions: Boolean = false,
    showMore: Boolean = false,
    action: (@Composable () -> Unit)? = null,
) {
    val hasValue = !value.isNullOrEmpty()
    PListItemRow(modifier, dimmed, hasSubtitle = subtitle.isNotEmpty(), icon, start, contentColor) {
        if (hasValue) {
            PListKeyValueContent(title, value.orEmpty(), subtitle, showMore, contentColor)
        } else {
            PListTextContent(title, subtitle, titleTrailing, titleSuffix, separatedActions, showMore, action, contentColor)
        }
    }
}

@Composable
private fun PListItemRow(
    modifier: Modifier,
    dimmed: Boolean,
    hasSubtitle: Boolean,
    icon: DrawableResource?,
    start: (@Composable RowScope.() -> Unit)?,
    contentColor: Color?,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .alpha(if (dimmed) 0.5f else 1f)
            .semantics(mergeDescendants = true) {}
            .heightIn(min = if (hasSubtitle) 72.dp else 56.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (start != null) {
            Row(verticalAlignment = Alignment.CenterVertically, content = start)
            HorizontalSpace(16.dp)
        } else if (icon != null) {
            Image(
                modifier = Modifier.size(24.dp),
                painter = painterResource(icon),
                colorFilter = ColorFilter.tint(contentColor ?: MaterialTheme.colorScheme.primary),
                contentDescription = null,
            )
            HorizontalSpace(16.dp)
        }
        content()
    }
}
