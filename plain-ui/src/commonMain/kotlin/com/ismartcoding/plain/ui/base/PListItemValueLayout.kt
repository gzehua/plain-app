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
    end: (@Composable () -> Unit)?,
) {
    Layout(
        modifier = modifier,
        content = {
            Box { title() }
            Box { value() }
            Column { supporting?.invoke() }
            Box { end?.invoke() }
        },
    ) { measurables, constraints ->
        val titleWidth = measurables[0].maxIntrinsicWidth(Constraints.Infinity)
        val valueWidth = measurables[1].maxIntrinsicWidth(Constraints.Infinity)
        val endMaxWidth = measurables[3].maxIntrinsicWidth(Constraints.Infinity)
        val width = if (constraints.hasBoundedWidth) constraints.maxWidth else {
            constraints.constrainWidth((titleWidth.toLong() + valueWidth + endMaxWidth + 16.dp.roundToPx()).coerceAtMost(Int.MAX_VALUE.toLong()).toInt())
        }
        val endPlaceable = measurables[3].measure(Constraints(maxWidth = width))
        val endWidth = endPlaceable.width
        val gap = 16.dp.roundToPx().coerceAtMost(width)
        val available = width - gap
        val stacked = listItemStacksValue(available, titleWidth, endWidth)
        val allocatedValue = if (stacked) width else listItemValueWidth(available, titleWidth, valueWidth + endWidth)
        val allocatedTitle = if (stacked) width else available - allocatedValue
        val titlePlaceable = measurables[0].measure(Constraints(maxWidth = allocatedTitle))
        val valuePlaceable = measurables[1].measure(Constraints(maxWidth = (allocatedValue - endWidth).coerceAtLeast(0)))
        val supportingPlaceable = measurables[2].measure(Constraints(maxWidth = allocatedTitle))
        val titleHeight = titlePlaceable.height + supportingPlaceable.height
        val valueHeight = maxOf(valuePlaceable.height, endPlaceable.height)
        val verticalGap = if (stacked) 8.dp.roundToPx() else 0
        val height = constraints.constrainHeight(
            if (stacked) titleHeight + verticalGap + valueHeight else maxOf(titleHeight, valueHeight),
        )
        layout(width, height) {
            val titleY = if (stacked) 0 else (height - titleHeight) / 2
            titlePlaceable.placeRelative(0, titleY)
            supportingPlaceable.placeRelative(0, titleY + titlePlaceable.height)
            val valueY = if (stacked) titleHeight + verticalGap else (height - valueHeight) / 2
            valuePlaceable.placeRelative(width - endPlaceable.width - valuePlaceable.width, valueY + (valueHeight - valuePlaceable.height) / 2)
            endPlaceable.placeRelative(width - endPlaceable.width, valueY + (valueHeight - endPlaceable.height) / 2)
        }
    }
}

internal fun listItemValueWidth(available: Int, titleWidth: Int, valueWidth: Int): Int =
    minOf(valueWidth, available - titleWidth).coerceIn(0, available)

internal fun listItemStacksValue(available: Int, titleWidth: Int, endWidth: Int): Boolean =
    titleWidth.toLong() + endWidth >= available
