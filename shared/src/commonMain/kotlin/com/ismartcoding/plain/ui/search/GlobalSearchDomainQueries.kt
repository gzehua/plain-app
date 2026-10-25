package com.ismartcoding.plain.ui.search

import com.ismartcoding.plain.features.packages.PackagesClient
import com.ismartcoding.plain.features.media.MediaCatalogClient
import com.ismartcoding.plain.features.files.FilesClient

import com.ismartcoding.plain.features.files.extension

import com.ismartcoding.plain.models.File as FileEntry

import com.ismartcoding.plain.models.Audio as AudioTrack
import com.ismartcoding.plain.models.Doc as Document
import com.ismartcoding.plain.models.Video as MediaVideo
import com.ismartcoding.plain.models.Image as MediaImage
import com.ismartcoding.plain.features.chat.ChatQueries
import com.ismartcoding.plain.features.chat.channel.snapshotChannelsGraphql
import com.ismartcoding.plain.features.chat.models.getMessagePreview
import com.ismartcoding.plain.features.chat.peer.RustPeerStore
import com.ismartcoding.plain.platform.system.DataType
import com.ismartcoding.plain.ui.platform.system.getIcon
import com.ismartcoding.plain.features.notes.NotesClient
import com.ismartcoding.plain.features.feeds.FeedEntriesClient
import com.ismartcoding.plain.features.files.FileSortBy
import com.ismartcoding.plain.models.Audio
import com.ismartcoding.plain.models.ChatItem
import com.ismartcoding.plain.models.ChatChannel
import com.ismartcoding.plain.models.Doc
import com.ismartcoding.plain.models.FeedEntry
import com.ismartcoding.plain.models.File
import com.ismartcoding.plain.models.Image
import com.ismartcoding.plain.models.Note
import com.ismartcoding.plain.models.Peer
import com.ismartcoding.plain.models.Video
import com.ismartcoding.plain.helpers.SearchHelper
import com.ismartcoding.plain.i18n.*
import com.ismartcoding.plain.i18n.devices
import com.ismartcoding.plain.lib.extensions.formatBytes
import com.ismartcoding.plain.lib.extensions.formatDurationMs
import com.ismartcoding.plain.models.Package as AppPackage
import com.ismartcoding.plain.ui.i18n.LocaleHelper
import com.ismartcoding.plain.platform.system.formatDateTime
import com.ismartcoding.plain.ui.navigation.Routing
import com.ismartcoding.plain.ui.resources.Res as UiRes
import com.ismartcoding.plain.ui.resources.bot as ui_drawable_bot
import com.ismartcoding.plain.ui.resources.devices as ui_drawable_devices
import com.ismartcoding.plain.ui.resources.hash as ui_drawable_hash
import org.jetbrains.compose.resources.DrawableResource

/**
 * Data-source layer of the global search: how each domain is counted,
 * queried and re-synced, and how its source models map to result hits.
 * The [GlobalSearchViewModel] owns state and orchestration only.
 *
 * The box holds free text, and every domain below wants it as a `text:` value
 * rather than spliced into the query as-is — `SearchHelper.buildTextFilter` is
 * what keeps a term like `Meeting: notes` from arriving as the unknown field
 * `Meeting`, which the notes and media query layers refuse by name.
 */

internal suspend fun GlobalSearchViewModel.countDomain(d: GlobalSearchDomain, q: String): Int =
    when (d) {
        GlobalSearchDomain.NOTES -> NotesClient.count("${SearchHelper.buildTextFilter(q)} trash:false")
        GlobalSearchDomain.AUDIO -> MediaCatalogClient.count(DataType.AUDIO, "${SearchHelper.buildTextFilter(q)} trash:false")
        GlobalSearchDomain.IMAGES -> MediaCatalogClient.count(DataType.IMAGE, "${SearchHelper.buildTextFilter(q)} trash:false")
        GlobalSearchDomain.VIDEOS -> MediaCatalogClient.count(DataType.VIDEO, "${SearchHelper.buildTextFilter(q)} trash:false")
        GlobalSearchDomain.DOCS -> MediaCatalogClient.count(DataType.DOC, "${SearchHelper.buildTextFilter(q)} trash:false")
        GlobalSearchDomain.FILES -> FilesClient.indexedCount(SearchHelper.buildTextFilter(q))
        GlobalSearchDomain.FEEDS -> FeedEntriesClient.count(SearchHelper.buildTextFilter(q))
        GlobalSearchDomain.CHAT -> ChatQueries.countAsync(q)
        GlobalSearchDomain.APPS -> PackagesClient.count(q)
    }

