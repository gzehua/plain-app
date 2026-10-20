package com.ismartcoding.plain.ui.base

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ismartcoding.plain.ui.theme.listItemTitle

@Composable
internal fun RowScope.PListTextContent(
    title: String,
    subtitle: String,
    titleTrailing: (@Composable () -> Unit)?,
    titleSuffix: (@Composable () -> Unit)?,
    separatedActions: Boolean,
    showMore: Boolean,
    action: (@Composable () -> Unit)?,
    contentColor: Color?,
) {
    Column(Modifier.weight(1f)) {
        PListItemTitle(title, titleTrailing, titleSuffix, contentColor)
        if (subtitle.isNotEmpty()) PListItemSubtitle(subtitle, contentColor)
    }
    if (action != null || separatedActions || showMore) {
        HorizontalSpace(16.dp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (separatedActions) {
                VerticalDivider(
                    modifier = Modifier.height(24.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(0.2f),
                )
                HorizontalSpace(16.dp)
            }
            action?.invoke()
            if (showMore) PListItemChevron(contentColor, spaced = action != null)
        }
    }
}

@Composable
private fun PListItemTitle(
    title: String,
    titleTrailing: (@Composable () -> Unit)?,
    titleSuffix: (@Composable () -> Unit)?,
    contentColor: Color?,
) {
    val style = MaterialTheme.typography.listItemTitle().contentColored(contentColor)
    when {
        titleTrailing != null -> Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = title, style = style, modifier = Modifier.weight(1f))
            titleTrailing()
        }
        titleSuffix != null -> Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = style,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            HorizontalSpace(4.dp)
            titleSuffix()
        }
        else -> Text(text = title, style = style)
    }
}
