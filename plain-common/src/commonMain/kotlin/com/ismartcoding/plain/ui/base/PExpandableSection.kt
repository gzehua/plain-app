package com.ismartcoding.plain.ui.base

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import org.jetbrains.compose.resources.painterResource
import com.ismartcoding.plain.ui.resources.Res as UiRes
import com.ismartcoding.plain.ui.resources.chevron_down as ui_drawable_chevron_down
import com.ismartcoding.plain.ui.resources.chevron_right as ui_drawable_chevron_right

@Composable
fun PExpandableSection(
    text: String,
    expanded: Boolean,
    stateDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    toggleModifier: Modifier = Modifier,
    fadeContent: Boolean = false,
    content: @Composable () -> Unit,
) {
    Column(modifier) {
        PTextButton(
            text = text,
            icon = painterResource(if (expanded) UiRes.drawable.ui_drawable_chevron_down else UiRes.drawable.ui_drawable_chevron_right),
            onClick = onClick,
            modifier = toggleModifier.semantics { this.stateDescription = stateDescription },
        )
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + if (fadeContent) fadeIn() else EnterTransition.None,
            exit = shrinkVertically() + if (fadeContent) fadeOut() else ExitTransition.None,
        ) {
            content()
        }
    }
}
