package com.ismartcoding.plain.ui.audio

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ismartcoding.plain.platform.system.AppFeatureType
import com.ismartcoding.plain.platform.system.has
import com.ismartcoding.plain.features.audio.AudioPlaylistsClient
import com.ismartcoding.plain.features.audio.AudioQueueClient
import com.ismartcoding.plain.api.graphql.models.AudioPlaylist
import com.ismartcoding.plain.api.graphql.models.TagHostRecord
import com.ismartcoding.plain.i18n.*
import com.ismartcoding.plain.lib.coIO
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.ui.navigation.PBackHandler
import com.ismartcoding.plain.platform.media.audioIsPlayingFlow
import com.ismartcoding.plain.platform.media.audioJustPlayWithNotificationCheck
import com.ismartcoding.plain.preferences.*
import com.ismartcoding.plain.ui.base.AnimatedBottomAction
import com.ismartcoding.plain.ui.components.NeedPermissionColumn
import com.ismartcoding.plain.ui.base.PSheetActionRow
import com.ismartcoding.plain.ui.base.VerticalSpace
import com.ismartcoding.plain.ui.base.dragselect.DragSelectState
import com.ismartcoding.plain.ui.base.pullrefresh.PullToRefresh
import com.ismartcoding.plain.ui.base.pullrefresh.RefreshContentState
import com.ismartcoding.plain.ui.base.pullrefresh.rememberRefreshLayoutState
import com.ismartcoding.plain.ui.base.pullrefresh.setRefreshState
import com.ismartcoding.plain.ui.media.MediaTopBar
import com.ismartcoding.plain.ui.audio.components.PlaylistNameDialog
import com.ismartcoding.plain.ui.audio.AudioHomeArtist
import com.ismartcoding.plain.ui.audio.AudioHomeViewModel
import com.ismartcoding.plain.ui.audio.AudioQueueViewModel
import com.ismartcoding.plain.ui.audio.AudioViewModel
import com.ismartcoding.plain.ui.casting.CastViewModel
import com.ismartcoding.plain.ui.files.MediaFoldersViewModel
import com.ismartcoding.plain.ui.tags.TagsViewModel
import com.ismartcoding.plain.ui.state.exitSearchMode
import com.ismartcoding.plain.ui.navigation.Routing
import com.ismartcoding.plain.ui.audio.components.ArtistsRow
import com.ismartcoding.plain.ui.audio.components.AudioFilesSelectModeBottomActions
import com.ismartcoding.plain.ui.audio.components.AudioListItem
import com.ismartcoding.plain.ui.audio.components.HomeQuickActions
import com.ismartcoding.plain.ui.audio.components.HomeSectionHeader
import com.ismartcoding.plain.ui.audio.components.PlaylistsRow
import com.ismartcoding.plain.ui.audio.components.ViewAudioBottomSheet
import com.ismartcoding.plain.ui.audio.player.components.AudioPlayerBar
import com.ismartcoding.plain.ui.casting.CastDialog
import com.ismartcoding.plain.ui.casting.CastQueueFab
import com.ismartcoding.plain.ui.tags.TagsBottomSheet
import com.ismartcoding.plain.ui.resources.Res as UiRes
import com.ismartcoding.plain.ui.resources.music as ui_drawable_music
import com.ismartcoding.plain.ui.resources.plus as ui_drawable_plus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

