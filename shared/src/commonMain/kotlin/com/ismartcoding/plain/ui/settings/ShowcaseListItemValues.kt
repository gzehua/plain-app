package com.ismartcoding.plain.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ismartcoding.plain.ui.base.PCard
import com.ismartcoding.plain.ui.base.PListItem
import com.ismartcoding.plain.ui.base.PSectionTitle
import com.ismartcoding.plain.ui.base.VerticalSpace
import com.ismartcoding.plain.ui.resources.Res as UiRes
import com.ismartcoding.plain.ui.resources.settings as ui_drawable_settings
import org.jetbrains.compose.resources.painterResource

@Composable
fun ShowcaseListItemValues() {
    PSectionTitle("PListItem title + value")
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        PCard {
            PListItem(title = "Setting title", value = "OK")
            PListItem(title = "ID", value = "Selected value")
            PListItem(title = "Longer setting title", value = "A value that exceeds the remaining width and wraps onto multiple lines")
            PListItem(title = "ID", value = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ")
            PListItem(title = "A title that is longer than the entire list item width and needs to wrap", value = "Complete value")
            PListItem(title = "多语言标题需要完整显示", value = "这是需要在剩余宽度内自动换行的详细内容")
        }
        VerticalSpace(8.dp)
        PCard {
            PListItem(
                title = "Notifications",
                subtitle = "Alerts and banners",
                value = "Enabled",
                showMore = true,
                start = { Icon(painterResource(UiRes.drawable.ui_drawable_settings), null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary) },
            )
        }
        VerticalSpace(8.dp)
        PCard {
            PListItem(
                title = "Notifications",
                subtitle = "Alerts and banners",
                showMore = true,
                start = { Icon(painterResource(UiRes.drawable.ui_drawable_settings), null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary) },
            )
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
