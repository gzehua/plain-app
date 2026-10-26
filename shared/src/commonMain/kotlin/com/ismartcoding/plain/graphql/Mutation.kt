package com.ismartcoding.plain.graphql

import com.ismartcoding.plain.features.chat.models.MessageShare
import com.ismartcoding.plain.features.dlna.sender.CastCommand
import com.ismartcoding.plain.features.files.FileTaskOp
import com.ismartcoding.plain.features.files.FileTaskType
import com.ismartcoding.plain.features.media.actions.MediaAction
import com.ismartcoding.plain.features.shares.SharedDownloadInput
import com.ismartcoding.plain.features.shares.SharedFileDto
import com.ismartcoding.plain.features.shares.SharedLink
import com.ismartcoding.plain.models.ImageEditorProject
import com.ismartcoding.plain.models.ImageEditorProjectInput
import com.ismartcoding.plain.models.NoteInput
import com.ismartcoding.plain.models.PairingDeviceInput
import com.ismartcoding.plain.models.PairingRequestInput
import com.ismartcoding.plain.platform.system.DataType
import com.ismartcoding.plain.rust.graphql.GraphqlRequest
import com.ismartcoding.plain.rust.graphql.variable
import kotlin.time.Instant
import kotlinx.serialization.json.JsonElement

object Mutation {
    fun editChatText(id: String, text: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($id: ID!, $text: String!) { editChatText(id: $id, text: $text) { changed chat { $${Fragments.CHAT_ITEM} } } }",
        mapOf(variable("id", id), variable("text", text)),
    )

    fun sendChatItem(target: String, content: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($target: String!, $content: String!) { sendChatItem(target: $target, content: $content) { $${Fragments.CHAT_ITEM} } }",
        mapOf(variable("target", target), variable("content", content)),
    )

    fun forwardChatItems(id: String, targets: Collection<String>): GraphqlRequest = GraphqlRequest(
        $$"mutation($id: ID!, $targets: [String!]!) { forwardChatItems(id: $id, targets: $targets) { $${Fragments.CHAT_ITEM} } }",
        mapOf(variable("id", id), variable("targets", targets)),
    )

    fun deleteChatItems(query: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($query: String!) { deleteChatItems(query: $query) { affectedCount } }",
        mapOf(variable("query", query)),
    )

    fun deleteChatItem(id: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($id: ID!) { deleteChatItem(id: $id) }",
        mapOf(variable("id", id)),
    )

    fun pairDevice(input: PairingDeviceInput): GraphqlRequest = GraphqlRequest(
        $$"mutation($input: PairingDeviceInput!) { pairDevice(input: $input) { $${Fragments.PAIRING_STATE} } }",
        mapOf(variable("input", input)),
    )

    fun cancelPairing(generation: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($generation: String!) { cancelPairing(generation: $generation) }",
        mapOf(variable("generation", generation)),
    )

    fun respondToPairing(input: PairingRequestInput, accepted: Boolean): GraphqlRequest = GraphqlRequest(
        $$"mutation($input: PairingRequestInput!, $accepted: Boolean!) { respondToPairing(input: $input, accepted: $accepted) }",
        mapOf(variable("input", input), variable("accepted", accepted)),
    )

    fun acquireDiscovery(token: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($token: ID!) { acquireDiscovery(token: $token) }",
        mapOf(variable("token", token)),
    )

    fun releaseDiscovery(token: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($token: ID!) { releaseDiscovery(token: $token) }",
        mapOf(variable("token", token)),
    )

    fun createTag(type: String, name: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($type: DataType!, $name: String!) { createTag(type: $type, name: $name) { $${Fragments.TAG_FIELDS} } }",
        mapOf(variable("type", type), variable("name", name)),
    )

    fun updateTag(id: String, name: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($id: ID!, $name: String!) { updateTag(id: $id, name: $name) { $${Fragments.TAG_FIELDS} } }",
        mapOf(variable("id", id), variable("name", name)),
    )

    fun deleteTag(id: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($id: ID!) { deleteTag(id: $id) }",
        mapOf(variable("id", id)),
    )

