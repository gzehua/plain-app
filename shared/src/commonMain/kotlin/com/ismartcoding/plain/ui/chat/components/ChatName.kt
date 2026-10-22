package com.ismartcoding.plain.ui.chat.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ismartcoding.plain.features.chat.models.MessageStatusData
import com.ismartcoding.plain.features.chat.ChatStatus
import com.ismartcoding.plain.i18n.*
import com.ismartcoding.plain.platform.system.formatTime
import com.ismartcoding.plain.ui.base.HorizontalSpace
import com.ismartcoding.plain.ui.components.PIconButton
import com.ismartcoding.plain.ui.chat.VChat
import com.ismartcoding.plain.ui.resources.Res as UiRes
import com.ismartcoding.plain.ui.resources.rotate_ccw as ui_drawable_rotate_ccw
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChatName(
    m: VChat,
    senderName: String?,
    isPeerChat: Boolean = false,
    isLocal: Boolean = false,
    onRetry: (() -> Unit)? = null,
    onShowDeliveryDetails: ((MessageStatusData) -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = when (m.fromId) {
                "local" -> stringResource(Res.string.local_chat)
                "me" -> stringResource(Res.string.me)
                else -> senderName ?: stringResource(Res.string.unknown)
            },

            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(end = 4.dp),
        )

        Text(
            text = m.createdAt.formatTime(),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Normal, color = MaterialTheme.colorScheme.onSurfaceVariant),
        )

        // Show status indicator based on message status (skip for local chat)
        if (!isLocal) {
            when {
                m.status == ChatStatus.PENDING -> {
                    HorizontalSpace(4.dp)
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        strokeWidth = 1.5.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Message with delivery data.
                m.fromId == "me" && m.statusData != null && m.statusData.failedCount > 0 -> {
                    val statusData = m.statusData
                    val isAllFailed = statusData.allFailed
                    val isPeerFailureBadge = isPeerChat && isAllFailed
                    val badgeColor = if (isAllFailed) MaterialTheme.colorScheme.error
                                     else MaterialTheme.colorScheme.tertiary

                    HorizontalSpace(6.dp)
                    Surface(
                        color = badgeColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onShowDeliveryDetails?.invoke(statusData) },
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (isAllFailed) {
                                Icon(
                                    painter = painterResource(UiRes.drawable.ui_drawable_rotate_ccw),
                                    contentDescription = null,
                                    tint = badgeColor,
                                    modifier = Modifier.size(10.dp),
                                )
                                HorizontalSpace(3.dp)
                            }
                            Text(
                                text = if (isPeerFailureBadge) stringResource(Res.string.error) else statusData.deliveryLabel(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = badgeColor,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                            )
                        }
                    }
                }

                // Peer / no-leader failure — show legacy retry button
                m.fromId == "me" && m.status == ChatStatus.FAILED -> {
                    if (onRetry != null) {
                        HorizontalSpace(4.dp)
                        PIconButton(
                            icon = UiRes.drawable.ui_drawable_rotate_ccw,
                            contentDescription = stringResource(Res.string.try_again),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        ) {
                            onRetry()
                        }
                    }
                }
            }
        }
    }
}
