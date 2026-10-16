package com.ismartcoding.plain.helpers

import com.ismartcoding.plain.graphql.models.AppFile
import com.ismartcoding.plain.lib.TimeHelper
import kotlin.test.Test
import kotlin.test.assertEquals

class AppFileStoreExtTest {
    @Test
    fun toFidUri_usesRealPathFileName() {
        val dFile = AppFile(id = "aabbccdd", size = 0, mimeType = "", realPath = "/data/files/aa/bb/aabbccdd.properties",
            fileName = "", createdAt = TimeHelper.now(), updatedAt = TimeHelper.now())
        assertEquals("fid:aabbccdd.properties", AppFileStore.toFidUri(dFile))
        dFile.realPath = "/data/files/aa/bb/aabbccdd"
        assertEquals("fid:aabbccdd", AppFileStore.toFidUri(dFile))
    }
}
