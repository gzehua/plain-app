package com.ismartcoding.plain.ui.home

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ismartcoding.plain.Prefs
import com.ismartcoding.plain.i18n.Res
import com.ismartcoding.plain.i18n.device_name
import com.ismartcoding.plain.platform.system.getDeviceName
import com.ismartcoding.plain.ui.base.PTopAppBar
import com.ismartcoding.plain.ui.components.ActionButtonScan
import com.ismartcoding.plain.ui.components.ActionButtonSettings
import com.ismartcoding.plain.ui.components.DeviceRenameDialog
import com.ismartcoding.plain.ui.components.PIconButton
import com.ismartcoding.plain.ui.navigation.Routing
import org.jetbrains.compose.resources.stringResource
import com.ismartcoding.plain.ui.resources.Res as UiRes
import com.ismartcoding.plain.ui.resources.pen as ui_drawable_pen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarHome(navController: NavHostController) {
    var showRenameDialog by remember { mutableStateOf(false) }
    val deviceName by Prefs.deviceName.collectAsState()

    if (showRenameDialog) {
        DeviceRenameDialog(
            name = deviceName,
            onDismiss = { showRenameDialog = false },
            onDone = { },
        )
    }

    PTopAppBar(
        title = deviceName.ifEmpty { getDeviceName() },
        titleTrailing = {
            PIconButton(
                icon = UiRes.drawable.ui_drawable_pen,
                modifier = Modifier.size(48.dp),
                iconSize = 16.dp,
                contentDescription = stringResource(Res.string.device_name),
                click = { showRenameDialog = true },
            )
        },
        actions = {
            ActionButtonScan { navController.navigate(Routing.Scan) }
            ActionButtonSettings { navController.navigate(Routing.Settings) }
        },
    )
}
