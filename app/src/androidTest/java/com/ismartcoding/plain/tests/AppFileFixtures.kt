package com.ismartcoding.plain.tests

import com.ismartcoding.plain.helpers.AppFileStore

object AppFileFixtures {
    suspend fun exists(id: String): Boolean = AppFileStore.page(0, 1, "text:$id").isNotEmpty()
}
