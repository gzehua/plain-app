package com.ismartcoding.plain

import com.ismartcoding.plain.ui.app.MainViewModel

import com.ismartcoding.plain.ui.app.navControllerState
import com.ismartcoding.plain.app.AppIntents
import com.ismartcoding.plain.app.IntentExtras

import android.content.Intent
import androidx.navigation.NavDestination.Companion.hasRoute
import com.ismartcoding.plain.platform.system.AppFeatureType
import com.ismartcoding.plain.ui.i18n.LocaleHelper
import com.ismartcoding.plain.i18n.Res
import com.ismartcoding.plain.i18n.not_supported_error
import com.ismartcoding.plain.lib.extensions.isAudioFast
import com.ismartcoding.plain.ui.components.DialogHelper
import com.ismartcoding.plain.ui.navigation.Routing
import com.ismartcoding.plain.ui.navigation.navigatePdf
import com.ismartcoding.plain.ui.navigation.navigateShareImage
import com.ismartcoding.plain.ui.navigation.navigateTextFile

internal fun MainActivity.handleIntent(intent: Intent) {
    // Launcher shortcuts (long-press app icon): open the tool page directly.
    when (intent.action) {
        AppIntents.actionOpenShortcut(AppFeatureType.NOTES) -> openToolShortcut(Routing.Notes)
        AppIntents.actionOpenShortcut(AppFeatureType.DOCS) -> openToolShortcut(Routing.Docs)
        AppIntents.actionOpenShortcut(AppFeatureType.POMODORO_TIMER) -> openToolShortcut(Routing.PomodoroTimer)
        AppIntents.actionOpenShortcut(AppFeatureType.FEEDS) -> openToolShortcut(Routing.FeedEntries(""))
        AppIntents.actionOpenShortcut(AppFeatureType.IMAGES) -> openToolShortcut(Routing.Images)
        AppIntents.actionOpenShortcut(AppFeatureType.VIDEOS) -> openToolShortcut(Routing.Videos)
        AppIntents.actionOpenShortcut(AppFeatureType.AUDIO) -> openToolShortcut(Routing.Audio)
        AppIntents.actionOpenShortcut(AppFeatureType.FILES) -> openToolShortcut(Routing.Files())
    }

    intent.getStringExtra(IntentExtras.CHAT_TARGET_ID)?.let { targetId ->
        val nav = navControllerState.value
        val alreadyThere = nav?.currentBackStackEntry?.destination?.hasRoute(Routing.Chat::class) == true
        if (!alreadyThere) nav?.navigate(Routing.Chat(targetId))
    }

    intent.getStringExtra(IntentExtras.SHARE_IMAGE_PATH)?.let { path ->
        val nav = navControllerState.value
        nav?.navigateShareImage(path, intent.getStringExtra(IntentExtras.SHARE_IMAGE_NAME) ?: "")
    }

    if (intent.action == Intent.ACTION_VIEW) {
        val uri = intent.data ?: return
        val mimeType = contentResolver.getType(uri) ?: intent.type
        if (mimeType != null) {
            if (mimeType.startsWith("text/")) navControllerState.value?.navigateTextFile(uri.toString())
            else if (mimeType == "application/pdf") navControllerState.value?.navigatePdf(uri.toString())
            else DialogHelper.showErrorMessage(LocaleHelper.getString(Res.string.not_supported_error))
        } else {
            DialogHelper.showErrorMessage(LocaleHelper.getString(Res.string.not_supported_error))
        }
    } else if (intent.action == AppIntents.ACTION_PLAY_MEDIA) {
        val path = intent.getStringExtra(Constants.EXTRA_MEDIA_PATH) ?: return
        if (path.isAudioFast()) {
            navControllerState.value?.navigate(Routing.PlayMedia(path))
        } else {
            // Images/videos preview as an overlay above the app UI (no black route).
            MainViewModel.shortcutMediaPath.value = path
        }
    }
}

private fun MainActivity.openToolShortcut(route: Any) {
    val nav = navControllerState.value ?: return
    if (nav.currentBackStackEntry?.destination?.hasRoute(route::class) == true) return
    nav.navigate(route)
}
