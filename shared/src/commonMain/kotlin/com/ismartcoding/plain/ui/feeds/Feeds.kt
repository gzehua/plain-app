package com.ismartcoding.plain.ui.feeds

import com.ismartcoding.plain.platform.files.getFileNameFromUri
import com.ismartcoding.plain.platform.files.readTextFile
import com.ismartcoding.plain.platform.files.writeBytesToUri
import com.ismartcoding.plain.ui.i18n.LocaleHelper

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.ismartcoding.plain.features.files.ExportFileType
import com.ismartcoding.plain.features.files.PickFileTag
import com.ismartcoding.plain.app.events.ExportFileResultEvent
import com.ismartcoding.plain.app.events.PickFileResultEvent
import com.ismartcoding.plain.features.feeds.FeedsClient
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.i18n.*
import com.ismartcoding.plain.lib.Channel
import com.ismartcoding.plain.ui.components.DialogHelper
import com.ismartcoding.plain.ui.feeds.FeedsViewModel

/**
 * Side-effect handler for the Feeds page: listens for OPML pick/export events
 * on the global [Channel.sharedFlow] and dispatches them through [FeedsClient].
 *
 * File I/O goes through the low-level URI expects ([readTextFile],
 * [writeBytesToUri], [getFileNameFromUri]); the event-handling structure is
 * identical across platforms. iOS does not emit pick/export events today, so
 * the collectors remain dormant but structurally unified.
 */
@Composable
fun FeedsPageEffects(feedsVM: FeedsViewModel) {
    LaunchedEffect(Channel.sharedFlow) {
        Channel.sharedFlow.collect { event ->
            when (event) {
                is PickFileResultEvent -> {
                    if (event.tag != PickFileTag.FEED) return@collect
                    val content = readTextFile(event.uris.first())
                    DialogHelper.showLoading()
                    withIO {
                        try {
                            FeedsClient.importAsync(content)
                            feedsVM.loadAsync(withCount = true)
                            DialogHelper.hideLoading()
                        } catch (ex: Exception) {
                            DialogHelper.hideLoading()
                            DialogHelper.showMessage(ex)
                        }
                    }
                }

                is ExportFileResultEvent -> {
                    if (event.type != ExportFileType.OPML) return@collect
                    val opml = withIO { FeedsClient.exportAsync() }
                    writeBytesToUri(event.uri, opml.encodeToByteArray())
                    val fileName = getFileNameFromUri(event.uri) ?: ""
                    DialogHelper.showConfirmDialog(
                        "",
                        LocaleHelper.getStringFAsync(Res.string.exported_to, fileName),
                    )
                }
            }
        }
    }
}
