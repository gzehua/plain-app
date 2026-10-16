package com.ismartcoding.plain.ui.models

import com.ismartcoding.plain.features.file.extension

import com.ismartcoding.plain.graphql.models.File as FileEntry

import com.ismartcoding.plain.graphql.models.Audio as AudioTrack
import com.ismartcoding.plain.graphql.models.Doc as Document
import com.ismartcoding.plain.graphql.models.Video as MediaVideo
import com.ismartcoding.plain.graphql.models.Image as MediaImage
import com.ismartcoding.plain.chat.ChatDbHelper
import com.ismartcoding.plain.chat.channel.ChannelCacher
import com.ismartcoding.plain.chat.models.getMessagePreview
import com.ismartcoding.plain.chat.peer.PeerCacher
import com.ismartcoding.plain.enums.DataType
import com.ismartcoding.plain.enums.getIcon
import com.ismartcoding.plain.features.NoteHelper
import com.ismartcoding.plain.features.feed.FeedEntryHelper
import com.ismartcoding.plain.features.file.FileSortBy
import com.ismartcoding.plain.graphql.models.Audio
import com.ismartcoding.plain.graphql.models.ChatItem
import com.ismartcoding.plain.graphql.models.Doc
import com.ismartcoding.plain.graphql.models.FeedEntry
import com.ismartcoding.plain.graphql.models.File
import com.ismartcoding.plain.graphql.models.Image
import com.ismartcoding.plain.graphql.models.Note
import com.ismartcoding.plain.graphql.models.Video
import com.ismartcoding.plain.helpers.SearchHelper
import com.ismartcoding.plain.i18n.*
import com.ismartcoding.plain.i18n.devices
import com.ismartcoding.plain.lib.extensions.formatBytes
import com.ismartcoding.plain.lib.extensions.formatDurationMs
import com.ismartcoding.plain.graphql.models.Package as AppPackage
import com.ismartcoding.plain.platform.LocaleHelper
import com.ismartcoding.plain.platform.countFiles
import com.ismartcoding.plain.platform.countMedia
import com.ismartcoding.plain.platform.countPackages
import com.ismartcoding.plain.platform.formatDateTime
import com.ismartcoding.plain.platform.searchFiles
import com.ismartcoding.plain.platform.searchMedia
import com.ismartcoding.plain.platform.searchPackages
import com.ismartcoding.plain.ui.nav.Routing
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
        GlobalSearchDomain.NOTES -> NoteHelper.count("${SearchHelper.buildTextFilter(q)} trash:false")
        GlobalSearchDomain.AUDIO -> countMedia(DataType.AUDIO, "${SearchHelper.buildTextFilter(q)} trash:false")
        GlobalSearchDomain.IMAGES -> countMedia(DataType.IMAGE, "${SearchHelper.buildTextFilter(q)} trash:false")
        GlobalSearchDomain.VIDEOS -> countMedia(DataType.VIDEO, "${SearchHelper.buildTextFilter(q)} trash:false")
        GlobalSearchDomain.DOCS -> countMedia(DataType.DOC, "${SearchHelper.buildTextFilter(q)} trash:false")
        GlobalSearchDomain.FILES -> countFiles(SearchHelper.buildTextFilter(q))
        GlobalSearchDomain.FEEDS -> FeedEntryHelper.count(SearchHelper.buildTextFilter(q))
        GlobalSearchDomain.CHAT -> ChatDbHelper.countAsync(q)
        GlobalSearchDomain.APPS -> countPackages(q)
    }

