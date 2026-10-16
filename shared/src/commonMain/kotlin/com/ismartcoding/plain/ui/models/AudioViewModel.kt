package com.ismartcoding.plain.ui.models

import com.ismartcoding.plain.graphql.models.Audio as AudioTrack
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateMapOf
import com.ismartcoding.plain.enums.DataType

class AudioViewModel : BaseMediaViewModel<AudioTrack>() {
    override val dataType = DataType.AUDIO
    override val showFoldersAsList = true
    val scrollStateMap = mutableStateMapOf<Int, LazyListState>()

}
