package com.ismartcoding.plain.ui.base

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp

@Composable
internal fun PListItemValueLayout(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    supporting: (@Composable () -> Unit)?,
    value: @Composable () -> Unit,
    valueLeading: @Composable () -> Unit,
    valueTrailing: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier,
        content = {
            Box { title() }
            Box { value() }
            Column { supporting?.invoke() }
            Box { valueLeading() }
            Box { valueTrailing() }
        },
    ) { measurables, constraints ->
        val titleWidth = measurables[0].maxIntrinsicWidth(Constraints.Infinity)
        val valueWidth = measurables[1].maxIntrinsicWidth(Constraints.Infinity)
        val controlsIntrinsicWidth = measurables[3].maxIntrinsicWidth(Constraints.Infinity).toLong() +
            measurables[4].maxIntrinsicWidth(Constraints.Infinity)
        val width = if (constraints.hasBoundedWidth) constraints.maxWidth else {
            constraints.constrainWidth((titleWidth.toLong() + valueWidth + controlsIntrinsicWidth + 16.dp.roundToPx()).coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
        }
        val leadingPlaceable = measurables[3].measure(Constraints(maxWidth = width))
        val trailingPlaceable = measurables[4].measure(Constraints(maxWidth = (width - leadingPlaceable.width).coerceAtLeast(0)))
        val controlsWidth = leadingPlaceable.width + trailingPlaceable.width
        val gap = 16.dp.roundToPx().coerceAtMost(width)
        val available = width - gap
        val stacked = listItemStacksValue(available, titleWidth, controlsWidth)
        val allocatedValue = if (stacked) width else listItemValueWidth(available, titleWidth, valueWidth + controlsWidth)
        val allocatedTitle = if (stacked) width else available - allocatedValue
        val titlePlaceable = measurables[0].measure(Constraints(maxWidth = allocatedTitle))
        val valuePlaceable = measurables[1].measure(Constraints(maxWidth = (allocatedValue - controlsWidth).coerceAtLeast(0)))
        val supportingPlaceable = measurables[2].measure(Constraints(maxWidth = allocatedTitle))
        val titleHeight = titlePlaceable.height + supportingPlaceable.height
        val valueHeight = maxOf(valuePlaceable.height, leadingPlaceable.height, trailingPlaceable.height)
        val verticalGap = if (stacked) 8.dp.roundToPx() else 0
        val height = constraints.constrainHeight(
            if (stacked) titleHeight + verticalGap + valueHeight else maxOf(titleHeight, valueHeight),
        )
        layout(width, height) {
            val titleY = if (stacked) 0 else (height - titleHeight) / 2
            titlePlaceable.placeRelative(0, titleY)
            supportingPlaceable.placeRelative(0, titleY + titlePlaceable.height)
            val valueY = if (stacked) titleHeight + verticalGap else (height - valueHeight) / 2
            leadingPlaceable.placeRelative(width - allocatedValue, valueY + (valueHeight - leadingPlaceable.height) / 2)
            valuePlaceable.placeRelative(width - trailingPlaceable.width - valuePlaceable.width, valueY + (valueHeight - valuePlaceable.height) / 2)
            trailingPlaceable.placeRelative(width - trailingPlaceable.width, valueY + (valueHeight - trailingPlaceable.height) / 2)
        }
    }
}

internal fun listItemValueWidth(available: Int, titleWidth: Int, valueWidth: Int): Int =
    minOf(valueWidth, available - titleWidth).coerceIn(0, available)

internal fun listItemStacksValue(available: Int, titleWidth: Int, controlsWidth: Int): Boolean =
    titleWidth.toLong() + controlsWidth >= available
