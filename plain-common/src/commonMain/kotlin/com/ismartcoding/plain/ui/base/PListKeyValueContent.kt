package com.ismartcoding.plain.ui.base

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ismartcoding.plain.ui.theme.listItemTitle
import com.ismartcoding.plain.ui.theme.listItemValue

@Composable
internal fun RowScope.PListKeyValueContent(
    title: String,
    value: String,
    subtitle: String,
    showMore: Boolean,
    contentColor: Color?,
) {
    PListItemValueLayout(
        modifier = Modifier.weight(1f),
        title = { Text(text = title, style = MaterialTheme.typography.listItemTitle().contentColored(contentColor)) },
        supporting = if (subtitle.isNotEmpty()) ({ PListItemSubtitle(subtitle, contentColor, Modifier.padding(top = 4.dp)) }) else null,
        value = {
            SelectionContainer {
                Text(
                    text = value,
                    style = MaterialTheme.typography.listItemValue().contentColored(contentColor),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                )
            }
        },
        end = if (showMore) ({ PListItemChevron(contentColor, spaced = true) }) else null,
    )
}
