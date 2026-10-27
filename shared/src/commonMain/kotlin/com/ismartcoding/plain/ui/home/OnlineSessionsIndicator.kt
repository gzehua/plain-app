package com.ismartcoding.plain.ui.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ismartcoding.plain.i18n.Res
import com.ismartcoding.plain.i18n.clients_online
import com.ismartcoding.plain.ui.base.StatusIndicator
import com.ismartcoding.plain.ui.theme.greenDot
import com.ismartcoding.plain.ui.theme.greenText
import org.jetbrains.compose.resources.stringResource

@Composable
fun OnlineSessionsIndicator(count: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    StatusIndicator(
        text = stringResource(Res.string.clients_online, count),
        onClick = onClick,
        modifier = modifier,
        pillColor = Color.Transparent,
        dotColor = MaterialTheme.colorScheme.greenDot,
        textColor = MaterialTheme.colorScheme.greenText,
        animationEnabled = false,
        contentPaddingHorizontal = 0.dp,
        contentPaddingVertical = 8.dp,
        textStartPadding = 4.dp,
    )
}
