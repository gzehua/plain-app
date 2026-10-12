package com.ismartcoding.plain.ui.page.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ismartcoding.plain.ui.base.PCard
import com.ismartcoding.plain.ui.base.PListItem
import com.ismartcoding.plain.ui.base.Subtitle
import com.ismartcoding.plain.ui.base.VerticalSpace
import com.ismartcoding.plain.ui.theme.listItemTitle
import com.ismartcoding.plain.ui.theme.listItemValue

@Composable
fun ShowcaseListItemValues() {
    Subtitle("PListItem title + value")
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        val density = LocalDensity.current
        val measurer = rememberTextMeasurer()
        for ((label, ratio) in listOf("Fits" to 1.25f, "Exact fit" to 1f, "Overflow" to 0.75f)) {
            Subtitle(label)
            for ((title, value) in listOf("Setting title" to "OK", "ID" to "Selected value", "Title" to "Value")) {
                val titleWidth = measurer.measure(title, MaterialTheme.typography.listItemTitle(), softWrap = false).size.width
                val valueWidth = measurer.measure(value, MaterialTheme.typography.listItemValue(), softWrap = false).size.width
                val width = with(density) { ((titleWidth + valueWidth) * ratio).toDp() } + 48.dp
                PCard(modifier = Modifier.width(width)) {
                    PListItem(title = title, value = value)
                }
                VerticalSpace(8.dp)
            }
        }
        VerticalSpace(8.dp)
        PCard(modifier = Modifier.fillMaxWidth(0.75f)) {
            PListItem(title = "Longer setting title", value = "A value that exceeds the remaining width")
            PListItem(title = "ID", value = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ")
            PListItem(title = "A longer title", value = "A longer value")
            PListItem(title = "A title that is longer than the entire list item width", value = "Complete value")
            PListItem(title = "多语言标题需要完整显示", value = "这是需要在剩余宽度内自动换行的详细内容")
        }
        VerticalSpace(8.dp)
        PCard {
            PListItem(title = "Title and subtitle", subtitle = "Supporting text remains below the title", value = "A long value with several words", showMore = true)
            PListItem(title = "Empty value", value = "", showMore = true)
            PListItem(title = "No value", showMore = true)
            PListItem(title = "Switch action", separatedActions = true, action = { Switch(checked = true, onCheckedChange = null) })
        }
    }
    VerticalSpace(16.dp)
}
