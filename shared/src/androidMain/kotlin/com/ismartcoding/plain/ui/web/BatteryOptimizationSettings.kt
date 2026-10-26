package com.ismartcoding.plain.ui.web

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ismartcoding.plain.events.WindowFocusChangedEvent
import com.ismartcoding.plain.i18n.Res
import com.ismartcoding.plain.i18n.battery_optimization_disabled
import com.ismartcoding.plain.i18n.battery_optimization_tips
import com.ismartcoding.plain.i18n.disable_battery_optimization
import com.ismartcoding.plain.lib.Channel
import com.ismartcoding.plain.platform.system.isIgnoringBatteryOptimizations
import com.ismartcoding.plain.platform.system.openBatteryOptimizationSettings
import com.ismartcoding.plain.ui.base.PCard
import com.ismartcoding.plain.ui.base.PListItem
import com.ismartcoding.plain.ui.base.Tips
import com.ismartcoding.plain.ui.base.VerticalSpace
import com.ismartcoding.plain.ui.theme.PlainTheme
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BatteryOptimizationSettings() {
    val context = LocalContext.current
    var ignoring by remember { mutableStateOf(isIgnoringBatteryOptimizations()) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        ignoring = isIgnoringBatteryOptimizations()
    }
    LaunchedEffect(Unit) {
        Channel.sharedFlow.collect { event ->
            if (event is WindowFocusChangedEvent && event.hasFocus) {
                ignoring = isIgnoringBatteryOptimizations()
            }
        }
    }
    VerticalSpace(dp = 16.dp)
    PCard(modifier = Modifier.padding(horizontal = PlainTheme.PAGE_HORIZONTAL_MARGIN)) {
        PListItem(
            modifier = Modifier.clickable {
                if (ignoring) openBatteryOptimizationSettings()
                else launcher.launch(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                })
            },
            title = stringResource(if (ignoring) Res.string.battery_optimization_disabled else Res.string.disable_battery_optimization),
            showMore = true,
        )
    }
    Tips(stringResource(Res.string.battery_optimization_tips))
}
