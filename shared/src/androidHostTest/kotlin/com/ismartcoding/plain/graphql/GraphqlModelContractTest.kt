package com.ismartcoding.plain.graphql

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlin.test.Test
import kotlin.test.assertEquals

class GraphqlModelContractTest {
    @OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
    @Test
    fun restoredPublicModelsMatchEverySchemaFieldAndItsNullability() {
        val sdl = java.io.File("apitest/schema.graphqls").readText()
            .replace(Regex("\"\"\"[\\s\\S]*?\"\"\""), "")
        val models: Map<String, SerialDescriptor> = mapOf(
            "ActionResult" to com.ismartcoding.plain.graphql.models.ActionResult.serializer().descriptor,
            "AndroidExtras" to com.ismartcoding.plain.graphql.models.AndroidExtras.serializer().descriptor,
            "App" to com.ismartcoding.plain.graphql.models.App.serializer().descriptor,
            "AppFile" to com.ismartcoding.plain.graphql.models.AppFile.serializer().descriptor,
            "Audio" to com.ismartcoding.plain.graphql.models.Audio.serializer().descriptor,
            "AudioFileInfo" to com.ismartcoding.plain.graphql.models.AudioFileInfo.serializer().descriptor,
            "AudioItem" to com.ismartcoding.plain.graphql.models.AudioItem.serializer().descriptor,
            "AudioPlayHistory" to com.ismartcoding.plain.graphql.models.AudioPlayHistory.serializer().descriptor,
            "AudioPlayback" to com.ismartcoding.plain.graphql.models.AudioPlayback.serializer().descriptor,
            "AudioPlaylist" to com.ismartcoding.plain.graphql.models.AudioPlaylist.serializer().descriptor,
            "Bookmark" to com.ismartcoding.plain.graphql.models.Bookmark.serializer().descriptor,
            "BookmarkGroup" to com.ismartcoding.plain.graphql.models.BookmarkGroup.serializer().descriptor,
            "BookmarkInput" to com.ismartcoding.plain.graphql.models.BookmarkInput.serializer().descriptor,
            "Call" to com.ismartcoding.plain.graphql.models.Call.serializer().descriptor,
            "Certificate" to com.ismartcoding.plain.graphql.models.Certificate.serializer().descriptor,
            "ChatChannel" to com.ismartcoding.plain.graphql.models.ChatChannel.serializer().descriptor,
            "ChatChannelMember" to com.ismartcoding.plain.graphql.models.ChatChannelMember.serializer().descriptor,
            "ChatItem" to com.ismartcoding.plain.graphql.models.ChatItem.serializer().descriptor,
            "ClipboardItem" to com.ismartcoding.plain.graphql.models.ClipboardItem.serializer().descriptor,
            "Contact" to com.ismartcoding.plain.graphql.models.Contact.serializer().descriptor,
            "ContactAddress" to com.ismartcoding.plain.graphql.models.ContactAddress.serializer().descriptor,
            "ContactAddressInput" to com.ismartcoding.plain.graphql.models.ContactAddressInput.serializer().descriptor,
            "ContactEmail" to com.ismartcoding.plain.graphql.models.ContactEmail.serializer().descriptor,
            "ContactEmailInput" to com.ismartcoding.plain.graphql.models.ContactEmailInput.serializer().descriptor,
            "ContactEvent" to com.ismartcoding.plain.graphql.models.ContactEvent.serializer().descriptor,
            "ContactEventInput" to com.ismartcoding.plain.graphql.models.ContactEventInput.serializer().descriptor,
            "ContactGroup" to com.ismartcoding.plain.graphql.models.ContactGroup.serializer().descriptor,
            "ContactIm" to com.ismartcoding.plain.graphql.models.ContactIm.serializer().descriptor,
            "ContactImInput" to com.ismartcoding.plain.graphql.models.ContactImInput.serializer().descriptor,
            "ContactInput" to com.ismartcoding.plain.graphql.models.ContactInput.serializer().descriptor,
            "ContactPhoneInput" to com.ismartcoding.plain.graphql.models.ContactPhoneInput.serializer().descriptor,
            "ContactPhoneNumber" to com.ismartcoding.plain.graphql.models.ContactPhoneNumber.serializer().descriptor,
            "ContactSource" to com.ismartcoding.plain.graphql.models.ContactSource.serializer().descriptor,
            "ContactWebsite" to com.ismartcoding.plain.graphql.models.ContactWebsite.serializer().descriptor,
            "ContactWebsiteInput" to com.ismartcoding.plain.graphql.models.ContactWebsiteInput.serializer().descriptor,
            "DbTableColumn" to com.ismartcoding.plain.graphql.models.DbTableColumn.serializer().descriptor,
            "DbTableInfo" to com.ismartcoding.plain.graphql.models.DbTableInfo.serializer().descriptor,
            "DeviceInfo" to com.ismartcoding.plain.graphql.models.DeviceInfo.serializer().descriptor,
            "DeviceStatus" to com.ismartcoding.plain.graphql.models.DeviceStatus.serializer().descriptor,
            "DisplayInfo" to com.ismartcoding.plain.graphql.models.DisplayInfo.serializer().descriptor,
            "Doc" to com.ismartcoding.plain.graphql.models.Doc.serializer().descriptor,
            "DocExtGroup" to com.ismartcoding.plain.graphql.models.DocExtGroup.serializer().descriptor,
            "FavoriteFolder" to com.ismartcoding.plain.graphql.models.FavoriteFolder.serializer().descriptor,
            "Feed" to com.ismartcoding.plain.graphql.models.Feed.serializer().descriptor,
            "FeedEntry" to com.ismartcoding.plain.graphql.models.FeedEntry.serializer().descriptor,
            "FeedEntryCount" to com.ismartcoding.plain.graphql.models.FeedEntryCount.serializer().descriptor,
            "FeedError" to com.ismartcoding.plain.graphql.models.FeedError.serializer().descriptor,
            "FeedSyncState" to com.ismartcoding.plain.graphql.models.FeedSyncState.serializer().descriptor,
            "File" to com.ismartcoding.plain.graphql.models.File.serializer().descriptor,
            "FileInfo" to com.ismartcoding.plain.graphql.models.FileInfo.serializer().descriptor,
            "Image" to com.ismartcoding.plain.graphql.models.Image.serializer().descriptor,
            "ImageEditorProject" to com.ismartcoding.plain.graphql.models.ImageEditorProject.serializer().descriptor,
            "ImageEditorProjectInput" to com.ismartcoding.plain.graphql.models.ImageEditorProjectInput.serializer().descriptor,
            "ImageEditorProjectSummary" to com.ismartcoding.plain.graphql.models.ImageEditorProjectSummary.serializer().descriptor,
            "ImageFileInfo" to com.ismartcoding.plain.graphql.models.ImageFileInfo.serializer().descriptor,
            "ImageSearchStatus" to com.ismartcoding.plain.graphql.models.ImageSearchStatus.serializer().descriptor,
            "KeyValuePair" to com.ismartcoding.plain.graphql.models.KeyValuePair.serializer().descriptor,
            "Location" to com.ismartcoding.plain.graphql.models.Location.serializer().descriptor,
            "MediaBucket" to com.ismartcoding.plain.graphql.models.MediaBucket.serializer().descriptor,
            "MergeTask" to com.ismartcoding.plain.graphql.models.MergeTask.serializer().descriptor,
            "Mount" to com.ismartcoding.plain.graphql.models.Mount.serializer().descriptor,
            "Note" to com.ismartcoding.plain.graphql.models.Note.serializer().descriptor,
            "NoteInput" to com.ismartcoding.plain.graphql.models.NoteInput.serializer().descriptor,
            "Notification" to com.ismartcoding.plain.graphql.models.Notification.serializer().descriptor,
            "Organization" to com.ismartcoding.plain.graphql.models.Organization.serializer().descriptor,
            "OrganizationInput" to com.ismartcoding.plain.graphql.models.OrganizationInput.serializer().descriptor,
            "Package" to com.ismartcoding.plain.graphql.models.Package.serializer().descriptor,
            "PackageInstallPending" to com.ismartcoding.plain.graphql.models.PackageInstallPending.serializer().descriptor,
            "PackageStatus" to com.ismartcoding.plain.graphql.models.PackageStatus.serializer().descriptor,
            "PairingDeviceInput" to com.ismartcoding.plain.graphql.models.PairingDeviceInput.serializer().descriptor,
            "PairingRequestInput" to com.ismartcoding.plain.graphql.models.PairingRequestInput.serializer().descriptor,
            "Peer" to com.ismartcoding.plain.graphql.models.Peer.serializer().descriptor,
            "PhoneGeo" to com.ismartcoding.plain.graphql.models.PhoneGeo.serializer().descriptor,
            "PomodoroSettings" to com.ismartcoding.plain.graphql.models.PomodoroSettings.serializer().descriptor,
            "PomodoroToday" to com.ismartcoding.plain.graphql.models.PomodoroToday.serializer().descriptor,
            "ScreenMirrorQuality" to com.ismartcoding.plain.graphql.models.ScreenMirrorQuality.serializer().descriptor,
            "ScreenMirrorVideoCodec" to com.ismartcoding.plain.graphql.models.ScreenMirrorVideoCodec.serializer().descriptor,
            "Sim" to com.ismartcoding.plain.graphql.models.Sim.serializer().descriptor,
            "Sms" to com.ismartcoding.plain.graphql.models.Sms.serializer().descriptor,
            "SmsAttachment" to com.ismartcoding.plain.graphql.models.SmsAttachment.serializer().descriptor,
            "SmsConversation" to com.ismartcoding.plain.graphql.models.SmsConversation.serializer().descriptor,
            "SmsCounts" to com.ismartcoding.plain.graphql.models.SmsCounts.serializer().descriptor,
            "Tag" to com.ismartcoding.plain.graphql.models.Tag.serializer().descriptor,
            "TagRelation" to com.ismartcoding.plain.graphql.models.TagRelation.serializer().descriptor,
            "TagRelationStub" to com.ismartcoding.plain.graphql.models.TagRelationStub.serializer().descriptor,
            "Temperature" to com.ismartcoding.plain.graphql.models.Temperature.serializer().descriptor,
            "Video" to com.ismartcoding.plain.graphql.models.Video.serializer().descriptor,
            "VideoFileInfo" to com.ismartcoding.plain.graphql.models.VideoFileInfo.serializer().descriptor,
            "SettingsUpdate" to com.ismartcoding.plain.graphql.models.SettingsUpdate.serializer().descriptor,
            "SystemSettings" to com.ismartcoding.plain.graphql.models.SystemSettings.serializer().descriptor,
            "SystemSettingsInput" to com.ismartcoding.plain.graphql.models.SystemSettingsInput.serializer().descriptor,
            "UserSettings" to com.ismartcoding.plain.graphql.models.UserSettings.serializer().descriptor,
            "UserSettingsInput" to com.ismartcoding.plain.graphql.models.UserSettingsInput.serializer().descriptor,
        )
        val blocks = Regex("""(?:type|input) (\w+)\s*\{([^}]+)}""").findAll(sdl)
            .associate { it.groupValues[1] to it.groupValues[2] }
        models.forEach { (name, descriptor) ->
            val fields = Regex("""(\w+)\s*:\s*([\w!\[\]]+)""").findAll(blocks.getValue(name))
                .associate { it.groupValues[1] to !it.groupValues[2].endsWith("!") }
            val actual = (0 until descriptor.elementsCount).associate {
                descriptor.getElementName(it) to descriptor.getElementDescriptor(it).isNullable
            }
            assertEquals(fields, actual, name)
        }
    }
}
