package com.ismartcoding.plain.features.mediaactions

import com.ismartcoding.plain.TempData
import com.ismartcoding.plain.enums.DataType
import com.ismartcoding.plain.lib.JsonHelper
import androidx.compose.runtime.snapshots.Snapshot

internal object MediaActionProjection {
    fun apply(payload: String) {
        val result = JsonHelper.jsonDecode<MediaActionFinished>(payload)
        val type = DataType.entries.single { it.value == result.type }
        val action = MediaAction.valueOf(result.action)
        Snapshot.withMutableSnapshot {
            result.ids.forEach { id ->
                TempData.mediaDurationMap.remove("${type.name.lowercase()}:$id")
                if (type == DataType.VIDEO && (action == MediaAction.TRASH || action == MediaAction.DELETE)) TempData.videoPlayProgressMap.remove(id)
            }
        }
    }
}