    fun updateTagRelations(type: String, keys: Collection<String>, addTagIds: Collection<String>, removeTagIds: Collection<String>): GraphqlRequest = GraphqlRequest(
        $$"mutation($type: DataType!, $keys: [String!]!, $addTagIds: [ID!]!, $removeTagIds: [ID!]!) { updateTagRelations(type: $type, keys: $keys, addTagIds: $addTagIds, removeTagIds: $removeTagIds) }",
        mapOf(variable("type", type), variable("keys", keys), variable("addTagIds", addTagIds), variable("removeTagIds", removeTagIds)),
    )

    fun markFeedEntriesRead(query: String, read: Boolean): GraphqlRequest = GraphqlRequest(
        $$"mutation($query: String!, $read: Boolean!) { markFeedEntriesRead(query: $query, read: $read) { affectedCount } }",
        mapOf(variable("query", query), variable("read", read)),
    )

    fun deleteFeedEntries(query: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($query: String!) { deleteFeedEntries(query: $query) { affectedCount } }",
        mapOf(variable("query", query)),
    )

    fun createFeed(url: String, name: String, fetchContent: Boolean): GraphqlRequest = GraphqlRequest(
        $$"mutation($url: String!, $name: String, $fetchContent: Boolean!) { createFeed(url: $url, name: $name, fetchContent: $fetchContent) { $${Fragments.FEED_FIELDS} } }",
        mapOf(variable("url", url), variable("name", name), variable("fetchContent", fetchContent)),
    )

    fun deleteFeed(id: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($id: ID!) { deleteFeed(id: $id) }",
        mapOf(variable("id", id)),
    )

    fun importFeeds(content: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($content: String!) { importFeeds(content: $content) }",
        mapOf(variable("content", content)),
    )

    fun exportFeeds(): GraphqlRequest = GraphqlRequest(
        $$"mutation { exportFeeds }",
    )

    fun saveMediaPlayProgress(mediaId: String, type: DataType, positionMs: Long): GraphqlRequest = GraphqlRequest(
        $$"mutation($mediaId: ID!, $type: MediaDataType!, $positionMs: Long!) { saveMediaPlayProgress(mediaId: $mediaId, type: $type, positionMs: $positionMs) }",
        mapOf(variable("mediaId", mediaId), variable("type", type.name), variable("positionMs", positionMs)),
    )

    fun recordMediaPlay(mediaId: String, type: DataType, positionMs: Long): GraphqlRequest = GraphqlRequest(
        $$"mutation($mediaId: ID!, $type: MediaDataType!, $positionMs: Long!) { recordMediaPlay(mediaId: $mediaId, type: $type, positionMs: $positionMs) }",
        mapOf(variable("mediaId", mediaId), variable("type", type.name), variable("positionMs", positionMs)),
    )

    fun createAudioPlaylist(name: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($name: String!) { createAudioPlaylist(name: $name) { $${Fragments.AUDIO_PLAYLIST_FIELDS} } }",
        mapOf(variable("name", name)),
    )

    fun updateAudioPlaylist(id: String, name: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($id: ID!, $name: String!) { updateAudioPlaylist(id: $id, name: $name) { id } }",
        mapOf(variable("id", id), variable("name", name)),
    )

    fun deleteAudioPlaylist(id: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($id: ID!) { deleteAudioPlaylist(id: $id) }",
        mapOf(variable("id", id)),
    )

    fun addAudioPlaylistItems(id: String, query: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($id: ID!, $query: String!) { addAudioPlaylistItems(id: $id, query: $query) }",
        mapOf(variable("id", id), variable("query", query)),
    )

    fun removeAudioPlaylistItem(id: String, query: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($id: ID!, $query: String!) { removeAudioPlaylistItem(id: $id, query: $query) { affectedCount } }",
        mapOf(variable("id", id), variable("query", query)),
    )

    fun advanceAudioSelection(action: String, expectedPath: String, expectedRevision: Long, shuffle: Boolean): GraphqlRequest = GraphqlRequest(
        $$"mutation($action: AudioSelectionAction!, $expectedPath: String!, $expectedRevision: Long!, $shuffle: Boolean!) { advanceAudioSelection(action: $action, expectedPath: $expectedPath, expectedRevision: $expectedRevision, shuffle: $shuffle) { track { $${Fragments.AUDIO_TRACK_FIELDS} } } }",
        mapOf(variable("action", action), variable("expectedPath", expectedPath), variable("expectedRevision", expectedRevision), variable("shuffle", shuffle)),
    )

