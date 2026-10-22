package com.ismartcoding.plain.ui.chat

import com.ismartcoding.plain.ui.permissions.getText
import com.ismartcoding.plain.ui.permissions.getGrantAccessText

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ismartcoding.plain.app.TempData
import com.ismartcoding.plain.ui.chat.ChatViewModel
import com.ismartcoding.plain.features.chat.channel.ChannelCacher
import com.ismartcoding.plain.models.ChatChannel
import com.ismartcoding.plain.models.Peer
import com.ismartcoding.plain.features.chat.models.getBestIp
import com.ismartcoding.plain.features.chat.models.getName
import com.ismartcoding.plain.ui.chat.peer.PeerCacher
import com.ismartcoding.plain.ui.components.ButtonSize
import com.ismartcoding.plain.ui.components.ButtonType
import com.ismartcoding.plain.platform.system.DeviceType
import com.ismartcoding.plain.ui.platform.system.getIcon
import com.ismartcoding.plain.ui.platform.system.getText
import com.ismartcoding.plain.i18n.Res
import com.ismartcoding.plain.i18n.add_member
import com.ismartcoding.plain.i18n.cancel
import com.ismartcoding.plain.i18n.channel_info
import com.ismartcoding.plain.i18n.channel_name
import com.ismartcoding.plain.i18n.clear_messages
import com.ismartcoding.plain.i18n.clear_messages_confirm
import com.ismartcoding.plain.i18n.close
import com.ismartcoding.plain.i18n.delete_channel
import com.ismartcoding.plain.i18n.delete_channel_warning
import com.ismartcoding.plain.i18n.device_type
import com.ismartcoding.plain.i18n.invite
import com.ismartcoding.plain.i18n.ip_address
import com.ismartcoding.plain.i18n.kick_member
import com.ismartcoding.plain.i18n.leave_channel
import com.ismartcoding.plain.i18n.leave_channel_warning
import com.ismartcoding.plain.i18n.members
import com.ismartcoding.plain.i18n.messages_cleared
import com.ismartcoding.plain.i18n.peer_id
import com.ismartcoding.plain.i18n.port
import com.ismartcoding.plain.i18n.resend_invite
import com.ismartcoding.plain.i18n.status
import com.ismartcoding.plain.lib.extensions.toSortName
import com.ismartcoding.plain.ui.base.BottomSpace
import com.ismartcoding.plain.ui.base.PCard
import com.ismartcoding.plain.ui.base.PFilledButton
import com.ismartcoding.plain.ui.base.PListItem
import com.ismartcoding.plain.ui.base.POutlinedButton
import com.ismartcoding.plain.ui.base.PScaffold
import com.ismartcoding.plain.ui.base.PTextButton
import com.ismartcoding.plain.ui.base.PTopAppBar
import com.ismartcoding.plain.ui.base.PSectionTitle
import com.ismartcoding.plain.ui.base.VerticalSpace
import com.ismartcoding.plain.ui.components.DialogHelper
import com.ismartcoding.plain.ui.chat.ChannelViewModel
import com.ismartcoding.plain.ui.chat.PeerViewModel
import com.ismartcoding.plain.ui.navigation.Routing
import com.ismartcoding.plain.ui.chat.components.ChannelMemberListItem
import com.ismartcoding.plain.ui.chat.components.PeerIconWithStatus
import com.ismartcoding.plain.ui.chat.components.PeerMember
import com.ismartcoding.plain.ui.chat.components.RenameChannelDialog
import com.ismartcoding.plain.ui.resources.close as ui_drawable_close
import com.ismartcoding.plain.ui.theme.PlainTheme
import com.ismartcoding.plain.ui.theme.dialogSheetBackground
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelInfoPage(
    navController: NavHostController, chatVM: ChatViewModel, peerVM: PeerViewModel, channelVM: ChannelViewModel,
) {
    val chatTarget = chatVM.target.collectAsState()
    // Collect channels as state so the UI recomposes whenever ChannelCacher
    // updates after Rust commits an invite, kick, or acceptance.
    val channels = ChannelCacher.channels.collectAsState()
    val liveChannel = channels.value.find { it.id == chatTarget.value.toId }
    val showRenameDialog = remember { mutableStateOf(false) }
    val selectedMemberPeer = remember { mutableStateOf<PeerMember?>(null) }

    val ownerPeerId: String? = remember(liveChannel?.ownerId) {
        if (liveChannel?.ownerId.isNullOrEmpty()) null
        else if (liveChannel.actions.isOwner) TempData.clientId
        else liveChannel.ownerId
    }
    val memberPeers: List<PeerMember> = remember(liveChannel?.members, ownerPeerId, com.ismartcoding.plain.ui.app.LocalDeviceState.peer.value) {
        liveChannel?.members?.mapNotNull { m ->
            val peer = (if (m.isMe()) mePeer() else PeerCacher.getPeer(m.peerId)) ?: return@mapNotNull null
            PeerMember(peer, m, isSelf = m.isMe(), isOwner = m.peerId == ownerPeerId)
        } ?: emptyList()
    }
    val inviteLabel = stringResource(Res.string.invite)

    val joinedMembers: List<PeerMember> = memberPeers.filter { it.isJoined() }
    val pendingMembers: List<PeerMember> = memberPeers.filter { it.isPending() }
    val displayMembers: List<PeerMember> = buildList {
        val ownerMember = joinedMembers.find { it.isOwner }
        if (ownerMember != null) {
            add(ownerMember)
            addAll(
                joinedMembers
                    .filter { !it.isOwner }
                    .sortedBy { it.displayName() },
            )
        } else {
            addAll(joinedMembers.sortedBy { it.displayName() })
        }
        addAll(pendingMembers.sortedBy { it.displayName() })
    }
    val addablePeers: List<Peer> = if (liveChannel?.actions?.canManageMembers == true) {
        val presentIds = liveChannel.members.map { it.peerId }.toMutableSet().apply {
            ownerPeerId?.let { add(it) }
        }
        PeerCacher.pairedPeers.collectAsState().value
            .filter { it.id !in presentIds }
            .sortedBy { it.getName().toSortName() }
    } else emptyList()

    PScaffold(topBar = {
        PTopAppBar(
            onNavigateBack = { navController.navigateUp() },
            title = stringResource(Res.string.channel_info)
        )
    }) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            if (liveChannel != null) {
                item { VerticalSpace(dp = 16.dp) }
                item {
                    PCard(modifier = Modifier.padding(horizontal = PlainTheme.PAGE_HORIZONTAL_MARGIN)) {
                        PListItem(
                            modifier = if (liveChannel.actions.canRename) Modifier.clickable { showRenameDialog.value = true } else Modifier,
                            title = stringResource(Res.string.channel_name),
                            value = liveChannel.name,
                            showMore = liveChannel.actions.canRename,
                        )
                    }
                }

                item { VerticalSpace(dp = 16.dp) }
                item { PSectionTitle(title = "${stringResource(Res.string.members)} (${displayMembers.size})") }
                item {
                    PCard(modifier = Modifier.padding(horizontal = PlainTheme.PAGE_HORIZONTAL_MARGIN)) {
                        displayMembers.forEach { pm ->
                            val canManage = liveChannel.actions.canManageMembers && !pm.isSelf && !pm.isOwner
                            val selfPendingInvite = pm.isSelf && pm.isPending()
                            val ownerPeer = if (selfPendingInvite) liveChannel.getOwner() else null
                            ChannelMemberListItem(
                                member = pm,
                                onClick = when {
                                    selfPendingInvite -> {
                                        {
                                            navController.navigate(
                                                Routing.ChannelInviteRequest(
                                                    channelId = liveChannel.id,
                                                    channelName = liveChannel.name,
                                                    ownerPeerId = liveChannel.ownerId,
                                                    ownerPeerName = ownerPeer?.getName() ?: "",
                                                )
                                            )
                                        }
                                    }
                                    !pm.isSelf -> { { selectedMemberPeer.value = pm } }
                                    else -> null
                                },
                                onRemove = if (canManage) {
                                    { channelVM.kickMember(liveChannel.id, pm.peer.id) }
                                } else null,
                                isLoading = pm.peer.id in channelVM.pendingIds,
                            )
                        }
                    }
                }

                if (liveChannel.actions.canManageMembers && addablePeers.isNotEmpty()) {
                    item { VerticalSpace(dp = 16.dp) }
                    item { PSectionTitle(title = stringResource(Res.string.add_member)) }
                    item {
                        PCard(modifier = Modifier.padding(horizontal = PlainTheme.PAGE_HORIZONTAL_MARGIN)) {
                            addablePeers.forEach { peer ->
                                PListItem(
                                    title = peer.getName(),
                                    subtitle = peer.getBestIp(),
                                    start = {
                                        PeerIconWithStatus(
                                            icon = peer.deviceType.getIcon(),
                                            title = peer.getName(),
                                            online = null
                                        )
                                    },
                                    action = {
                                        POutlinedButton(
                                            text = inviteLabel,
                                            onClick = { channelVM.inviteMember(liveChannel.id, peer.id) },
                                            buttonSize = ButtonSize.SMALL,
                                            isLoading = peer.id in channelVM.pendingIds,
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
            item { VerticalSpace(dp = 24.dp) }
            item {
                val clearMessagesText = stringResource(Res.string.clear_messages)
                val clearMessagesConfirmText = stringResource(Res.string.clear_messages_confirm)
                val cancelText = stringResource(Res.string.cancel)
                POutlinedButton(
                    text = clearMessagesText, type = ButtonType.DANGER, modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp), onClick = {
                        DialogHelper.showConfirmDialog(
                            title = clearMessagesText,
                            message = clearMessagesConfirmText,
                            confirmButton = Pair(clearMessagesText) { chatVM.clearAllMessages(); navController.navigateUp(); DialogHelper.showSuccess(Res.string.messages_cleared) },
                            dismissButton = Pair(cancelText) {})
                    })
            }
            if (liveChannel != null && liveChannel.actions.canDelete) {
                item {
                    val deleteChannelText = stringResource(Res.string.delete_channel);
                    val deleteChannelWarningText = stringResource(Res.string.delete_channel_warning);
                    val cancelText = stringResource(Res.string.cancel); VerticalSpace(dp = 16.dp)
                    POutlinedButton(
                        text = deleteChannelText,
                        type = ButtonType.DANGER,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        onClick = {
                            DialogHelper.showConfirmDialog(
                                title = deleteChannelText,
                                message = deleteChannelWarningText,
                                confirmButton = Pair(deleteChannelText) {
                                    channelVM.removeChannel(liveChannel.id)
                                    navController.popBackStack(navController.graph.startDestinationId, false)
                                },
                                dismissButton = Pair(cancelText) {})
                        })
                }
            }
            if (liveChannel != null && liveChannel.actions.canLeave) {
                item {
                    val leaveChannelText = stringResource(Res.string.leave_channel)
                    val leaveChannelWarningText = stringResource(Res.string.leave_channel_warning)
                    val cancelText = stringResource(Res.string.cancel); VerticalSpace(dp = 16.dp)
                    POutlinedButton(
                        text = leaveChannelText,
                        type = ButtonType.DANGER,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        onClick = {
                            DialogHelper.showConfirmDialog(
                                title = leaveChannelText,
                                message = leaveChannelWarningText,
                                confirmButton = Pair(leaveChannelText) {
                                    channelVM.leaveChannel(liveChannel.id)
                                    navController.navigateUp()
                                },
                                dismissButton = Pair(cancelText) {})
                        })
                }
            }
            item { BottomSpace(paddingValues) }
        }
    }

    if (showRenameDialog.value && liveChannel != null) {
        RenameChannelDialog(
            currentName = liveChannel.name,
            onDismiss = {
                showRenameDialog.value = false
            },
            onConfirm = { newName ->
                showRenameDialog.value = false
                channelVM.renameChannel(liveChannel.id, newName)
            },
        )
    }

    selectedMemberPeer.value?.let { sp ->
        MemberInfoDialog(
            peerMember = sp,
            canManageMembers = liveChannel?.actions?.canManageMembers == true,
            liveChannel = liveChannel,
            channelVM = channelVM,
            onDismiss = { selectedMemberPeer.value = null },
        )
    }
}

@Composable
private fun MemberInfoDialog(
    peerMember: PeerMember,
    canManageMembers: Boolean,
    liveChannel: ChatChannel?,
    channelVM: ChannelViewModel,
    onDismiss: () -> Unit,
) {
    val peer = peerMember.peer
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.dialogSheetBackground,
        onDismissRequest = onDismiss,
        confirmButton = {
            PTextButton(
                text = stringResource(Res.string.close),
                buttonSize = ButtonSize.MEDIUM,
                onClick = onDismiss,
            )
        },
        dismissButton = if (canManageMembers && !peerMember.isOwner && !peerMember.isSelf && liveChannel != null) {
            {
                if (peerMember.isPending()) {
                    PFilledButton(
                        text = stringResource(Res.string.resend_invite),
                        buttonSize = ButtonSize.MEDIUM,
                        onClick = {
                            channelVM.resendInvite(liveChannel.id, peer.id)
                            onDismiss()
                        },
                        type = ButtonType.TERTIARY,
                        isLoading = peer.id in channelVM.pendingIds,
                    )
                } else {
                    PFilledButton(
                        text = stringResource(Res.string.kick_member),
                        buttonSize = ButtonSize.MEDIUM,
                        onClick = {
                            channelVM.kickMember(liveChannel.id, peer.id)
                            onDismiss()
                        },
                        type = ButtonType.DANGER,
                        isLoading = peer.id in channelVM.pendingIds,
                    )
                }
            }
        } else null,
        title = { Text(text = peer.getName(), style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                PListItem(title = stringResource(Res.string.peer_id), value = peer.id)
                PListItem(title = stringResource(Res.string.ip_address), value = peer.getBestIp())
                PListItem(title = stringResource(Res.string.port), value = peer.port?.toString().orEmpty())
                PListItem(title = stringResource(Res.string.device_type), value = peer.deviceType.getText())
                val status = peer.status.getText()
                if (status.isNotEmpty()) {
                    PListItem(title = stringResource(Res.string.status), value = status)
                }
            }
        },
    )
}
