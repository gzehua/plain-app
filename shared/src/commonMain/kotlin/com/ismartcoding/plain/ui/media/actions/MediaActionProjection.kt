package com.ismartcoding.plain.ui.media.actions

import com.ismartcoding.plain.features.media.actions.MediaAction
import com.ismartcoding.plain.features.media.actions.MediaActionFinished

import com.ismartcoding.plain.ui.app.AppUiState

import com.ismartcoding.plain.app.TempData
import com.ismartcoding.plain.platform.system.DataType
import com.ismartcoding.plain.lib.JsonHelper
import androidx.compose.runtime.snapshots.Snapshot

internal object MediaActionProjection {
    fun apply(payload: String) {
        val result = JsonHelper.jsonDecode<MediaActionFinished>(payload)
        val type = DataType.entries.single { it.value == result.type }
        val action = MediaAction.valueOf(result.action)
        Snapshot.withMutableSnapshot {
            result.ids.forEach { id ->
                if (type == DataType.VIDEO && (action == MediaAction.TRASH || action == MediaAction.DELETE)) AppUiState.videoPlayProgressMap.remove(id)
            }
        }
    }
}