    fun clearAudioQueue(): GraphqlRequest = GraphqlRequest(
        $$"mutation { clearAudioQueue }",
    )

    fun addAudioToQueue(query: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($query: String!) { addAudioToQueue(query: $query) }",
        mapOf(variable("query", query)),
    )

    fun removeAudioFromQueue(mediaId: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($mediaId: ID!) { removeAudioFromQueue(mediaId: $mediaId) }",
        mapOf(variable("mediaId", mediaId)),
    )

    fun reorderAudioQueue(mediaIds: Collection<String>): GraphqlRequest = GraphqlRequest(
        $$"mutation($mediaIds: [ID!]!) { reorderAudioQueue(mediaIds: $mediaIds) }",
        mapOf(variable("mediaIds", mediaIds)),
    )

    fun audioCommand(action: String, positionMs: Long, speed: Float): GraphqlRequest = GraphqlRequest(
        $$"mutation($action: AudioCommandAction!, $positionMs: Long!, $speed: Float!) { audioCommand(action: $action, positionMs: $positionMs, speed: $speed) { path positionMs revision } }",
        mapOf(variable("action", action), variable("positionMs", positionMs), variable("speed", speed)),
    )

    fun updateAudioPlayMode(mode: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($mode: MediaPlayMode!) { updateAudioPlayMode(mode: $mode) }",
        mapOf(variable("mode", mode)),
    )

    fun audioPlayTrack(mediaId: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($mediaId: ID!) { audioPlayTrack(mediaId: $mediaId) { path positionMs revision } }",
        mapOf(variable("mediaId", mediaId)),
    )

    fun audioPlayPath(path: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($path: String!) { audioPlayPath(path: $path) { path positionMs revision } }",
        mapOf(variable("path", path)),
    )

    fun renameSession(clientId: String, name: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($clientId: String!, $name: String!) { renameSession(clientId: $clientId, name: $name) }",
        mapOf(variable("clientId", clientId), variable("name", name)),
    )

    fun revokeSession(clientId: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($clientId: String!) { revokeSession(clientId: $clientId) }",
        mapOf(variable("clientId", clientId)),
    )

    fun saveFeedEntriesToNotes(query: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($query: String!) { saveFeedEntriesToNotes(query: $query) }",
        mapOf(variable("query", query)),
    )

    fun trashNotes(query: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($query: String!) { trashNotes(query: $query) { affectedCount } }",
        mapOf(variable("query", query)),
    )

    fun restoreNotes(query: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($query: String!) { restoreNotes(query: $query) { affectedCount } }",
        mapOf(variable("query", query)),
    )

    fun deleteNotes(query: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($query: String!) { deleteNotes(query: $query) { affectedCount } }",
        mapOf(variable("query", query)),
    )

    fun fetchBookmarkMetadata(id: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($id: ID!) { fetchBookmarkMetadata(id: $id) { $${Fragments.BOOKMARK} } }",
        mapOf(variable("id", id)),
    )

    fun startPomodoro(durationSec: Int): GraphqlRequest = GraphqlRequest(
        $$"mutation($durationSec: Int!) { startPomodoro(durationSec: $durationSec) }",
        mapOf(variable("durationSec", durationSec)),
    )

    fun pausePomodoro(): GraphqlRequest = GraphqlRequest(
        $$"mutation { pausePomodoro }",
    )

    fun stopPomodoro(): GraphqlRequest = GraphqlRequest(
        $$"mutation { stopPomodoro }",
    )

    fun adjustPomodoro(durationSec: Int): GraphqlRequest = GraphqlRequest(
        $$"mutation($durationSec: Int!) { adjustPomodoro(durationSec: $durationSec) { $${Fragments.POMODORO} } }",
        mapOf(variable("durationSec", durationSec)),
    )

    fun copyFile(src: String, dst: String, overwrite: Boolean): GraphqlRequest = GraphqlRequest(
        $$"mutation($src: String!, $dst: String!, $overwrite: Boolean!) { copyFile(src: $src, dst: $dst, overwrite: $overwrite) }",
        mapOf(variable("src", src), variable("dst", dst), variable("overwrite", overwrite)),
    )

