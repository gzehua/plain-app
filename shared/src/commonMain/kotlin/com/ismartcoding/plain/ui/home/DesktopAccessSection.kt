package com.ismartcoding.plain.ui.home

import com.ismartcoding.plain.ui.theme.PlainTheme

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.ismartcoding.plain.i18n.Res
import com.ismartcoding.plain.i18n.access_settings
import com.ismartcoding.plain.i18n.desktop_access
import com.ismartcoding.plain.i18n.desktop_access_desc
import com.ismartcoding.plain.ui.resources.devices as ui_drawable_devices
import com.ismartcoding.plain.i18n.open_web_address
import com.ismartcoding.plain.ui.base.PCard
import com.ismartcoding.plain.ui.base.PDivider
import com.ismartcoding.plain.ui.base.PListItem
import androidx.compose.material3.Switch
import com.ismartcoding.plain.Prefs
import com.ismartcoding.plain.ui.base.VerticalSpace
import com.ismartcoding.plain.ui.web.components.WebAddressPager
import com.ismartcoding.plain.ui.media.extensions.collectAsStateValue
import com.ismartcoding.plain.ui.state.launchSafe
import com.ismartcoding.plain.ui.settings.DesktopAccessSettingsViewModel
import com.ismartcoding.plain.ui.navigation.Routing
import com.ismartcoding.plain.ui.theme.tipsText
import com.ismartcoding.plain.features.sessions.onlineClientIds
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.stringResource
import com.ismartcoding.plain.ui.resources.Res as UiRes
import com.ismartcoding.plain.platform.system.setDesktopAccessEnabled

@Composable
fun DesktopAccessSection(navController: NavHostController) {
    val desktopAccessEnabled = Prefs.desktopAccess.collectAsStateValue()
    val scope = rememberCoroutineScope()
    val onlineCount by onlineClientIds.map { it.size }.collectAsState(0)

    PCard(modifier = Modifier.padding(horizontal = PlainTheme.PAGE_HORIZONTAL_MARGIN)) {
        PListItem(icon = UiRes.drawable.ui_drawable_devices, title = stringResource(Res.string.desktop_access)) {
            Switch(checked = desktopAccessEnabled, onCheckedChange = { enable ->
                scope.launchSafe {
                    setDesktopAccessEnabled(enable)
                }
            })
        }
        if (desktopAccessEnabled && onlineCount > 0) {
            OnlineSessionsIndicator(
                count = onlineCount,
                onClick = { navController.navigate(Routing.Connections) })
        } else {
            Text(
                text = stringResource(if (desktopAccessEnabled) Res.string.open_web_address else Res.string.desktop_access_desc),
                style = MaterialTheme.typography.tipsText(),
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
        if (desktopAccessEnabled) {
            val webVM: DesktopAccessSettingsViewModel = viewModel { DesktopAccessSettingsViewModel() }
            val lanIpv4Addresses by webVM.lanIpv4Addresses.collectAsState()
            LaunchedEffect(webVM) { webVM.observeNetworkAddresses() }
            lanIpv4Addresses?.let { WebAddressPager(lanIpv4Addresses = it) }
        }
        VerticalSpace(16.dp)
        PDivider(modifier = Modifier.padding(start = 16.dp))
        PListItem(modifier = Modifier.clickable {
            navController.navigate(Routing.DesktopAccessSettings)
        }, title = stringResource(Res.string.access_settings), showMore = true)
    }
}
