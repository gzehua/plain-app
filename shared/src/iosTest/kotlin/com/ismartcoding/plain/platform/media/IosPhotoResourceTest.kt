package com.ismartcoding.plain.platform.media

import platform.Photos.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class IosPhotoResourceTest {
    @Test
    fun imagesPreferCurrentEditsAndNeverReadTheLivePhotoVideoInstead() {
        assertEquals(2, IosPhotoResource.resourceIndex(PHAssetMediaTypeImage,
            listOf(PHAssetResourceTypePairedVideo, PHAssetResourceTypePhoto, PHAssetResourceTypeFullSizePhoto)))
        assertEquals(1, IosPhotoResource.resourceIndex(PHAssetMediaTypeImage,
            listOf(PHAssetResourceTypePairedVideo, PHAssetResourceTypePhoto)))
    }

    @Test
    fun videosPreferCurrentEditsAndKeepTheOriginalWhenUnedited() {
        assertEquals(1, IosPhotoResource.resourceIndex(PHAssetMediaTypeVideo,
            listOf(PHAssetResourceTypeVideo, PHAssetResourceTypeFullSizeVideo)))
        assertEquals(0, IosPhotoResource.resourceIndex(PHAssetMediaTypeVideo,
            listOf(PHAssetResourceTypeVideo)))
    }

    @Test
    fun auxiliaryResourcesAndUnsupportedKindsCannotStandInForTheAsset() {
        assertFailsWith<IllegalStateException> {
            IosPhotoResource.resourceIndex(PHAssetMediaTypeImage, listOf(PHAssetResourceTypeAdjustmentData, PHAssetResourceTypePairedVideo))
        }
        assertFailsWith<IllegalStateException> {
            IosPhotoResource.resourceIndex(PHAssetMediaTypeUnknown, listOf(PHAssetResourceTypePhoto))
        }
    }
}