    fun copyFileToDirectory(src: String, directory: String, name: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($src:String!,$directory:String!,$name:String!){copyFileToDirectory(src:$src,directory:$directory,name:$name)}",
        mapOf(variable("src", src), variable("directory", directory), variable("name", name)),
    )

    fun extractZipArchive(path: String, target: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($path: String!, $target: String!) { extractZipArchive(path: $path, target: $target) }",
        mapOf(variable("path", path), variable("target", target)),
    )

    fun cacheZipEntry(path: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($path: String!) { cacheZipEntry(path: $path) }",
        mapOf(variable("path", path)),
    )

    fun renameFile(path: String, name: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($path: String!, $name: String!) { renameFile(path: $path, name: $name) }",
        mapOf(variable("path", path), variable("name", name)),
    )

    fun deleteFiles(paths: Collection<String>): GraphqlRequest = GraphqlRequest(
        $$"mutation($paths: [String!]!) { deleteFiles(paths: $paths) { affectedCount } }",
        mapOf(variable("paths", paths)),
    )

    fun addFavoriteFolder(rootPath: String, fullPath: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($rootPath: String!, $fullPath: String!) { addFavoriteFolder(rootPath: $rootPath, fullPath: $fullPath) { $${Fragments.FAVORITE_FOLDER} } }",
        mapOf(variable("rootPath", rootPath), variable("fullPath", fullPath)),
    )

    fun removeFavoriteFolder(fullPath: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($fullPath: String!) { removeFavoriteFolder(fullPath: $fullPath) { $${Fragments.FAVORITE_FOLDER} } }",
        mapOf(variable("fullPath", fullPath)),
    )

    fun setFavoriteFolderAlias(fullPath: String, alias: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($fullPath: String!, $alias: String!) { setFavoriteFolderAlias(fullPath: $fullPath, alias: $alias) { $${Fragments.FAVORITE_FOLDER} } }",
        mapOf(variable("fullPath", fullPath), variable("alias", alias)),
    )

    fun cacheRemoteFile(url: String, name: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($url: String!, $name: String!) { cacheRemoteFile(url: $url, name: $name) }",
        mapOf(variable("url", url), variable("name", name)),
    )

    fun createFileTask(type: FileTaskType, ops: List<FileTaskOp>): GraphqlRequest = GraphqlRequest(
        $$"mutation($type:FileTaskType!,$title:String!,$ops:[FileTaskOpInput!]!){createFileTask(type:$type,title:$title,ops:$ops,waitForCompletion:true){ $${Fragments.FILE_TASK} }}",
        mapOf(variable("type", type.name), variable("title", type.name), variable("ops", ops)),
    )

    fun browseSharedLink(messageId: String, expected: MessageShare, virtualPath: String?): GraphqlRequest = GraphqlRequest(
        $$"mutation($messageId: ID!, $expected: SharedMessageCardInput!, $virtualPath: String) { browseSharedLink(messageId: $messageId, expected: $expected, virtualPath: $virtualPath) { info { $${Fragments.SHARED_INFO} } link { $${Fragments.SHARE_LINK} } card { $${Fragments.SHARED_CARD} } } }",
        mapOf(variable("messageId", messageId), variable("expected", expected), variable("virtualPath", virtualPath)),
    )

    fun cacheSharedFile(link: SharedLink, urlToken: String, entry: SharedFileDto, thumbnail: Boolean): GraphqlRequest = GraphqlRequest(
        $$"mutation($link: SharedLinkInput!, $urlToken: String!, $entry: SharedEntryInput!, $thumbnail: Boolean!) { cacheSharedFile(link: $link, urlToken: $urlToken, entry: $entry, thumbnail: $thumbnail) }",
        mapOf(variable("link", link), variable("urlToken", urlToken), variable("entry", entry), variable("thumbnail", thumbnail)),
    )

    fun deleteShare(id: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($id: ID!) { deleteShare(id: $id) }",
        mapOf(variable("id", id)),
    )

    internal fun enqueueSharedDownload(input: SharedDownloadInput): GraphqlRequest = GraphqlRequest(
        $$"mutation($input: SharedDownloadInput!) { enqueueSharedDownload(input: $input) }",
        mapOf(variable("input", input)),
    )

