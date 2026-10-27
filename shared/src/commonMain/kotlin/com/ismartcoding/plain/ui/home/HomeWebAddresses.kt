package com.ismartcoding.plain.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ismartcoding.plain.i18n.Res
import com.ismartcoding.plain.i18n.home_try_other_addresses
import com.ismartcoding.plain.i18n.change_port
import com.ismartcoding.plain.i18n.mdns_hostname
import com.ismartcoding.plain.i18n.show_qr_code
import com.ismartcoding.plain.platform.setClipboardText
import com.ismartcoding.plain.ui.components.DialogHelper
import com.ismartcoding.plain.ui.components.PIconButton
import com.ismartcoding.plain.ui.web.components.WebAddressBar
import com.ismartcoding.plain.ui.web.components.WebAddressPager
import org.jetbrains.compose.resources.stringResource
import com.ismartcoding.plain.ui.resources.Res as UiRes
import com.ismartcoding.plain.ui.resources.pen
import com.ismartcoding.plain.ui.resources.qr_code
import com.ismartcoding.plain.ui.resources.chevron_right
import com.ismartcoding.plain.ui.resources.chevron_down

@Composable
fun HomeWebAddresses(lanIpv4Addresses: List<String>) {
    WebAddressPager(
        lanIpv4Addresses = lanIpv4Addresses,
        showProtocolSelector = true,
        addressBar = { isHttps, addresses ->
            WebAddressBar(
                isHttps = isHttps,
                lanIpv4Addresses = addresses,
                modifier = Modifier.fillMaxWidth(),
                moreAddressesText = stringResource(Res.string.home_try_other_addresses),
                backupAddressesBelowToggle = true,
                backupToggleModifier = Modifier,
                expandIcon = UiRes.drawable.chevron_right,
                collapseIcon = UiRes.drawable.chevron_down,
                addressRow = { url, isHostname, onEdit, onQr ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        SelectionContainer(Modifier.weight(1f)) {
                            Text(
                                text = url,
                                modifier = Modifier.fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .clickable {
                                        setClipboardText("url", url)
                                        DialogHelper.showTextCopiedMessage(url)
                                    }
                                    .padding(vertical = 8.dp),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                softWrap = false,
                            )
                        }
                        PIconButton(
                            icon = UiRes.drawable.pen,
                            modifier = Modifier.size(32.dp),
                            iconSize = 16.dp,
                            contentDescription = stringResource(if (isHostname) Res.string.mdns_hostname else Res.string.change_port),
                            click = onEdit,
                        )
                        PIconButton(
                            icon = UiRes.drawable.qr_code,
                            modifier = Modifier.size(32.dp),
                            iconSize = 16.dp,
                            contentDescription = stringResource(Res.string.show_qr_code),
                            click = onQr,
                        )
                    }
                },
            )
        },
    )
}
