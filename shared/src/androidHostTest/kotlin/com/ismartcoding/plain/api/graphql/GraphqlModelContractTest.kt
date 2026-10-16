package com.ismartcoding.plain.api.graphql

import com.ismartcoding.plain.api.graphql.models.SmsAttachment

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlin.test.Test
import kotlin.test.assertEquals

class GraphqlModelContractTest {
    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    @Test
    fun baseModelsMatchSchemaWithoutLazyTagFields() {
        val sdl = java.io.File("apitest/schema.graphqls").readText()
            .replace(Regex("\"\"\"[\\s\\S]*?\"\"\""), "")
        val models: Map<String, SerialDescriptor> = mapOf(
            "ActionResult" to com.ismartcoding.plain.api.graphql.models.ActionResult.serializer().descriptor,
            "AndroidExtras" to com.ismartcoding.plain.api.graphql.models.AndroidExtras.serializer().descriptor,
            "App" to com.ismartcoding.plain.api.graphql.models.App.serializer().descriptor,
            "AppFile" to com.ismartcoding.plain.api.graphql.models.AppFile.serializer().descriptor,
            "Audio" to com.ismartcoding.plain.api.graphql.models.Audio.serializer().descriptor,
            "AudioFileInfo" to com.ismartcoding.plain.api.graphql.models.AudioFileInfo.serializer().descriptor,
            "AudioItem" to com.ismartcoding.plain.api.graphql.models.AudioItem.serializer().descriptor,
            "AudioPlayHistory" to com.ismartcoding.plain.api.graphql.models.AudioPlayHistory.serializer().descriptor,
            "AudioPlayback" to com.ismartcoding.plain.api.graphql.models.AudioPlayback.serializer().descriptor,
            "AudioPlaylist" to com.ismartcoding.plain.api.graphql.models.AudioPlaylist.serializer().descriptor,
            "Bookmark" to com.ismartcoding.plain.api.graphql.models.Bookmark.serializer().descriptor,
            "BookmarkGroup" to com.ismartcoding.plain.api.graphql.models.BookmarkGroup.serializer().descriptor,
            "BookmarkInput" to com.ismartcoding.plain.api.graphql.models.BookmarkInput.serializer().descriptor,
            "Call" to com.ismartcoding.plain.api.graphql.models.Call.serializer().descriptor,
            "Certificate" to com.ismartcoding.plain.api.graphql.models.Certificate.serializer().descriptor,
            "ChatChannel" to com.ismartcoding.plain.api.graphql.models.ChatChannel.serializer().descriptor,
            "ChatChannelMember" to com.ismartcoding.plain.api.graphql.models.ChatChannelMember.serializer().descriptor,
            "ChatItem" to com.ismartcoding.plain.api.graphql.models.ChatItem.serializer().descriptor,
            "ClipboardItem" to com.ismartcoding.plain.api.graphql.models.ClipboardItem.serializer().descriptor,
            "Contact" to com.ismartcoding.plain.api.graphql.models.Contact.serializer().descriptor,
            "ContactAddress" to com.ismartcoding.plain.api.graphql.models.ContactAddress.serializer().descriptor,
            "ContactAddressInput" to com.ismartcoding.plain.api.graphql.models.ContactAddressInput.serializer().descriptor,
            "ContactEmail" to com.ismartcoding.plain.api.graphql.models.ContactEmail.serializer().descriptor,
            "ContactEmailInput" to com.ismartcoding.plain.api.graphql.models.ContactEmailInput.serializer().descriptor,
            "ContactEvent" to com.ismartcoding.plain.api.graphql.models.ContactEvent.serializer().descriptor,
            "ContactEventInput" to com.ismartcoding.plain.api.graphql.models.ContactEventInput.serializer().descriptor,
            "ContactGroup" to com.ismartcoding.plain.api.graphql.models.ContactGroup.serializer().descriptor,
            "ContactIm" to com.ismartcoding.plain.api.graphql.models.ContactIm.serializer().descriptor,
            "ContactImInput" to com.ismartcoding.plain.api.graphql.models.ContactImInput.serializer().descriptor,
            "ContactInput" to com.ismartcoding.plain.api.graphql.models.ContactInput.serializer().descriptor,
            "ContactPhoneInput" to com.ismartcoding.plain.api.graphql.models.ContactPhoneInput.serializer().descriptor,
            "ContactPhoneNumber" to com.ismartcoding.plain.api.graphql.models.ContactPhoneNumber.serializer().descriptor,
            "ContactSource" to com.ismartcoding.plain.api.graphql.models.ContactSource.serializer().descriptor,
            "ContactWebsite" to com.ismartcoding.plain.api.graphql.models.ContactWebsite.serializer().descriptor,
            "ContactWebsiteInput" to com.ismartcoding.plain.api.graphql.models.ContactWebsiteInput.serializer().descriptor,
            "DbTableColumn" to com.ismartcoding.plain.api.graphql.models.DbTableColumn.serializer().descriptor,
            "DbTableInfo" to com.ismartcoding.plain.api.graphql.models.DbTableInfo.serializer().descriptor,
            "DeviceInfo" to com.ismartcoding.plain.api.graphql.models.DeviceInfo.serializer().descriptor,
            "DeviceStatus" to com.ismartcoding.plain.api.graphql.models.DeviceStatus.serializer().descriptor,
            "DisplayInfo" to com.ismartcoding.plain.api.graphql.models.DisplayInfo.serializer().descriptor,
            "Doc" to com.ismartcoding.plain.api.graphql.models.Doc.serializer().descriptor,
            "DocExtGroup" to com.ismartcoding.plain.api.graphql.models.DocExtGroup.serializer().descriptor,
            "FavoriteFolder" to com.ismartcoding.plain.api.graphql.models.FavoriteFolder.serializer().descriptor,
            "Feed" to com.ismartcoding.plain.api.graphql.models.Feed.serializer().descriptor,
            "FeedEntry" to com.ismartcoding.plain.api.graphql.models.FeedEntry.serializer().descriptor,
            "FeedEntryCount" to com.ismartcoding.plain.api.graphql.models.FeedEntryCount.serializer().descriptor,
            "FeedError" to com.ismartcoding.plain.api.graphql.models.FeedError.serializer().descriptor,
            "FeedSyncState" to com.ismartcoding.plain.api.graphql.models.FeedSyncState.serializer().descriptor,
            "File" to com.ismartcoding.plain.api.graphql.models.File.serializer().descriptor,
            "FileInfo" to com.ismartcoding.plain.api.graphql.models.FileInfo.serializer().descriptor,
            "Image" to com.ismartcoding.plain.api.graphql.models.Image.serializer().descriptor,
            "ImageEditorProject" to com.ismartcoding.plain.api.graphql.models.ImageEditorProject.serializer().descriptor,
            "ImageEditorProjectInput" to com.ismartcoding.plain.api.graphql.models.ImageEditorProjectInput.serializer().descriptor,
            "ImageEditorProjectSummary" to com.ismartcoding.plain.api.graphql.models.ImageEditorProjectSummary.serializer().descriptor,
            "ImageFileInfo" to com.ismartcoding.plain.api.graphql.models.ImageFileInfo.serializer().descriptor,
            "ImageSearchStatus" to com.ismartcoding.plain.api.graphql.models.ImageSearchStatus.serializer().descriptor,
            "KeyValuePair" to com.ismartcoding.plain.api.graphql.models.KeyValuePair.serializer().descriptor,
            "Location" to com.ismartcoding.plain.api.graphql.models.Location.serializer().descriptor,
            "MediaBucket" to com.ismartcoding.plain.api.graphql.models.MediaBucket.serializer().descriptor,
            "MergeTask" to com.ismartcoding.plain.api.graphql.models.MergeTask.serializer().descriptor,
            "Mount" to com.ismartcoding.plain.api.graphql.models.Mount.serializer().descriptor,
            "Note" to com.ismartcoding.plain.api.graphql.models.Note.serializer().descriptor,
            "NoteInput" to com.ismartcoding.plain.api.graphql.models.NoteInput.serializer().descriptor,
            "Notification" to com.ismartcoding.plain.api.graphql.models.Notification.serializer().descriptor,
            "Organization" to com.ismartcoding.plain.api.graphql.models.Organization.serializer().descriptor,
            "OrganizationInput" to com.ismartcoding.plain.api.graphql.models.OrganizationInput.serializer().descriptor,
            "Package" to com.ismartcoding.plain.api.graphql.models.Package.serializer().descriptor,
            "PackageInstallPending" to com.ismartcoding.plain.api.graphql.models.PackageInstallPending.serializer().descriptor,
            "PackageStatus" to com.ismartcoding.plain.api.graphql.models.PackageStatus.serializer().descriptor,
            "PairingDeviceInput" to com.ismartcoding.plain.api.graphql.models.PairingDeviceInput.serializer().descriptor,
            "PairingRequestInput" to com.ismartcoding.plain.api.graphql.models.PairingRequestInput.serializer().descriptor,
            "Peer" to com.ismartcoding.plain.api.graphql.models.Peer.serializer().descriptor,
            "PhoneGeo" to com.ismartcoding.plain.api.graphql.models.PhoneGeo.serializer().descriptor,
            "PomodoroSettings" to com.ismartcoding.plain.api.graphql.models.PomodoroSettings.serializer().descriptor,
            "PomodoroToday" to com.ismartcoding.plain.api.graphql.models.PomodoroToday.serializer().descriptor,
            "ScreenMirrorQuality" to com.ismartcoding.plain.api.graphql.models.ScreenMirrorQuality.serializer().descriptor,
            "ScreenMirrorVideoCodec" to com.ismartcoding.plain.api.graphql.models.ScreenMirrorVideoCodec.serializer().descriptor,
            "Sim" to com.ismartcoding.plain.api.graphql.models.Sim.serializer().descriptor,
            "Sms" to com.ismartcoding.plain.api.graphql.models.Sms.serializer().descriptor,
            "SmsAttachment" to com.ismartcoding.plain.api.graphql.models.SmsAttachment.serializer().descriptor,
            "SmsConversation" to com.ismartcoding.plain.api.graphql.models.SmsConversation.serializer().descriptor,
            "SmsCounts" to com.ismartcoding.plain.api.graphql.models.SmsCounts.serializer().descriptor,
            "Tag" to com.ismartcoding.plain.api.graphql.models.Tag.serializer().descriptor,
            "TagRelation" to com.ismartcoding.plain.api.graphql.models.TagRelation.serializer().descriptor,
            "TagRelationStub" to com.ismartcoding.plain.api.graphql.models.TagRelationStub.serializer().descriptor,
            "Temperature" to com.ismartcoding.plain.api.graphql.models.Temperature.serializer().descriptor,
            "Video" to com.ismartcoding.plain.api.graphql.models.Video.serializer().descriptor,
            "VideoFileInfo" to com.ismartcoding.plain.api.graphql.models.VideoFileInfo.serializer().descriptor,
            "SettingsUpdate" to com.ismartcoding.plain.api.graphql.models.SettingsUpdate.serializer().descriptor,
            "SystemSettings" to com.ismartcoding.plain.api.graphql.models.SystemSettings.serializer().descriptor,
            "SystemSettingsInput" to com.ismartcoding.plain.api.graphql.models.SystemSettingsInput.serializer().descriptor,
            "UserSettings" to com.ismartcoding.plain.api.graphql.models.UserSettings.serializer().descriptor,
            "UserSettingsInput" to com.ismartcoding.plain.api.graphql.models.UserSettingsInput.serializer().descriptor,
        )
        val schemaWithoutDescriptions = Regex("\"\"\"[\\s\\S]*?\"\"\"").replace(sdl, "")
        val blocks = Regex("""(?:type|input) (\w+)(?:\s+implements\s+\w+)?\s*\{([^}]+)}""").findAll(schemaWithoutDescriptions)
            .associate { it.groupValues[1] to it.groupValues[2] }
        val lazyTags = setOf("Audio", "Call", "Contact", "Sms", "Doc", "Image", "Video", "Note", "FeedEntry")
        val mismatches = mutableListOf<String>()
        models.forEach { (name, descriptor) ->
            val fields = Regex("""(\w+)\s*:\s*([\w!\[\]]+)""").findAll(blocks.getValue(name))
                .associate { it.groupValues[1] to !it.groupValues[2].endsWith("!") }
            val actual = (0 until descriptor.elementsCount).associate {
                descriptor.getElementName(it) to descriptor.getElementDescriptor(it).isNullable
            }
            if (name in lazyTags) {
                assertEquals(false, fields.getValue("tags"), "$name.tags remains a non-null GraphQL field")
                assertEquals(false, actual.containsKey("tags"), "$name.tags must be resolved separately")
            }
            val expected = fields.filterKeys { it != "tags" || name !in lazyTags }
            if (expected != actual) mismatches.add("$name expected=$expected actual=$actual")
        }
        assertEquals(emptyList<String>(), mismatches)
    }
}