    fun cacheAudioCover(path: String): GraphqlRequest = GraphqlRequest(
        $$"mutation CacheAudioCover($path: String!) { cacheAudioCover(path: $path) }",
        mapOf(variable("path", path)),
    )

    fun deleteClipboardItems(query: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($query: String!) { deleteClipboardItems(query: $query) { affectedCount } }",
        mapOf(variable("query", query)),
    )

    fun mergeChunks(fileId: String, totalChunks: Long, path: String, totalSize: Long): GraphqlRequest = GraphqlRequest(
        $$"mutation($fileId: String!, $totalChunks: Int!, $path: String!, $totalSize: Long!) { mergeChunks(fileId: $fileId, totalChunks: $totalChunks, path: $path, replace: true, totalSize: $totalSize) { status error } }",
        mapOf(variable("fileId", fileId), variable("totalChunks", totalChunks), variable("path", path), variable("totalSize", totalSize)),
    )

    fun deleteChunks(fileId: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($fileId: String!) { deleteChunks(fileId: $fileId) }",
        mapOf(variable("fileId", fileId)),
    )

    fun importAppFile(source: String, fileName: String, mimeType: String, deleteSource: Boolean): GraphqlRequest = GraphqlRequest(
        $$"mutation($source: String!, $fileName: String!, $mimeType: String!, $deleteSource: Boolean!) { importAppFile(source: $source, fileName: $fileName, mimeType: $mimeType, deleteSource: $deleteSource) { $${Fragments.APP_FILE} } }",
        mapOf(variable("source", source), variable("fileName", fileName), variable("mimeType", mimeType), variable("deleteSource", deleteSource)),
    )

    fun releaseAppFile(fileId: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($fileId: String!) { releaseAppFile(fileId: $fileId) }",
        mapOf(variable("fileId", fileId)),
    )

    fun saveImageEditorProject(id: String, project: ImageEditorProject): GraphqlRequest {
        val input = ImageEditorProjectInput(project.stateB64, project.thumbnail, project.canvasWidth, project.canvasHeight, project.layerCount)
        return GraphqlRequest(
            $$"mutation($id: ID!, $input: ImageEditorProjectInput!) { saveImageEditorProject(id: $id, input: $input) { $${Fragments.IMAGE_EDITOR_PROJECT} } }",
            mapOf(variable("id", id), variable("input", input)),
        )
    }

    fun deleteImageEditorProject(id: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($id: ID!) { deleteImageEditorProject(id: $id) }",
        mapOf(variable("id", id)),
    )

    fun deletePeer(id: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($id: ID!) { deletePeer(id: $id) }",
        mapOf(variable("id", id)),
    )

    fun unpairPeer(id: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($id: ID!) { unpairPeer(id: $id) }",
        mapOf(variable("id", id)),
    )

    fun createChatChannel(name: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($name: String!) { createChatChannel(name: $name) { $${Fragments.CHANNEL_FIELDS} } }",
        mapOf(variable("name", name)),
    )

    fun updateChatChannel(id: String, name: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($id: ID!, $name: String!) { updateChatChannel(id: $id, name: $name) { $${Fragments.CHANNEL_FIELDS} } }",
        mapOf(variable("id", id), variable("name", name)),
    )

    fun deleteChatChannel(id: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($id: ID!) { deleteChatChannel(id: $id) }",
        mapOf(variable("id", id)),
    )

    fun leaveChatChannel(id: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($id: ID!) { leaveChatChannel(id: $id) }",
        mapOf(variable("id", id)),
    )

    fun addChatChannelMember(id: String, peerId: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($id: ID!, $peerId: ID!) { addChatChannelMember(id: $id, peerId: $peerId) { $${Fragments.CHANNEL_FIELDS} } }",
        mapOf(variable("id", id), variable("peerId", peerId)),
    )

    fun removeChatChannelMember(id: String, peerId: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($id: ID!, $peerId: ID!) { removeChatChannelMember(id: $id, peerId: $peerId) { $${Fragments.CHANNEL_FIELDS} } }",
        mapOf(variable("id", id), variable("peerId", peerId)),
    )

