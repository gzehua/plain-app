package com.ismartcoding.plain.ui.base

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.ismartcoding.plain.ui.resources.Res as UiRes
import com.ismartcoding.plain.ui.resources.chevron_right as ui_drawable_chevron_right
import com.ismartcoding.plain.ui.theme.listItemSubtitle
import org.jetbrains.compose.resources.painterResource

internal fun TextStyle.contentColored(contentColor: Color?): TextStyle =
    if (contentColor == null) this else copy(color = contentColor)

@Composable
internal fun PListItemSubtitle(subtitle: String, contentColor: Color?, modifier: Modifier = Modifier) {
    Text(
        text = subtitle,
        modifier = modifier,
        style = MaterialTheme.typography.listItemSubtitle().contentColored(contentColor),
    )
}

@Composable
internal fun PListItemChevron(contentColor: Color?, spaced: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (spaced) HorizontalSpace(8.dp)
        Icon(
            painter = painterResource(UiRes.drawable.ui_drawable_chevron_right),
            modifier = Modifier.size(16.dp),
            contentDescription = null,
            tint = contentColor?.copy(alpha = 0.55f) ?: MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
