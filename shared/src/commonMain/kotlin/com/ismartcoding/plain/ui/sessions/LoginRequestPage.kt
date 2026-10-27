package com.ismartcoding.plain.ui.sessions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ismartcoding.plain.features.sessions.RustWebLogin
import com.ismartcoding.plain.features.sessions.WebLoginRequestFacts
import com.ismartcoding.plain.i18n.*
import com.ismartcoding.plain.lib.extensions.capitalize
import com.ismartcoding.plain.ui.base.PCard
import com.ismartcoding.plain.ui.base.PFilledButton
import com.ismartcoding.plain.ui.base.PListItem
import com.ismartcoding.plain.ui.base.POutlinedButton
import com.ismartcoding.plain.ui.components.ButtonSize
import com.ismartcoding.plain.ui.components.ButtonType
import com.ismartcoding.plain.ui.navigation.PBackHandler
import com.ismartcoding.plain.ui.state.launchSafe
import com.ismartcoding.plain.ui.theme.PlainTheme
import com.ismartcoding.plain.ui.resources.Res as UiRes
import com.ismartcoding.plain.ui.resources.chrome as ui_drawable_chrome
import com.ismartcoding.plain.ui.resources.laptop as ui_drawable_laptop
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LoginRequestPage(
    loginRequest: WebLoginRequestFacts,
    onClose: () -> Unit,
) {
    PBackHandler(onBack = onClose)
    val request = loginRequest.request
    val isWeb = request.browserName != "PlainApp"
    val descRes = if (request.peer != null) Res.string.allow_remote_chat_access_desc else Res.string.allow_remote_access_desc
    val iconRes = if (isWeb) UiRes.drawable.ui_drawable_chrome else UiRes.drawable.ui_drawable_laptop
    var allowing by remember { mutableStateOf(false) }
    var denying by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding(),
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = PlainTheme.PAGE_HORIZONTAL_MARGIN, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(Res.string.allow_remote_access),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(descRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            item {
                PCard {
                    PListItem(
                        title = "${request.browserName.capitalize()} ${request.browserVersion}".trim(),
                        subtitle = "${request.osName.capitalize()} ${request.osVersion}".trim(),
                        icon = iconRes,
                    )
                    PListItem(
                        title = stringResource(Res.string.ip_address),
                        value = loginRequest.clientIp,
                    )
                    if (request.verificationCode.isNotEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = stringResource(Res.string.login_compare_code),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = request.verificationCode,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            POutlinedButton(
                modifier = Modifier.weight(1f),
                text = stringResource(Res.string.deny),
                buttonSize = ButtonSize.LARGE,
                type = ButtonType.DANGER,
                isLoading = denying,
                enabled = !allowing && !denying,
                onClick = {
                    denying = true
                    scope.launchSafe(onDone = { denying = false }) {
                        RustWebLogin.cancel(loginRequest.requestId)
                        onClose()
                    }
                },
            )
            PFilledButton(
                modifier = Modifier.weight(1f),
                text = stringResource(Res.string.allow),
                buttonSize = ButtonSize.LARGE,
                isLoading = allowing,
                enabled = !allowing && !denying,
                onClick = {
                    allowing = true
                    scope.launchSafe(onDone = { allowing = false }) {
                        RustWebLogin.complete(loginRequest.requestId)
                        onClose()
                    }
                },
            )
        }
    }
}
