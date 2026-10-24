package com.ismartcoding.plain.ui.base

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ismartcoding.plain.ui.theme.Spacing
import com.ismartcoding.plain.ui.theme.orange
import org.jetbrains.compose.resources.painterResource
import com.ismartcoding.plain.ui.resources.Res as UiRes
import com.ismartcoding.plain.ui.resources.circle_alert as ui_drawable_circle_alert
import com.ismartcoding.plain.ui.resources.circle_info as ui_drawable_circle_info
import com.ismartcoding.plain.ui.resources.octagon_alert as ui_drawable_octagon_alert

enum class AlertType {
    WARNING,
    ERROR,
    INFO,
}

/**
 * Material You tonal alert card (no accent bar): semantic container +
 * auto semantic icon + description, with optional trailing icons, an
 * action row of text buttons (PTextButton — filled buttons are too loud
 * for banners) and a custom content slot. The whole card can be clickable
 * via [onClick].
 */
@Composable
fun PAlert(
    description: String,
    type: AlertType,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    val accentColor = when (type) {
        AlertType.WARNING -> MaterialTheme.colorScheme.orange
        AlertType.ERROR -> MaterialTheme.colorScheme.error
        AlertType.INFO -> MaterialTheme.colorScheme.primary
    }
    val containerColor = when (type) {
        AlertType.WARNING -> accentColor.copy(alpha = 0.15f)
        AlertType.ERROR -> accentColor.copy(alpha = 0.12f)
        AlertType.INFO -> accentColor.copy(alpha = 0.12f)
    }
    val iconRes = when (type) {
        AlertType.WARNING -> UiRes.drawable.ui_drawable_octagon_alert
        AlertType.ERROR -> UiRes.drawable.ui_drawable_circle_alert
        AlertType.INFO -> UiRes.drawable.ui_drawable_circle_info
    }
    val shape = MaterialTheme.shapes.medium

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg),
        shape = shape,
        color = containerColor,
    ) {
        Row(
            modifier = Modifier
                .then(if (onClick != null) Modifier.clip(shape).clickable(onClick = onClick) else Modifier),
        ) {
            val bottomPadding = if (actions != null && content == null) Spacing.sm else Spacing.lg
            Column(modifier = Modifier.padding(start = Spacing.lg, top = Spacing.lg, end = Spacing.lg, bottom = bottomPadding)) {
                // Icon aligns to the first text line, not vertically centered
                // in the row (multi-line descriptions would float the icon).
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(Spacing.xl),
                    )
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 24.sp,
                            lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None),
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = if (trailing != null) Modifier.weight(1f) else Modifier,
                    )
                    if (trailing != null) {
                        Row(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            trailing.invoke(this)
                        }
                    }
                }
                if (actions != null) {
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    // Without this the buttons reserve a 48.dp touch target and push the card bottom away.
                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            actions.invoke(this)
                        }
                    }
                }
                content?.invoke(this)
            }
        }
    }
    VerticalSpace(dp = Spacing.sm)
}
