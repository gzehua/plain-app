package com.ismartcoding.plain.ui.base

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ismartcoding.plain.ui.resources.Res as UiRes
import com.ismartcoding.plain.ui.resources.chevron_right as ui_drawable_chevron_right
import com.ismartcoding.plain.ui.theme.listItemSubtitle
import com.ismartcoding.plain.ui.theme.listItemTitle
import com.ismartcoding.plain.ui.theme.listItemValue
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
    // ListItem supplies the outer padding and the gap after this slot.
    start: (@Composable RowScope.() -> Unit)? = null,
    titleTrailing: (@Composable () -> Unit)? = null,
    titleSuffix: (@Composable () -> Unit)? = null,
    separatedActions: Boolean = false,
    showMore: Boolean = false,
    action: (@Composable () -> Unit)? = null,
) {
    require(titleTrailing == null || titleSuffix == null) { "Choose either titleTrailing or titleSuffix" }
    val headline: @Composable () -> Unit = {
        PListItemTitle(title, contentColor, titleTrailing, titleSuffix, !value.isNullOrEmpty())
    }
    val supporting: (@Composable () -> Unit)? = if (subtitle.isNotEmpty()) {
        {
            Text(
                text = subtitle,
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.listItemSubtitle().contentColored(contentColor),
            )
        }
    } else null
    val valueLeading: @Composable () -> Unit = {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (separatedActions) {
                VerticalDivider(
                    modifier = Modifier.height(24.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(0.2f),
                )
                HorizontalSpace(16.dp)
            }
            action?.invoke()
        }
    }
    val valueText: @Composable () -> Unit = {
        value?.let {
            SelectionContainer {
                Text(
                    text = it,
                    style = MaterialTheme.typography.listItemValue().contentColored(contentColor),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                )
            }
        }
    }
    val valueTrailing: @Composable () -> Unit = {
        if (showMore) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!value.isNullOrEmpty() || action != null) HorizontalSpace(8.dp)
                Icon(
                    painter = painterResource(UiRes.drawable.ui_drawable_chevron_right),
                    modifier = Modifier.size(16.dp),
                    contentDescription = null,
                    tint = contentColor?.copy(alpha = 0.55f) ?: MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
    val trailing: (@Composable () -> Unit)? = if (action != null || showMore || separatedActions) {
        {
            Row(verticalAlignment = Alignment.CenterVertically) {
                valueLeading()
                valueTrailing()
            }
        }
    } else null
    ListItem(
        modifier = modifier.alpha(if (dimmed) 0.5f else 1f),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        headlineContent = {
            if (!value.isNullOrEmpty()) {
                PListItemValueLayout(
                    modifier = Modifier.fillMaxWidth(),
                    title = headline,
                    supporting = supporting,
                    value = valueText,
                    valueLeading = valueLeading,
                    valueTrailing = valueTrailing,
                )
            } else {
                headline()
            }
        },
        supportingContent = if (value.isNullOrEmpty()) supporting else null,
        leadingContent = when {
            start != null -> {
                { Row(verticalAlignment = Alignment.CenterVertically, content = start) }
            }
            icon != null -> {
                {
                    Image(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(icon),
                        colorFilter = ColorFilter.tint(contentColor ?: MaterialTheme.colorScheme.primary),
                        contentDescription = null,
                    )
                }
            }
            else -> null
        },
        trailingContent = if (value.isNullOrEmpty()) trailing else null,
    )
}

@Composable
private fun PListItemTitle(
    title: String,
    contentColor: Color?,
    titleTrailing: (@Composable () -> Unit)?,
    titleSuffix: (@Composable () -> Unit)?,
    completeTitle: Boolean,
) {
    when {
        titleTrailing != null -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.listItemTitle().contentColored(contentColor),
                    modifier = Modifier.weight(1f),
                )
                titleTrailing()
            }
        }
        titleSuffix != null -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.listItemTitle().contentColored(contentColor),
                    maxLines = if (completeTitle) Int.MAX_VALUE else 1,
                    overflow = if (completeTitle) TextOverflow.Clip else TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                HorizontalSpace(4.dp)
                titleSuffix()
            }
        }
        else -> Text(
            text = title,
            style = MaterialTheme.typography.listItemTitle().contentColored(contentColor),
        )
    }
}

private fun TextStyle.contentColored(contentColor: Color?): TextStyle =
    if (contentColor == null) this else copy(color = contentColor)
