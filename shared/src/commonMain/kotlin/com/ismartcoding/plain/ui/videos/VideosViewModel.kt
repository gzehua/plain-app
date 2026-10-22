package com.ismartcoding.plain.ui.videos

import com.ismartcoding.plain.ui.media.BaseMediaViewModel

import com.ismartcoding.plain.models.Video as MediaVideo
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.mutableStateMapOf
import com.ismartcoding.plain.platform.system.DataType

class VideosViewModel : BaseMediaViewModel<MediaVideo>() {
    override val dataType = DataType.VIDEO
    val scrollStateMap = mutableStateMapOf<Int, LazyGridState>()
}
