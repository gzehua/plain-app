package com.ismartcoding.plain.ui.web.components

import com.ismartcoding.plain.HttpServerManager
import com.ismartcoding.plain.preferences.PrefKeys

import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.ismartcoding.plain.lib.UrlHelper
import com.ismartcoding.plain.i18n.Res
import com.ismartcoding.plain.i18n.collapse_backup_addresses
import com.ismartcoding.plain.i18n.collapsed
import com.ismartcoding.plain.i18n.expanded
import com.ismartcoding.plain.i18n.try_more_addresses
import com.ismartcoding.plain.Prefs
import com.ismartcoding.plain.ui.base.PTextButton
import com.ismartcoding.plain.ui.theme.cardBackgroundNormal
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import com.ismartcoding.plain.ui.resources.Res as UiRes
import com.ismartcoding.plain.ui.resources.chevron_down as ui_drawable_chevron_down
import com.ismartcoding.plain.ui.resources.chevron_up as ui_drawable_chevron_up

@Composable
fun WebAddressBar(
    isHttps: Boolean,
    lanIpv4Addresses: List<String>,
) {
    val port = if (isHttps) Prefs.httpsPort.collectAsState() else Prefs.httpPort.collectAsState()
    // Shared persisted state so the HTTP/HTTPS pager pages stay in sync.
    val expanded = Prefs.webAddressBarExpanded.collectAsState()
    var portDialogVisible by remember { mutableStateOf(false) }
    var qrCodeDialogVisible by remember { mutableStateOf(false) }
    var mdnsEditDialogVisible by remember { mutableStateOf(false) }
    val hostname by Prefs.mdnsHostname.collectAsState()
    var qrCodeUrl by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val scheme = if (isHttps) "https" else "http"
    val primaryAddress = lanIpv4Addresses.firstOrNull() ?: hostname
    val backupAddresses = (listOf(hostname) + lanIpv4Addresses.drop(1)).filter { it != primaryAddress }

    Column(
        Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.cardBackgroundNormal,
                shape = RoundedCornerShape(12.dp),
            )
            .padding(vertical = 8.dp),
    ) {
        val primaryUrl = UrlHelper.buildUrl(scheme, primaryAddress, port.value)
        AddressRow(
            url = primaryUrl,
            isHostnameRow = primaryAddress == hostname,
            onEditClick = {
                if (primaryAddress == hostname) mdnsEditDialogVisible = true
                else portDialogVisible = true
            },
            onQrClick = {
                qrCodeUrl = primaryUrl
                qrCodeDialogVisible = true
            },
        )
        AnimatedVisibility(
            visible = expanded.value && backupAddresses.isNotEmpty(),
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Column {
                backupAddresses.forEach { address ->
                    val url = UrlHelper.buildUrl(scheme, address, port.value)
                    AddressRow(
                        url = url,
                        isHostnameRow = address == hostname,
                        onEditClick = {
                            if (address == hostname) {
                                mdnsEditDialogVisible = true
                            } else {
                                portDialogVisible = true
                            }
                        },
                        onQrClick = {
                            qrCodeUrl = url
                            qrCodeDialogVisible = true
                        },
                    )
                }
            }
        }
        if (backupAddresses.isNotEmpty()) {
            val expansion = stringResource(if (expanded.value) Res.string.expanded else Res.string.collapsed)
            PTextButton(
                modifier = Modifier.padding(horizontal = 8.dp).semantics {
                    stateDescription = expansion
                },
                text = stringResource(if (expanded.value) Res.string.collapse_backup_addresses else Res.string.try_more_addresses),
                icon = painterResource(
                    if (expanded.value)
                        UiRes.drawable.ui_drawable_chevron_up
                    else
                        UiRes.drawable.ui_drawable_chevron_down
                ),
                onClick = {
                    scope.launch { Prefs.set(PrefKeys.WEB_ADDRESS_BAR_EXPANDED, !expanded.value)}
                },
            )
        }
    }

    if (mdnsEditDialogVisible) {
        MdnsAndPortEditDialog(
            isHttps = isHttps,
            currentHostname = hostname,
            currentPort = port.value,
            onDismiss = { mdnsEditDialogVisible = false },
            onSave = { newHostname, newPort ->
                val hostnameChanged = newHostname != hostname
                val portChanged = newPort != port.value
                mdnsEditDialogVisible = false
                scope.launch {
                    if (hostnameChanged) Prefs.setMdnsHostname(newHostname)
                    if (portChanged) {
                        if (isHttps) Prefs.set(PrefKeys.HTTPS_PORT, newPort)
                        else Prefs.set(PrefKeys.HTTP_PORT, newPort)
                    }
                    if (hostnameChanged || portChanged) HttpServerManager.restartHttpServer()
                }
            },
        )
    }

    if (portDialogVisible) {
        PortSelectionDialog(
            isHttps = isHttps,
            currentPort = port.value,
            onDismiss = { portDialogVisible = false },
            onSelect = {
                persistPort(scope, isHttps, it)
                portDialogVisible = false
                HttpServerManager.restartHttpServer()
            },
        )
    }

    if (qrCodeDialogVisible) {
        WebAddressBarQrDialog(
            url = qrCodeUrl,
            onClose = { qrCodeDialogVisible = false },
        )
    }
}

@Composable
private fun AddressRow(
    url: String,
    isHostnameRow: Boolean,
    onEditClick: () -> Unit,
    onQrClick: () -> Unit,
) {
    Row(
        modifier = Modifier.height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WebAddressBarRow(
            url = url,
            isHostnameRow = isHostnameRow,
            onEditClick = onEditClick,
            onQrClick = onQrClick,
        )
    }
}
