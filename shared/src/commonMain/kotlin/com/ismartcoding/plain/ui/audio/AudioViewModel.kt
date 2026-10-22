package com.ismartcoding.plain.ui.audio

import com.ismartcoding.plain.ui.media.BaseMediaViewModel

import com.ismartcoding.plain.models.Audio as AudioTrack
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.mutableStateMapOf
import com.ismartcoding.plain.platform.system.DataType

class AudioViewModel : BaseMediaViewModel<AudioTrack>() {
    override val dataType = DataType.AUDIO
    override val showFoldersAsList = true
    val scrollStateMap = mutableStateMapOf<Int, LazyListState>()

}
