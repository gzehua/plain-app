package com.ismartcoding.plain.ui.state

import com.ismartcoding.plain.platform.system.AppFeatureType
import com.ismartcoding.plain.platform.system.has

import com.ismartcoding.plain.platform.system.Capability
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppFeatureCapabilitiesTest {
    @Test
    fun optionalFeaturesFollowTheAdvertisedCapabilitiesWithoutPlatformChecks() {
        val capabilities = listOf(Capability.NOTES, Capability.MEDIA_TRASH, Capability.CONTACTS)
        assertTrue(AppFeatureType.NOTES.has(capabilities))
        assertTrue(AppFeatureType.MEDIA_TRASH.has(capabilities))
        assertTrue(AppFeatureType.CONTACTS.has(capabilities))
        assertFalse(AppFeatureType.SMS.has(capabilities))
        assertFalse(AppFeatureType.APPS.has(capabilities))
        assertFalse(AppFeatureType.MEDIA_FAVORITE.has(capabilities))
        assertFalse(AppFeatureType.NOTIFICATIONS.has(capabilities))
    }

    @Test
    fun mediaMutationControlsRequireEachDeclaredCapability() {
        for ((feature, capability) in listOf(
            AppFeatureType.MEDIA_DELETE to Capability.MEDIA_DELETE,
            AppFeatureType.MEDIA_MOVE to Capability.MEDIA_MOVE,
            AppFeatureType.MEDIA_TRASH to Capability.MEDIA_TRASH,
            AppFeatureType.MEDIA_FAVORITE to Capability.MEDIA_FAVORITE,
        )) {
            assertFalse(feature.has(emptyList()))
            assertTrue(feature.has(listOf(capability)))
            assertFalse(feature.has(Capability.entries.filter { it != capability }))
        }
    }

    @Test
    fun distributionAndFileBrowsingFeaturesUseTheirOwnCapabilities() {
        assertFalse(AppFeatureType.CHECK_UPDATES.has(emptyList()))
        assertTrue(AppFeatureType.CHECK_UPDATES.has(listOf(Capability.APP_UPDATES)))
        assertFalse(AppFeatureType.DONATION.has(listOf(Capability.APP_UPDATES)))
        assertTrue(AppFeatureType.DONATION.has(listOf(Capability.DONATION)))
        assertFalse(AppFeatureType.FILES.has(emptyList()))
        assertTrue(AppFeatureType.FILES.has(listOf(Capability.FILE_BROWSER)))
    }
    @Test
    fun libraryAndToolEntriesRequireTheirImplementedProviders() {
        for ((feature, capability) in listOf(
            AppFeatureType.FILES to Capability.FILE_BROWSER,
            AppFeatureType.IMAGES to Capability.IMAGE_LIBRARY,
            AppFeatureType.VIDEOS to Capability.VIDEO_LIBRARY,
            AppFeatureType.AUDIO to Capability.AUDIO_LIBRARY,
            AppFeatureType.DOCS to Capability.DOCUMENT_LIBRARY,
            AppFeatureType.SOUND_METER to Capability.SOUND_METER,
        )) {
            assertFalse(feature.has(emptyList()))
            assertTrue(feature.has(listOf(capability)))
            assertFalse(feature.has(Capability.entries.filter { it != capability }))
        }
    }

}