internal suspend fun GlobalSearchViewModel.queryDomain(
    d: GlobalSearchDomain,
    q: String,
    limit: Int,
    offset: Int,
): List<GlobalSearchHit> =
    when (d) {
        GlobalSearchDomain.NOTES ->
            NotesClient.search("${SearchHelper.buildTextFilter(q)} trash:false", limit, offset).map { it.toHit(q) }
        GlobalSearchDomain.AUDIO ->
            MediaCatalogClient.rows(DataType.AUDIO, "${SearchHelper.buildTextFilter(q)} trash:false", limit, offset, FileSortBy.DATE_DESC)
                .filterIsInstance<AudioTrack>().map { it.toHit() }
        GlobalSearchDomain.IMAGES ->
            MediaCatalogClient.rows(DataType.IMAGE, "${SearchHelper.buildTextFilter(q)} trash:false", limit, offset, FileSortBy.DATE_DESC)
                .filterIsInstance<MediaImage>().map { it.toHit() }
        GlobalSearchDomain.VIDEOS ->
            MediaCatalogClient.rows(DataType.VIDEO, "${SearchHelper.buildTextFilter(q)} trash:false", limit, offset, FileSortBy.DATE_DESC)
                .filterIsInstance<MediaVideo>().map { it.toHit() }
        GlobalSearchDomain.DOCS ->
            MediaCatalogClient.rows(DataType.DOC, "${SearchHelper.buildTextFilter(q)} trash:false", limit, offset, FileSortBy.DATE_DESC)
                .filterIsInstance<Document>().map { it.toHit() }
        GlobalSearchDomain.FILES ->
            FilesClient.indexed(SearchHelper.buildTextFilter(q), limit, offset, FileSortBy.DATE_DESC).map { it.toHit() }
        GlobalSearchDomain.FEEDS ->
            FeedEntriesClient.search(SearchHelper.buildTextFilter(q), limit, offset).map { it.toHit(q) }
        GlobalSearchDomain.CHAT -> {
            val items = ChatQueries.searchAsync(q, limit, offset)
            val peers = if (items.isEmpty()) emptyMap() else RustPeerStore.getAll().associateBy { it.id }
            val channels = if (items.any { !it.channelId.isNullOrEmpty() }) {
                snapshotChannelsGraphql().associateBy { it.id }
            } else emptyMap()
            items.map { it.toHit(q, peers, channels) }
        }
        GlobalSearchDomain.APPS ->
            PackagesClient.search(q, limit, offset, FileSortBy.DATE_DESC).filter { it.name.isNotBlank() }.map { it.toHit() }
    }

/**
 * Re-fetches the currently loaded hits of an editable domain by their source
 * ids; entries that no longer exist fall out. Returns null for domains
 * without an editable source (files, apps). Chat results are re-queried in
 * one batch so their peer names and icons also stay current.
 */
internal suspend fun GlobalSearchViewModel.reloadDomainHits(
    d: GlobalSearchDomain,
    hits: List<GlobalSearchHit>,
    q: String,
): List<GlobalSearchHit>? =
    when (d) {
        GlobalSearchDomain.NOTES -> hits.mapNotNull { hit ->
            val src = hit.source as? GlobalSearchSource.Note ?: return@mapNotNull hit
            NotesClient.getById(src.note.id)?.toHit(q)
        }
        GlobalSearchDomain.FEEDS -> hits.mapNotNull { hit ->
            val src = hit.source as? GlobalSearchSource.Feed ?: return@mapNotNull hit
            FeedEntriesClient.getAsync(src.entry.id)?.toHit(q)
        }
        GlobalSearchDomain.AUDIO -> reloadMediaHits<AudioTrack>(hits, DataType.AUDIO) { it.toHit() }
        GlobalSearchDomain.IMAGES -> reloadMediaHits<MediaImage>(hits, DataType.IMAGE) { it.toHit() }
        GlobalSearchDomain.VIDEOS -> reloadMediaHits<MediaVideo>(hits, DataType.VIDEO) { it.toHit() }
        GlobalSearchDomain.DOCS -> reloadMediaHits<Document>(hits, DataType.DOC) { it.toHit() }
        GlobalSearchDomain.CHAT -> queryDomain(d, q, hits.size, 0)
        else -> null
    }

