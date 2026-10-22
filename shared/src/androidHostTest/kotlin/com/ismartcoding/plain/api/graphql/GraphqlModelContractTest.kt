package com.ismartcoding.plain.api.graphql

import com.ismartcoding.plain.models.SmsAttachment

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlin.test.Test
import kotlin.test.assertEquals

class GraphqlModelContractTest {
    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    @Test
    fun baseModelsMatchSchemaWithoutLazyTagFields() {
        val sdl = readMainSchema()
            .replace(Regex("\"\"\"[\\s\\S]*?\"\"\""), "")
        val models: Map<String, SerialDescriptor> = mapOf(
            "ActionResult" to com.ismartcoding.plain.models.ActionResult.serializer().descriptor,
            "AndroidExtras" to com.ismartcoding.plain.models.AndroidExtras.serializer().descriptor,
            "App" to com.ismartcoding.plain.models.App.serializer().descriptor,
            "AppFile" to com.ismartcoding.plain.models.AppFile.serializer().descriptor,
            "Audio" to com.ismartcoding.plain.models.Audio.serializer().descriptor,
            "AudioFileInfo" to com.ismartcoding.plain.models.AudioFileInfo.serializer().descriptor,
            "AudioItem" to com.ismartcoding.plain.models.AudioItem.serializer().descriptor,
            "AudioPlayHistory" to com.ismartcoding.plain.models.AudioPlayHistory.serializer().descriptor,
            "AudioPlayback" to com.ismartcoding.plain.models.AudioPlayback.serializer().descriptor,
            "AudioPlaylist" to com.ismartcoding.plain.models.AudioPlaylist.serializer().descriptor,
            "Bookmark" to com.ismartcoding.plain.models.Bookmark.serializer().descriptor,
            "BookmarkGroup" to com.ismartcoding.plain.models.BookmarkGroup.serializer().descriptor,
            "BookmarkInput" to com.ismartcoding.plain.models.BookmarkInput.serializer().descriptor,
            "Call" to com.ismartcoding.plain.models.Call.serializer().descriptor,
            "Certificate" to com.ismartcoding.plain.models.Certificate.serializer().descriptor,
            "ChatChannel" to com.ismartcoding.plain.models.ChatChannel.serializer().descriptor,
            "ChatChannelMember" to com.ismartcoding.plain.models.ChatChannelMember.serializer().descriptor,
            "ChatItem" to com.ismartcoding.plain.models.ChatItem.serializer().descriptor,
            "ClipboardItem" to com.ismartcoding.plain.models.ClipboardItem.serializer().descriptor,
            "Contact" to com.ismartcoding.plain.models.Contact.serializer().descriptor,
            "ContactAddress" to com.ismartcoding.plain.models.ContactAddress.serializer().descriptor,
            "ContactAddressInput" to com.ismartcoding.plain.models.ContactAddressInput.serializer().descriptor,
            "ContactEmail" to com.ismartcoding.plain.models.ContactEmail.serializer().descriptor,
            "ContactEmailInput" to com.ismartcoding.plain.models.ContactEmailInput.serializer().descriptor,
            "ContactEvent" to com.ismartcoding.plain.models.ContactEvent.serializer().descriptor,
            "ContactEventInput" to com.ismartcoding.plain.models.ContactEventInput.serializer().descriptor,
            "ContactGroup" to com.ismartcoding.plain.models.ContactGroup.serializer().descriptor,
            "ContactIm" to com.ismartcoding.plain.models.ContactIm.serializer().descriptor,
            "ContactImInput" to com.ismartcoding.plain.models.ContactImInput.serializer().descriptor,
            "ContactInput" to com.ismartcoding.plain.models.ContactInput.serializer().descriptor,
            "ContactPhoneInput" to com.ismartcoding.plain.models.ContactPhoneInput.serializer().descriptor,
            "ContactPhoneNumber" to com.ismartcoding.plain.models.ContactPhoneNumber.serializer().descriptor,
            "ContactSource" to com.ismartcoding.plain.models.ContactSource.serializer().descriptor,
            "ContactWebsite" to com.ismartcoding.plain.models.ContactWebsite.serializer().descriptor,
            "ContactWebsiteInput" to com.ismartcoding.plain.models.ContactWebsiteInput.serializer().descriptor,
            "DbTableColumn" to com.ismartcoding.plain.models.DbTableColumn.serializer().descriptor,
            "DbTableInfo" to com.ismartcoding.plain.models.DbTableInfo.serializer().descriptor,
            "DeviceInfo" to com.ismartcoding.plain.models.DeviceInfo.serializer().descriptor,
            "DeviceStatus" to com.ismartcoding.plain.models.DeviceStatus.serializer().descriptor,
            "DisplayInfo" to com.ismartcoding.plain.models.DisplayInfo.serializer().descriptor,
            "Doc" to com.ismartcoding.plain.models.Doc.serializer().descriptor,
            "DocExtGroup" to com.ismartcoding.plain.models.DocExtGroup.serializer().descriptor,
            "FavoriteFolder" to com.ismartcoding.plain.models.FavoriteFolder.serializer().descriptor,
            "Feed" to com.ismartcoding.plain.models.Feed.serializer().descriptor,
            "FeedPreview" to com.ismartcoding.plain.models.FeedPreview.serializer().descriptor,
            "FeedEntry" to com.ismartcoding.plain.models.FeedEntry.serializer().descriptor,
            "FeedEntryCount" to com.ismartcoding.plain.models.FeedEntryCount.serializer().descriptor,
            "FeedError" to com.ismartcoding.plain.models.FeedError.serializer().descriptor,
            "FeedSyncState" to com.ismartcoding.plain.models.FeedSyncState.serializer().descriptor,
            "File" to com.ismartcoding.plain.models.File.serializer().descriptor,
            "FileInfo" to com.ismartcoding.plain.models.FileInfo.serializer().descriptor,
            "Image" to com.ismartcoding.plain.models.Image.serializer().descriptor,
            "ImageEditorProject" to com.ismartcoding.plain.models.ImageEditorProject.serializer().descriptor,
            "ImageEditorProjectInput" to com.ismartcoding.plain.models.ImageEditorProjectInput.serializer().descriptor,
            "ImageEditorProjectSummary" to com.ismartcoding.plain.models.ImageEditorProjectSummary.serializer().descriptor,
            "ImageFileInfo" to com.ismartcoding.plain.models.ImageFileInfo.serializer().descriptor,
            "ImageSearchStatus" to com.ismartcoding.plain.models.ImageSearchStatus.serializer().descriptor,
            "KeyValuePair" to com.ismartcoding.plain.models.KeyValuePair.serializer().descriptor,
            "Location" to com.ismartcoding.plain.models.Location.serializer().descriptor,
            "MediaBucket" to com.ismartcoding.plain.models.MediaBucket.serializer().descriptor,
            "MergeTask" to com.ismartcoding.plain.models.MergeTask.serializer().descriptor,
            "Mount" to com.ismartcoding.plain.models.Mount.serializer().descriptor,
            "Note" to com.ismartcoding.plain.models.Note.serializer().descriptor,
            "NoteInput" to com.ismartcoding.plain.models.NoteInput.serializer().descriptor,
            "Notification" to com.ismartcoding.plain.models.Notification.serializer().descriptor,
            "Organization" to com.ismartcoding.plain.models.Organization.serializer().descriptor,
            "OrganizationInput" to com.ismartcoding.plain.models.OrganizationInput.serializer().descriptor,
            "Package" to com.ismartcoding.plain.models.Package.serializer().descriptor,
            "PackageInstallPending" to com.ismartcoding.plain.models.PackageInstallPending.serializer().descriptor,
            "PackageStatus" to com.ismartcoding.plain.models.PackageStatus.serializer().descriptor,
            "PairingDeviceInput" to com.ismartcoding.plain.models.PairingDeviceInput.serializer().descriptor,
            "PairingRequestInput" to com.ismartcoding.plain.models.PairingRequestInput.serializer().descriptor,
            "Peer" to com.ismartcoding.plain.models.Peer.serializer().descriptor,
            "PhoneGeo" to com.ismartcoding.plain.models.PhoneGeo.serializer().descriptor,
            "PomodoroSettings" to com.ismartcoding.plain.models.PomodoroSettings.serializer().descriptor,
            "PomodoroToday" to com.ismartcoding.plain.models.PomodoroToday.serializer().descriptor,
            "ScreenMirrorQuality" to com.ismartcoding.plain.models.ScreenMirrorQuality.serializer().descriptor,
            "ScreenMirrorVideoCodec" to com.ismartcoding.plain.models.ScreenMirrorVideoCodec.serializer().descriptor,
            "Sim" to com.ismartcoding.plain.models.Sim.serializer().descriptor,
            "Sms" to com.ismartcoding.plain.models.Sms.serializer().descriptor,
            "SmsAttachment" to com.ismartcoding.plain.models.SmsAttachment.serializer().descriptor,
            "SmsConversation" to com.ismartcoding.plain.models.SmsConversation.serializer().descriptor,
            "SmsCounts" to com.ismartcoding.plain.models.SmsCounts.serializer().descriptor,
            "Tag" to com.ismartcoding.plain.models.Tag.serializer().descriptor,
            "TagRelation" to com.ismartcoding.plain.models.TagRelation.serializer().descriptor,

            "Temperature" to com.ismartcoding.plain.models.Temperature.serializer().descriptor,
            "Video" to com.ismartcoding.plain.models.Video.serializer().descriptor,
            "VideoFileInfo" to com.ismartcoding.plain.models.VideoFileInfo.serializer().descriptor,
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
            if (name == "ChatChannel") {
                assertEquals(false, fields.getValue("key"), "The trusted native key remains in the schema")
                assertEquals(false, actual.containsKey("key"), "The UI model must not hydrate the channel key")
            }
            val expected = fields.filterKeys { (it != "tags" || name !in lazyTags) && (name != "ChatChannel" || it != "key") }
            if (expected != actual) mismatches.add("$name expected=$expected actual=$actual")
        }
        assertEquals(emptyList<String>(), mismatches)
    }
}
