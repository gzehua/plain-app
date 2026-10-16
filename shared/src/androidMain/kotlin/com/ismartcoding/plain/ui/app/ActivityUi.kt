package com.ismartcoding.plain.ui.app

import com.ismartcoding.plain.MainActivity
import com.ismartcoding.plain.handleIntent
import com.ismartcoding.plain.ui.media.extensions.collectAsStateValue
import com.ismartcoding.plain.preferences.*
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.database.CursorWindow
import android.net.wifi.WifiManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import com.ismartcoding.plain.app.HttpServerManager
import com.ismartcoding.plain.platform.isTPlus
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.features.files.ExportFileType
import com.ismartcoding.plain.features.settings.Language
import com.ismartcoding.plain.features.files.PickFileTag
import com.ismartcoding.plain.features.files.PickFileType
import com.ismartcoding.plain.app.events.ExportFileResultEvent
import com.ismartcoding.plain.app.events.IgnoreBatteryOptimizationResultEvent
import com.ismartcoding.plain.app.events.PickFileResultEvent
import com.ismartcoding.plain.app.events.WindowFocusChangedEvent
import com.ismartcoding.plain.features.audio.AudioPlayer
import com.ismartcoding.plain.platform.permissions.Permission
import com.ismartcoding.plain.ui.permissions.Permissions
import com.ismartcoding.plain.features.bluetooth.client.BluetoothPermission
import com.ismartcoding.plain.platform.permissions.isGranted
import com.ismartcoding.plain.platform.system.publishLauncherShortcuts
import com.ismartcoding.plain.receivers.NetworkStateReceiver
import com.ismartcoding.plain.receivers.PlugInControlReceiver
import com.ismartcoding.plain.services.PlainAccessibilityService
import com.ismartcoding.plain.services.ScreenMirrorService
import com.ismartcoding.plain.platform.system.FilePickHelper
import com.ismartcoding.plain.ui.audio.AudioQueueViewModel
import com.ismartcoding.plain.ui.chat.ChannelViewModel
import com.ismartcoding.plain.ui.app.MainViewModel
import com.ismartcoding.plain.ui.chat.PeerViewModel
import com.ismartcoding.plain.ui.pomodoro.PomodoroViewModel
import com.ismartcoding.plain.ui.theme.DarkTheme
import com.ismartcoding.plain.lib.sendEvent
import com.ismartcoding.plain.ui.settings.CrashReportDialog
import com.ismartcoding.plain.ui.navigation.Routing
import com.ismartcoding.plain.ui.navigation.Main
import com.ismartcoding.plain.ui.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

internal class ActivityUiState {
    val navControllerState = mutableStateOf<NavHostController?>(null)
    var pendingCrashReport by mutableStateOf<String?>(null)
}

internal val MainActivity.navControllerState get() = uiState.navControllerState
internal var MainActivity.pendingCrashReport: String?
    get() = uiState.pendingCrashReport
    set(value) { uiState.pendingCrashReport = value }

internal fun MainActivity.installComposeUi() {
    setContent {
            AppTheme(
                useDarkTheme = DarkTheme.isDarkTheme(UserPrefs.darkTheme.collectAsStateValue()),
                amoledDarkTheme = UserPrefs.amoledDarkTheme.collectAsStateValue(),
            ) {
                Main(
                    navControllerState, onLaunched = { handleIntent(intent) },
                    mainVM, audioQueueVM, pomodoroVM,
                    peerVM = peerVM,
                    channelVM = channelVM
                )
                pendingCrashReport?.let { report ->
                    CrashReportDialog(crashReport = report, navController = navControllerState.value, onDismiss = { pendingCrashReport = null })
                }
            }
        }
}
