package com.ismartcoding.plain.ui.models

import com.ismartcoding.plain.graphql.models.Video as MediaVideo
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.mutableStateMapOf
import com.ismartcoding.plain.enums.DataType

class VideosViewModel : BaseMediaViewModel<MediaVideo>() {
    override val dataType = DataType.VIDEO
    val scrollStateMap = mutableStateMapOf<Int, LazyGridState>()
}
