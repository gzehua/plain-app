package com.ismartcoding.plain.ui.sessions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ismartcoding.plain.features.sessions.RustWebLogin
import com.ismartcoding.plain.features.sessions.WebLoginRequestFacts
import com.ismartcoding.plain.i18n.*
import com.ismartcoding.plain.lib.extensions.capitalize
import com.ismartcoding.plain.ui.base.PCard
import com.ismartcoding.plain.ui.base.PFilledButton
import com.ismartcoding.plain.ui.base.POutlinedButton
import com.ismartcoding.plain.ui.components.ButtonSize
import com.ismartcoding.plain.ui.components.ButtonType
import com.ismartcoding.plain.ui.navigation.PBackHandler
import com.ismartcoding.plain.ui.state.launchSafe
import com.ismartcoding.plain.ui.theme.PlainTheme
import com.ismartcoding.plain.ui.resources.Res as UiRes
import com.ismartcoding.plain.ui.resources.lock as ui_drawable_lock
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LoginRequestPage(
    loginRequest: WebLoginRequestFacts,
    onClose: () -> Unit,
) {
    PBackHandler(onBack = onClose)
    val request = loginRequest.request
    val descRes = if (request.peer != null) Res.string.allow_remote_chat_access_desc else Res.string.allow_remote_access_desc
    val device = listOf(
        request.browserName.capitalize().trim(),
        request.osName.capitalize().trim(),
    ).filter { it.isNotEmpty() }.joinToString(" · ")
    val requester = listOf(device, loginRequest.clientIp).filter { it.isNotEmpty() }.joinToString("   ")
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
            modifier = Modifier.weight(1f).widthIn(max = 480.dp).fillMaxWidth().align(Alignment.CenterHorizontally),
            contentPadding = PaddingValues(horizontal = PlainTheme.PAGE_HORIZONTAL_MARGIN, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier.size(56.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            painter = painterResource(UiRes.drawable.ui_drawable_lock),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Text(
                        text = stringResource(Res.string.allow_remote_access),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = stringResource(descRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            if (request.verificationCode.isNotEmpty()) {
                item {
                    PCard {
                        Column(
                            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer).padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = request.verificationCode,
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                softWrap = false,
                                autoSize = TextAutoSize.StepBased(
                                    minFontSize = MaterialTheme.typography.titleMedium.fontSize,
                                    maxFontSize = MaterialTheme.typography.headlineLarge.fontSize,
                                ),
                            )
                            Text(
                                text = stringResource(Res.string.login_compare_code),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
            item {
                Text(
                    text = requester,
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Row(
            modifier = Modifier.widthIn(max = 480.dp).fillMaxWidth().align(Alignment.CenterHorizontally).padding(16.dp),
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