    fun acceptChatChannelInvite(id: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($id: ID!) { acceptChatChannelInvite(id: $id) }",
        mapOf(variable("id", id)),
    )

    fun declineChatChannelInvite(id: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($id: ID!) { declineChatChannelInvite(id: $id) }",
        mapOf(variable("id", id)),
    )

    fun clearAppLogs(): GraphqlRequest = GraphqlRequest(
        $$"mutation { clearAppLogs }",
    )

    fun exportAppLogs(): GraphqlRequest = GraphqlRequest(
        $$"mutation { exportAppLogs }",
    )

    fun clearAppCache(): GraphqlRequest = GraphqlRequest(
        $$"mutation { clearAppCache }",
    )

    fun deliverChatItem(id: String, recipients: Collection<String>?): GraphqlRequest = GraphqlRequest(
        $$"mutation($id: ID!, $recipients: [ID!]) { deliverChatItem(id: $id, recipients: $recipients) { $${Fragments.CHAT_ITEM} } }",
        mapOf(variable("id", id), variable("recipients", recipients)),
    )

    fun sendChatItems(targets: Collection<String>, content: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($targets: [String!]!, $content: String!) { sendChatItems(targets: $targets, content: $content) { $${Fragments.CHAT_ITEM} } }",
        mapOf(variable("targets", targets), variable("content", content)),
    )

    fun sendShare(targets: Collection<String>, shareId: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($targets: [String!]!, $shareId: ID!) { sendShare(targets: $targets, shareId: $shareId) { $${Fragments.CHAT_ITEM} } }",
        mapOf(variable("targets", targets), variable("shareId", shareId)),
    )

    fun sendFolderShare(targets: Collection<String>, path: String, name: String, expiresAt: Instant?): GraphqlRequest = GraphqlRequest(
        $$"mutation($targets: [String!]!, $path: String!, $name: String!, $expiresAt: Instant) { sendFolderShare(targets: $targets, path: $path, name: $name, expiresAt: $expiresAt) { $${Fragments.CHAT_ITEM} } }",
        mapOf(variable("targets", targets), variable("path", path), variable("name", name), variable("expiresAt", expiresAt)),
    )

    fun updateFeed(id: String, name: String?, url: String?, fetchContent: Boolean?): GraphqlRequest = GraphqlRequest(
        $$"mutation($id: ID!, $name: String, $url: String, $fetchContent: Boolean) { updateFeed(id: $id, name: $name, url: $url, fetchContent: $fetchContent) { $${Fragments.FEED_FIELDS} } }",
        mapOf(variable("id", id)) + listOfNotNull(
            name?.let { variable("name", it) },
            url?.let { variable("url", it) },
            fetchContent?.let { variable("fetchContent", it) },
        ),
    )

    fun syncFeeds(id: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($id: ID) { syncFeeds(id: $id) }",
        mapOf(variable("id", id.ifEmpty { null })),
    )

    fun saveNote(id: String, title: String, content: String): GraphqlRequest {
        val input = NoteInput(title, content)
        return if (id.isEmpty()) {
            GraphqlRequest(
                $$"mutation($input: NoteInput!) { createNote(input: $input) { $${Fragments.NOTE_FIELDS} } }",
                mapOf(variable("input", input)),
                field = "createNote",
            )
        } else {
            GraphqlRequest(
                $$"mutation($id: ID!, $input: NoteInput!) { updateNote(id: $id, input: $input) { $${Fragments.NOTE_FIELDS} } }",
                mapOf(variable("id", id), variable("input", input)),
                field = "updateNote",
            )
        }
    }

    fun createZipArchive(paths: Collection<String>, target: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($paths: [String!]!, $target: String!) { createZipArchive(paths: $paths, target: $target) }",
        mapOf(variable("paths", paths), variable("target", target)),
    )

    fun fileWrite(action: String, target: String, content: String, overwrite: Boolean): GraphqlRequest = when (action) {
        "createDirectory" -> GraphqlRequest(
            $$"mutation($path: String!) { createDir(path: $path) { $${Fragments.FILE} } }",
            mapOf(variable("path", target)),
            field = "createDir",
        )
        "createFile" -> GraphqlRequest(
            $$"mutation($path: String!) { createFile(path: $path) { $${Fragments.FILE} } }",
            mapOf(variable("path", target)),
            field = "createFile",
        )
        "writeText" -> GraphqlRequest(
            $$"mutation($path: String!, $content: String!, $overwrite: Boolean!) { writeTextFile(path: $path, content: $content, overwrite: $overwrite) { $${Fragments.FILE} } }",
            mapOf(variable("path", target), variable("content", content), variable("overwrite", overwrite)),
            field = "writeTextFile",
        )
        else -> error("Unknown file write action")
    }

