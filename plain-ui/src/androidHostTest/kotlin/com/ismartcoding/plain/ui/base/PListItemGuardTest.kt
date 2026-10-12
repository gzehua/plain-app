package com.ismartcoding.plain.ui.base

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class PListItemGuardTest {
    @Test
    fun fittingTextKeepsBothSidesComplete() {
        assertEquals(40, listItemValueWidth(300, 160, 40))
        assertEquals(160, listItemValueWidth(300, 40, 160))
        assertEquals(100, listItemValueWidth(300, 100, 100))
    }

    @Test
    fun exactlyFittingTextKeepsBothSidesComplete() {
        assertEquals(100, listItemValueWidth(300, 200, 100))
        assertEquals(200, listItemValueWidth(300, 100, 200))
        assertEquals(150, listItemValueWidth(300, 150, 150))
    }

    @Test
    fun overflowingTextAlwaysPreservesTitleWidth() {
        assertEquals(60, listItemValueWidth(300, 240, 160))
        assertEquals(220, listItemValueWidth(300, 80, 400))
        assertEquals(100, listItemValueWidth(300, 200, 200))
    }

    @Test
    fun titleConsumesAllAvailableSpaceWithoutNegativeWidths() {
        assertEquals(0, listItemValueWidth(300, 300, 100))
        assertEquals(0, listItemValueWidth(300, 600, 100))
        assertEquals(0, listItemValueWidth(0, 100, 100))
    }

    @Test
    fun fullWidthTitleAndControlsMoveValueBelow() {
        assertFalse(listItemStacksValue(300, 200, 24))
        assertTrue(listItemStacksValue(300, 276, 24))
        assertTrue(listItemStacksValue(300, 300, 0))
        assertTrue(listItemStacksValue(300, 600, 0))
        assertTrue(listItemStacksValue(0, 100, 24))
    }

    @Test
    fun emptyTextDoesNotReserveHalfTheRow() {
        assertEquals(0, listItemValueWidth(300, 100, 0))
        assertEquals(300, listItemValueWidth(300, 0, 500))
    }
}
