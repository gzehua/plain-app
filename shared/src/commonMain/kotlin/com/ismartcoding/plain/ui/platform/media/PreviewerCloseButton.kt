package com.ismartcoding.plain.ui.platform.media


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.ismartcoding.plain.i18n.Res
import com.ismartcoding.plain.i18n.close
import com.ismartcoding.plain.ui.components.ControlChipIconButton
import com.ismartcoding.plain.ui.media.viewer.previewer.MediaPreviewerState
import com.ismartcoding.plain.ui.resources.Res as UiRes
import com.ismartcoding.plain.ui.resources.close as ui_drawable_close
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource


@Composable
internal fun PreviewerCloseButton(state: MediaPreviewerState) {
    if (state.videoState.enablePip) return
    val scope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .padding(start = 16.dp, top = 32.dp)
            .alpha(state.uiAlpha.value),
    ) {
        if (state.showActions) {
            ControlChipIconButton(
                icon = UiRes.drawable.ui_drawable_close,
                contentDescription = stringResource(Res.string.close),
            ) {
                scope.launch { state.closeTransform() }
            }
        }
    }
}