internal suspend fun GlobalSearchViewModel.queryDomain(
    d: GlobalSearchDomain,
    q: String,
    limit: Int,
    offset: Int,
): List<GlobalSearchHit> =
    when (d) {
        GlobalSearchDomain.NOTES ->
            NoteHelper.search("${SearchHelper.buildTextFilter(q)} trash:false", limit, offset).map { it.toHit(q) }
        GlobalSearchDomain.AUDIO ->
            searchMedia(DataType.AUDIO, "${SearchHelper.buildTextFilter(q)} trash:false", limit, offset, FileSortBy.DATE_DESC)
                .filterIsInstance<AudioTrack>().map { it.toHit() }
        GlobalSearchDomain.IMAGES ->
            searchMedia(DataType.IMAGE, "${SearchHelper.buildTextFilter(q)} trash:false", limit, offset, FileSortBy.DATE_DESC)
                .filterIsInstance<MediaImage>().map { it.toHit() }
        GlobalSearchDomain.VIDEOS ->
            searchMedia(DataType.VIDEO, "${SearchHelper.buildTextFilter(q)} trash:false", limit, offset, FileSortBy.DATE_DESC)
                .filterIsInstance<MediaVideo>().map { it.toHit() }
        GlobalSearchDomain.DOCS ->
            searchMedia(DataType.DOC, "${SearchHelper.buildTextFilter(q)} trash:false", limit, offset, FileSortBy.DATE_DESC)
                .filterIsInstance<Document>().map { it.toHit() }
        GlobalSearchDomain.FILES ->
            searchFiles(SearchHelper.buildTextFilter(q), limit, offset, FileSortBy.DATE_DESC).map { it.toHit() }
        GlobalSearchDomain.FEEDS ->
            FeedEntryHelper.search(SearchHelper.buildTextFilter(q), limit, offset).map { it.toHit(q) }
        GlobalSearchDomain.CHAT ->
            ChatDbHelper.searchAsync(q, limit, offset).map { it.toHit(q) }
        GlobalSearchDomain.APPS ->
            searchPackages(q, limit, offset, FileSortBy.DATE_DESC).filter { it.name.isNotBlank() }.map { it.toHit() }
    }

/**
 * Re-fetches the currently loaded hits of an editable domain by their source
 * ids; entries that no longer exist fall out. Returns null for domains
 * without an editable source (files, chat, apps).
 */
internal suspend fun GlobalSearchViewModel.reloadDomainHits(
    d: GlobalSearchDomain,
    hits: List<GlobalSearchHit>,
    q: String,
): List<GlobalSearchHit>? =
    when (d) {
        GlobalSearchDomain.NOTES -> hits.mapNotNull { hit ->
            val src = hit.source as? GlobalSearchSource.Note ?: return@mapNotNull hit
            NoteHelper.getById(src.note.id)?.toHit(q)
        }
        GlobalSearchDomain.FEEDS -> hits.mapNotNull { hit ->
            val src = hit.source as? GlobalSearchSource.Feed ?: return@mapNotNull hit
            FeedEntryHelper.getAsync(src.entry.id)?.toHit(q)
        }
        GlobalSearchDomain.AUDIO -> reloadMediaHits<AudioTrack>(hits, DataType.AUDIO) { it.toHit() }
        GlobalSearchDomain.IMAGES -> reloadMediaHits<MediaImage>(hits, DataType.IMAGE) { it.toHit() }
        GlobalSearchDomain.VIDEOS -> reloadMediaHits<MediaVideo>(hits, DataType.VIDEO) { it.toHit() }
        GlobalSearchDomain.DOCS -> reloadMediaHits<Document>(hits, DataType.DOC) { it.toHit() }
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
    return searchMedia(dataType, "ids:${ids.joinToString(",")} trash:false", ids.size, 0, FileSortBy.DATE_DESC)
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

private fun ChatItem.toHit(q: String): GlobalSearchHit {
    val channelId = channelId.orEmpty()
    val isChannel = channelId.isNotEmpty()
    val conversation: String
    val chatRoute: String
    val senderName: String
    if (isChannel) {
        conversation = ChannelCacher.getChannel(channelId)?.name ?: channelId
        chatRoute = "channel:$channelId"
        senderName = if (fromId == "me") LocaleHelper.getString(Res.string.me)
        else PeerCacher.getPeer(fromId)?.name ?: fromId
    } else {
        val other = if (fromId == "me" || fromId == "local") toId else fromId
        conversation = if (other == "local") LocaleHelper.getString(Res.string.local_chat)
        else PeerCacher.getPeer(other)?.name ?: other
        chatRoute = "peer:$other"
        senderName = if (fromId == "me") LocaleHelper.getString(Res.string.me) else conversation
    }
    val thumbRes: DrawableResource = if (isChannel) {
        UiRes.drawable.ui_drawable_hash
    } else {
        val other = if (fromId == "me" || fromId == "local") toId else fromId
        if (other == "local") UiRes.drawable.ui_drawable_bot
        else PeerCacher.getPeer(other)?.deviceType?.getIcon() ?: UiRes.drawable.ui_drawable_devices
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