private suspend inline fun <reified T : Any> reloadMediaHits(
    hits: List<GlobalSearchHit>,
    dataType: DataType,
    crossinline toHit: (T) -> GlobalSearchHit,
): List<GlobalSearchHit>? {
    val ids = hits.mapNotNull { hit ->
        when (val src = hit.source) {
            is GlobalSearchSource.Audio -> if (dataType == DataType.AUDIO) src.audio.id else null
            is GlobalSearchSource.Image -> if (dataType == DataType.IMAGE) src.image.id else null
            is GlobalSearchSource.Video -> if (dataType == DataType.VIDEO) src.video.id else null
            is GlobalSearchSource.Doc -> if (dataType == DataType.DOC) src.doc.id else null
            else -> null
        }
    }
    if (ids.isEmpty()) return null
    return MediaCatalogClient.rows(dataType, "ids:${ids.joinToString(",")} trash:false", ids.size, 0, FileSortBy.DATE_DESC)
        .filterIsInstance<T>()
        .map { toHit(it) }
}

private fun Note.toHit(q: String): GlobalSearchHit = GlobalSearchHit(
    key = "note_$id",
    title = title,
    snippet = content.snippetAround(q),
    source = GlobalSearchSource.Note(this),
    action = GlobalSearchAction.Navigate(Routing.NoteDetail(id)),
)

private fun AudioTrack.toHit(): GlobalSearchHit = GlobalSearchHit(
    key = "audio_$id",
    title = title,
    subtitle = artist,
    source = GlobalSearchSource.Audio(this),
    action = GlobalSearchAction.PlayAudio(this),
)

private fun MediaImage.toHit(): GlobalSearchHit = GlobalSearchHit(
    key = "image_$id",
    title = title,
    subtitle = (takenAt ?: createdAt).formatDateTime(),
    source = GlobalSearchSource.Image(this),
    action = GlobalSearchAction.Navigate(Routing.Images),
)

private fun MediaVideo.toHit(): GlobalSearchHit = GlobalSearchHit(
    key = "video_$id",
    title = title,
    subtitle = durationMs.formatDurationMs() + " · " + (takenAt ?: createdAt).formatDateTime(),
    source = GlobalSearchSource.Video(this),
    action = GlobalSearchAction.Navigate(Routing.Videos),
)

private fun Document.toHit(): GlobalSearchHit = GlobalSearchHit(
    key = "doc_$id",
    title = title,
    subtitle = size.formatBytes(),
    source = GlobalSearchSource.Doc(this),
    action = GlobalSearchAction.Navigate(Routing.Docs),
)

private fun FileEntry.toHit(): GlobalSearchHit = GlobalSearchHit(
    key = "file_$path",
    title = name,
    subtitle = size.formatBytes(),
    source = GlobalSearchSource.File(this),
    action = GlobalSearchAction.Navigate(Routing.Files(path.substringBeforeLast('/'))),
)

private fun FeedEntry.toHit(q: String): GlobalSearchHit = GlobalSearchHit(
    key = "feed_$id",
    title = title,
    snippet = description.snippetAround(q),
    source = GlobalSearchSource.Feed(this),
    action = GlobalSearchAction.Navigate(Routing.FeedEntry(id)),
)

private fun ChatItem.toHit(q: String, peers: Map<String, Peer>, channels: Map<String, ChatChannel>): GlobalSearchHit {
    val channelId = channelId.orEmpty()
    val isChannel = channelId.isNotEmpty()
    val conversation: String
    val chatRoute: String
    val senderName: String
    if (isChannel) {
        conversation = channels[channelId]?.name ?: channelId
        chatRoute = "channel:$channelId"
        senderName = if (fromId == "me") LocaleHelper.getString(Res.string.me)
        else peers[fromId]?.name ?: fromId
    } else {
        val other = if (fromId == "me" || fromId == "local") toId else fromId
        conversation = if (other == "local") LocaleHelper.getString(Res.string.local_chat)
        else peers[other]?.name ?: other
        chatRoute = "peer:$other"
        senderName = if (fromId == "me") LocaleHelper.getString(Res.string.me) else conversation
    }
    val thumbRes: DrawableResource = if (isChannel) {
        UiRes.drawable.ui_drawable_hash
    } else {
        val other = if (fromId == "me" || fromId == "local") toId else fromId
        if (other == "local") UiRes.drawable.ui_drawable_bot
        else peers[other]?.deviceType?.getIcon() ?: UiRes.drawable.ui_drawable_devices
    }
    return GlobalSearchHit(
        key = "chat_$id",
        title = senderName,
        snippet = getMessagePreview().snippetAround(q),
        subtitle = conversation,
        iconRes = thumbRes,
        roundThumb = true,
        action = GlobalSearchAction.Navigate(Routing.Chat(chatRoute)),
    )
}

private fun AppPackage.toHit(): GlobalSearchHit = GlobalSearchHit(
    key = "app_$id",
    title = name,
    subtitle = id,
    source = GlobalSearchSource.App(this),
    action = GlobalSearchAction.Navigate(Routing.AppDetails(id)),
)