/**
 * Audio home: quick actions + artists / playlists / recent sections on top of
 * the shared media infrastructure ([MediaTopBar], drag select, cast). While
 * the top search bar is active or a sidebar folder/tag/trash filter is
 * selected it shows the same flat results list as [AudioAllPage].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioHomePage(
    navController: NavHostController,
    audioQueueVM: AudioQueueViewModel,
    audioVM: AudioViewModel,
    tagsVM: TagsViewModel,
    mediaFoldersVM: MediaFoldersViewModel,
    castVM: CastViewModel,
    homeVM: AudioHomeViewModel,
) {
    val scope = rememberCoroutineScope()
    val audioState = AudioPageState.create(audioVM, tagsVM, mediaFoldersVM)
    val scrollBehavior = audioState.scrollBehavior
    val dragSelectState = audioState.dragSelectState
    val itemsState = audioState.itemsState
    val scrollState = audioState.scrollState
    val tagsState = audioState.tagsState
    val tagsMapState = audioState.tagsMapState
    val isAudioPlaying by audioIsPlayingFlow().collectAsState()
    var showCreatePlaylist by remember { mutableStateOf(false) }

    // Floating player bar covers list content; measure it and pad the lists.
    val density = LocalDensity.current
    var playerBarClearance by remember { mutableStateOf(0.dp) }

    val topRefreshLayoutState = rememberRefreshLayoutState {
        scope.launch {
            audioVM.loadAsync(tagsVM)
            audioQueueVM.loadAsync()
            withIO { mediaFoldersVM.loadAsync() }
            homeVM.loadAsync(audioVM)
            setRefreshState(RefreshContentState.Finished)
        }
    }

    PBackHandler(enabled = dragSelectState.selectMode || castVM.castMode.value || audioVM.showSearchBar.value) {
        when {
            dragSelectState.selectMode -> dragSelectState.exitSelectMode()
            castVM.castMode.value -> castVM.exitCastMode()
            audioVM.showSearchBar.value && (!audioVM.searchActive.value || audioVM.queryText.value.isEmpty()) -> {
                audioVM.exitSearchMode()
                audioVM.showLoading.value = true
                scope.launch(Dispatchers.Default) { audioVM.loadAsync(tagsVM) }
            }
        }
    }

    AudioPageEffects(audioState, audioVM, audioQueueVM, tagsVM, mediaFoldersVM)

    val audioTagsMap = remember(tagsMapState, tagsState) {
        tagsMapState.mapValues { entry -> entry.value.mapNotNull { relation -> tagsState.find { it.id == relation.tagId } } }
    }

    ViewAudioBottomSheet(audioVM = audioVM, tagsVM = tagsVM, tagsMapState = tagsMapState, tagsState = tagsState, dragSelectState = dragSelectState, castVM = castVM)
    if (audioVM.showTagsDialog.value) {
        TagsBottomSheet(tagsVM) { audioVM.showTagsDialog.value = false }
    }
    CastDialog(castVM)

    // A sidebar folder/tag/trash filter shows the flat filtered list (same as
    // search mode / the all-items page) instead of the home sections, which
    // are not filter-aware. Cast Mode needs that list, so the menu only shows
    // while a filter is active.
    val sidebarFilterActive =
        audioVM.trash.value || audioVM.bucketId.value.isNotEmpty() || audioVM.tag.value != null

    LaunchedEffect(Unit) {
        homeVM.loadAsync(audioVM)
    }
    LaunchedEffect(itemsState) {
        homeVM.rebuild(itemsState)
    }

    MediaTopBar(
        navController = navController,
        mediaVM = audioVM,
        tagsVM = tagsVM,
        castVM = castVM,
        mediaFoldersVM = mediaFoldersVM,
        dragSelectState = dragSelectState,
        scrollBehavior = scrollBehavior,
        showCastModeMenu = sidebarFilterActive,
        bucketsMap = audioState.bucketsMap,
        itemsState = itemsState,
        scrollToTop = { scope.launch { scrollState.scrollToItem(0) } },
        onSortSelected = { sortBy ->
            scope.launch(Dispatchers.Default) {
                UserPrefs.audioSortBy.value = sortBy
                audioVM.sortBy.value = sortBy
                audioVM.loadAsync(tagsVM)
            }
        },
        onSearchAction = { tv ->
            scope.launch(Dispatchers.Default) {
                audioVM.loadAsync(tv)
            }
        },
        moreMenu = { dismiss ->
            PSheetActionRow(UiRes.drawable.ui_drawable_plus, stringResource(Res.string.new_playlist)) {
                dismiss()
                showCreatePlaylist = true
            }
        },
        bottomBar = {
            AnimatedBottomAction(visible = dragSelectState.showBottomActions()) {
                AudioFilesSelectModeBottomActions(audioVM, audioQueueVM, tagsVM, tagsState, dragSelectState)
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (!audioVM.hasPermission.value) {
                    NeedPermissionColumn(UiRes.drawable.ui_drawable_music, AppFeatureType.FILES.getPermission()!!); return@Column
                }

                if (audioVM.showSearchBar.value || sidebarFilterActive) {
                    // Search and filtered views mirror the all-items page;
                    // AudioPageList brings its own pull-to-refresh, so it sits
                    // outside ours.
                    AudioPageList(
                        scrollBehavior, dragSelectState, itemsState, audioVM, audioQueueVM,
                        tagsVM, castVM, audioTagsMap, isAudioPlaying, topRefreshLayoutState, paddingValues,
                        extraBottomPadding = playerBarClearance,
                    )
                } else {
                    PullToRefresh(refreshLayoutState = topRefreshLayoutState, userEnable = !dragSelectState.selectMode, modifier = Modifier.weight(1f)) {
                        HomeSections(
                            homeVM = homeVM,
                            audioVM = audioVM,
                            audioQueueVM = audioQueueVM,
                            tagsVM = tagsVM,
                            castVM = castVM,
                            audioTagsMap = audioTagsMap,
                            isAudioPlaying = isAudioPlaying,
                            scrollState = scrollState,
                            dragSelectState = dragSelectState,
                            playerBarClearance = playerBarClearance,
                            paddingValues = paddingValues,
                            onShuffleAll = {
                                scope.launch {
                                    coIO {
                                        AudioQueueClient.clearQueue()
                                        AudioQueueClient.add("trash:false")
                                        val start = AudioQueueClient.resolveNext(isNext = true, shuffle = true)
                                        if (start != null) {
                                            audioJustPlayWithNotificationCheck(start)
                                            audioQueueVM.onStarted(start)
                                        }
                                    }
                                }
                            },
                            onViewAllItems = { navController.navigate(Routing.AudioAll) },
                            onArtistsViewAll = { navController.navigate(Routing.Artists) },
                            onArtistClick = { artist -> navController.navigate(Routing.ArtistDetail(artist.name)) },
                            onPlaylistClick = { pl -> navController.navigate(Routing.PlaylistDetail(pl.id)) },
                        )
                    }
                }
            }
            AudioPlayerBar(
                audioQueueVM, castVM,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .onSizeChanged { playerBarClearance = with(density) { it.height.toDp() } },
                dragSelectState = dragSelectState,
            )
            CastQueueFab(castVM = castVM, modifier = Modifier.align(Alignment.BottomEnd), dragSelectState = dragSelectState)
        }
    }

    if (showCreatePlaylist) {
        PlaylistNameDialog(
            title = stringResource(Res.string.new_playlist),
            initial = "",
            confirmText = stringResource(Res.string.create),
            onConfirm = { name ->
                showCreatePlaylist = false
                scope.launch(Dispatchers.Default) {
                    AudioPlaylistsClient.createPlaylist(name)
                    homeVM.loadAsync(audioVM)
                }
            },
            onDismiss = { showCreatePlaylist = false },
        )
    }
}

/** The three home sections; pure composition, all state arrives via params. */
@Composable
private fun HomeSections(
    homeVM: AudioHomeViewModel,
    audioVM: AudioViewModel,
    audioQueueVM: AudioQueueViewModel,
    tagsVM: TagsViewModel,
    castVM: CastViewModel,
    audioTagsMap: Map<String, List<TagHostRecord>>,
    isAudioPlaying: Boolean,
    scrollState: LazyListState,
    dragSelectState: DragSelectState,
    playerBarClearance: Dp,
    paddingValues: PaddingValues,
    onShuffleAll: () -> Unit,
    onViewAllItems: () -> Unit,
    onArtistsViewAll: () -> Unit,
    onArtistClick: (AudioHomeArtist) -> Unit,
    onPlaylistClick: (AudioPlaylist) -> Unit,
) {
    val artists = homeVM.artists.value
    val playlists = homeVM.playlists.value
    val recent = homeVM.recentItems.value

    LazyColumn(modifier = Modifier.fillMaxSize(), state = scrollState) {
        item(key = "quick_actions") {
            HomeQuickActions(onShuffleAll = onShuffleAll, onViewAllItems = onViewAllItems)
        }
        if (artists.isNotEmpty()) {
            item(key = "artists_header") {
                HomeSectionHeader(stringResource(Res.string.artists), onArtistsViewAll)
            }
            item(key = "artists") {
                ArtistsRow(artists.take(10), onArtistClick)
            }
        }
        if (playlists.isNotEmpty()) {
            item(key = "playlists_header") {
                HomeSectionHeader(stringResource(Res.string.playlists), null)
            }
            item(key = "playlists") {
                PlaylistsRow(playlists, homeVM.playlistCovers.value, onPlaylistClick)
            }
        }
        item(key = "recent_header") {
            HomeSectionHeader(stringResource(Res.string.recent), null)
        }
        items(recent.size, key = { recent[it].path }) { index ->
            val item = recent[index]
            AudioListItem(
                item = item,
                audioVM = audioVM,
                audioQueueVM = audioQueueVM,
                tagsVM = tagsVM,
                castVM = castVM,
                tags = audioTagsMap[item.id] ?: emptyList(),
                dragSelectState = dragSelectState,
                isCurrentlyPlaying = isAudioPlaying && audioQueueVM.selectedMediaId.value == item.id,
                isInQueue = audioQueueVM.isInQueue(item.id),
            )
            VerticalSpace(dp = 8.dp)
        }
        item(key = "bottom") {
            VerticalSpace(dp = maxOf(playerBarClearance, 40.dp + paddingValues.calculateBottomPadding()))
        }
    }
}