    fun createShare(name: String, realPaths: Collection<String>, readOnly: Boolean, expiresAt: Instant?): GraphqlRequest = GraphqlRequest(
        $$"mutation($name: String!, $realPaths: [String!]!, $readOnly: Boolean!, $expiresAt: Instant) { createShare(name: $name, realPaths: $realPaths, readOnly: $readOnly, expiresAt: $expiresAt) { $${Fragments.SHARE} } }",
        mapOf(variable("name", name), variable("realPaths", realPaths), variable("readOnly", readOnly), variable("expiresAt", expiresAt)),
    )

    fun updateShare(id: String, name: String, expiresAt: Instant?, realPaths: Collection<String>?): GraphqlRequest = GraphqlRequest(
        $$"mutation($id: ID!, $name: String!, $expiresAt: Instant, $realPaths: [String!]) { updateShare(id: $id, name: $name, expiresAt: $expiresAt, realPaths: $realPaths) { $${Fragments.SHARE} } }",
        mapOf(variable("id", id), variable("name", name), variable("expiresAt", expiresAt), variable("realPaths", realPaths)),
    )

    fun sharedDownloadControl(command: String, id: String): GraphqlRequest {
        val field = when (command) {
            "retry" -> "retrySharedDownload"
            "cancel" -> "cancelSharedDownload"
            "remove" -> "removeSharedDownload"
            else -> error("Unknown shared download control")
        }
        return GraphqlRequest(
            $$"mutation($id: ID!) { $${field}(id: $id) }",
            mapOf(variable("id", id)),
            field = field,
        )
    }

    fun enqueueChatDownload(messageId: String, fileId: String, peerId: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($messageId: ID!, $fileId: String!, $peerId: ID!) { enqueueChatDownload(messageId: $messageId, fileId: $fileId, peerId: $peerId) }",
        mapOf(variable("messageId", messageId), variable("fileId", fileId), variable("peerId", peerId)),
    )

    fun chatDownloadControl(command: String, id: String): GraphqlRequest {
        val field = when (command) {
            "pause" -> "pauseChatDownload"
            "resume" -> "resumeChatDownload"
            "retry" -> "retryChatDownload"
            "remove" -> "removeChatDownload"
            else -> error("Unknown chat download control")
        }
        return GraphqlRequest(
            $$"mutation($id: ID!) { $${field}(id: $id) }",
            mapOf(variable("id", id)),
            field = field,
        )
    }

    fun setSavedValue(key: String, value: JsonElement): GraphqlRequest = GraphqlRequest(
        $$"mutation($key: String!, $value: JSON!) { setSavedValue(key: $key, value: $value) }",
        mapOf(variable("key", key), variable("value", value)),
    )

    fun syncFeedEntryContent(id: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($id: ID!) { syncFeedEntryContent(id: $id) { $${Fragments.ENTRY_FIELDS} } }",
        mapOf(variable("id", id)),
    )

    fun mediaAction(action: MediaAction, type: String, query: String, destDir: String): GraphqlRequest = when (action) {
        MediaAction.TRASH -> GraphqlRequest(
            $$"mutation($type: MediaDataType!, $query: String!) { trashMediaItems(type: $type, query: $query) { affectedCount } }",
            mapOf(variable("type", type), variable("query", query)),
            field = "trashMediaItems",
        )
        MediaAction.RESTORE -> GraphqlRequest(
            $$"mutation($type: MediaDataType!, $query: String!) { restoreMediaItems(type: $type, query: $query) { affectedCount } }",
            mapOf(variable("type", type), variable("query", query)),
            field = "restoreMediaItems",
        )
        MediaAction.DELETE -> GraphqlRequest(
            $$"mutation($type: MediaDataType!, $query: String!) { deleteMediaItems(type: $type, query: $query) { affectedCount } }",
            mapOf(variable("type", type), variable("query", query)),
            field = "deleteMediaItems",
        )
        MediaAction.MOVE -> GraphqlRequest(
            $$"mutation($type: MediaDataType!, $query: String!, $destDir: String!) { moveMediaItems(type: $type, query: $query, destDir: $destDir) { affectedCount } }",
            mapOf(variable("type", type), variable("query", query), variable("destDir", destDir)),
            field = "moveMediaItems",
        )
    }

