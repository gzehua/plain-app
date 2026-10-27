package com.ismartcoding.plain.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ismartcoding.plain.features.settings.ServiceState
import com.ismartcoding.plain.i18n.*
import com.ismartcoding.plain.i18n.Res
import com.ismartcoding.plain.HttpServerManager
import com.ismartcoding.plain.platform.system.Capability
import com.ismartcoding.plain.platform.system.getDeviceCapabilities
import com.ismartcoding.plain.ui.base.*
import com.ismartcoding.plain.ui.theme.PlainTheme
import com.ismartcoding.plain.ui.theme.tipsText
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.ismartcoding.plain.ui.resources.Res as UiRes
import com.ismartcoding.plain.ui.resources.chevron_down as ui_drawable_chevron_down
import com.ismartcoding.plain.ui.resources.chevron_up as ui_drawable_chevron_up
import com.ismartcoding.plain.ui.resources.dark_mode as ui_drawable_dark_mode

@Composable
fun PlainAppServiceSection(
    backgroundServiceEnabled: Boolean,
    foregroundServiceState: ServiceState,
    errorMessage: String,
    onRetry: (() -> Unit)?,
    onStayOnline: () -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val label = stringResource(Res.string.connection_keeps_dropping)
    val expansion = stringResource(if (expanded) Res.string.expanded else Res.string.collapsed)
    PCard(modifier = Modifier.padding(horizontal = PlainTheme.PAGE_HORIZONTAL_MARGIN)) {
        if (Capability.BACKGROUND_SERVICE in getDeviceCapabilities()) {
            PListItem(
                icon = UiRes.drawable.ui_drawable_dark_mode,
                title = {
                    Text(
                        text = stringResource(Res.string.keep_online_in_background),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        autoSize = TextAutoSize.StepBased(
                            minFontSize = MaterialTheme.typography.bodyMedium.fontSize,
                            maxFontSize = MaterialTheme.typography.titleMedium.fontSize,
                        ),
                    )
                },
            ) {
                Switch(
                    checked = backgroundServiceEnabled,
                    enabled = !foregroundServiceState.isProcessing(),
                    onCheckedChange = { HttpServerManager.setBackgroundServiceEnabled(it) },
                )
            }
            Text(
                text = stringResource(Res.string.keep_online_in_background_desc),
                style = MaterialTheme.typography.tipsText(),
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        if (errorMessage.isNotEmpty()) {
            PAlert(description = errorMessage, type = AlertType.ERROR) {
                if (onRetry != null) {
                    PTextButton(text = stringResource(Res.string.retry), onClick = onRetry)
                }
            }
        }
        PTextButton(
            modifier = Modifier.padding(horizontal = 8.dp).semantics {
                stateDescription = expansion
            },
            text = label,
            icon = painterResource(if (expanded) UiRes.drawable.ui_drawable_chevron_up else UiRes.drawable.ui_drawable_chevron_down),
            onClick = { expanded = !expanded },
        )
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 8.dp)) {
                Text(
                    text = stringResource(Res.string.connection_keeps_dropping_help),
                    style = MaterialTheme.typography.tipsText(),
                )
                VerticalSpace(16.dp)
                POutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(Res.string.stay_online),
                    onClick = onStayOnline,
                )
            }
        }
        VerticalSpace(8.dp)
    }
}