    internal fun dlna(command: CastCommand): GraphqlRequest = when (command) {
        CastCommand.StartScan -> GraphqlRequest(
            $$"mutation { startDlnaScan { $${Fragments.DLNA_STATE} } }",
            field = "startDlnaScan",
        )
        CastCommand.StopScan -> GraphqlRequest(
            $$"mutation { stopDlnaScan { $${Fragments.DLNA_STATE} } }",
            field = "stopDlnaScan",
        )
        is CastCommand.Select -> GraphqlRequest(
            $$"mutation($id: ID!) { selectDlnaDevice(id: $id) { $${Fragments.DLNA_STATE} } }",
            mapOf(variable("id", command.id)),
            field = "selectDlnaDevice",
        )
        is CastCommand.Cast -> GraphqlRequest(
            $$"mutation($item: DlnaMediaInput!) { castDlnaMedia(item: $item) { $${Fragments.DLNA_STATE} } }",
            mapOf(variable("item", command.item)),
            field = "castDlnaMedia",
        )
        CastCommand.Play -> GraphqlRequest(
            $$"mutation { playDlna { $${Fragments.DLNA_STATE} } }",
            field = "playDlna",
        )
        CastCommand.Pause -> GraphqlRequest(
            $$"mutation { pauseDlna { $${Fragments.DLNA_STATE} } }",
            field = "pauseDlna",
        )
        is CastCommand.Seek -> GraphqlRequest(
            $$"mutation($positionMs: Long!) { seekDlna(positionMs: $positionMs) { $${Fragments.DLNA_STATE} } }",
            mapOf(variable("positionMs", command.positionMs)),
            field = "seekDlna",
        )
        CastCommand.Stop -> GraphqlRequest(
            $$"mutation { stopDlna { $${Fragments.DLNA_STATE} } }",
            field = "stopDlna",
        )
        CastCommand.Exit -> GraphqlRequest(
            $$"mutation { exitDlna { $${Fragments.DLNA_STATE} } }",
            field = "exitDlna",
        )
        is CastCommand.Add -> GraphqlRequest(
            $$"mutation($item: DlnaMediaInput!) { addDlnaItem(item: $item) { $${Fragments.DLNA_STATE} } }",
            mapOf(variable("item", command.item)),
            field = "addDlnaItem",
        )
        is CastCommand.Remove -> GraphqlRequest(
            $$"mutation($path: String!) { removeDlnaItem(path: $path) { $${Fragments.DLNA_STATE} } }",
            mapOf(variable("path", command.path)),
            field = "removeDlnaItem",
        )
        is CastCommand.RemoveAt -> GraphqlRequest(
            $$"mutation($index: Int!) { removeDlnaItemAt(index: $index) { $${Fragments.DLNA_STATE} } }",
            mapOf(variable("index", command.index)),
            field = "removeDlnaItemAt",
        )
        is CastCommand.Reorder -> GraphqlRequest(
            $$"mutation($from: Int!, $to: Int!) { reorderDlnaItems(from: $from, to: $to) { $${Fragments.DLNA_STATE} } }",
            mapOf(variable("from", command.from), variable("to", command.to)),
            field = "reorderDlnaItems",
        )
        CastCommand.Clear -> GraphqlRequest(
            $$"mutation { clearDlnaItems { $${Fragments.DLNA_STATE} } }",
            field = "clearDlnaItems",
        )
    }

    fun saveSharedText(directory: String, path: String, content: String): GraphqlRequest = GraphqlRequest(
        $$"mutation($directory: String!, $path: String!, $content: String!) { createDir(path: $directory) { path } writeTextFile(path: $path, content: $content, overwrite: true) { path } }",
        mapOf(variable("directory", directory), variable("path", path), variable("content", content)),
    )
}
