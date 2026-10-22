package com.ismartcoding.plain.ui.shares

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ismartcoding.plain.features.chat.ShareSendClient
import com.ismartcoding.plain.ui.components.ButtonType
import com.ismartcoding.plain.features.shares.ShareExpiry
import com.ismartcoding.plain.features.shares.SharesClient
import com.ismartcoding.plain.models.ShareRecord
import com.ismartcoding.plain.i18n.Res
import com.ismartcoding.plain.i18n.confirm_to_delete
import com.ismartcoding.plain.i18n.delete
import com.ismartcoding.plain.i18n.edit_share_link
import com.ismartcoding.plain.i18n.folder
import com.ismartcoding.plain.i18n.name
import com.ismartcoding.plain.i18n.save
import com.ismartcoding.plain.i18n.sent
import com.ismartcoding.plain.i18n.share_expired
import com.ismartcoding.plain.i18n.share_expires_on
import com.ismartcoding.plain.i18n.share_expiry
import com.ismartcoding.plain.i18n.share_expiry_never
import com.ismartcoding.plain.i18n.share_link
import com.ismartcoding.plain.i18n.share_link_desc
import com.ismartcoding.plain.i18n.share_name_placeholder
import com.ismartcoding.plain.lib.TimeHelper
import com.ismartcoding.plain.lib.extensions.toBreakableUrl
import com.ismartcoding.plain.lib.withIO
import com.ismartcoding.plain.platform.system.formatDateTime
import com.ismartcoding.plain.platform.system.shareText
import com.ismartcoding.plain.ui.components.ActionButtons
import com.ismartcoding.plain.ui.base.BottomSpace
import com.ismartcoding.plain.ui.components.ClipboardTextField
import com.ismartcoding.plain.ui.components.CornerCopyCard
import com.ismartcoding.plain.ui.components.IconTextForwardButton
import com.ismartcoding.plain.ui.components.IconTextQrCodeButton
import com.ismartcoding.plain.ui.components.IconTextShareButton
import com.ismartcoding.plain.ui.base.PFilledButton
import com.ismartcoding.plain.ui.base.PFilterChip
import com.ismartcoding.plain.ui.base.POutlinedButton
import com.ismartcoding.plain.ui.base.PScaffold
import com.ismartcoding.plain.ui.base.PTextButton
import com.ismartcoding.plain.ui.base.PTopAppBar
import com.ismartcoding.plain.ui.base.TopSpace
import com.ismartcoding.plain.ui.base.VerticalSpace
import com.ismartcoding.plain.ui.web.components.WebAddressBarQrDialog
import com.ismartcoding.plain.ui.components.DialogHelper
import com.ismartcoding.plain.ui.components.confirmActionAsync
import com.ismartcoding.plain.ui.chat.components.ForwardTargetDialog
import com.ismartcoding.plain.ui.files.label
import com.ismartcoding.plain.ui.resources.Res as UiRes
import com.ismartcoding.plain.ui.resources.save as ui_drawable_save
import kotlin.math.abs
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Page to edit an existing share link: rename it, change the expiry, and
 * access the link itself (copy, QR code, system share, forward to a chat).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditSharePage(
    navController: NavHostController,
    shareId: String,
) {
    val scope = rememberCoroutineScope()
    var share by remember { mutableStateOf<ShareRecord?>(null) }
    var link by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf(ShareExpiry.NEVER) }
    var paths by remember { mutableStateOf(listOf<String>()) }
    var isLoading by remember { mutableStateOf(false) }
    var showQr by remember { mutableStateOf(false) }
    var showForwardDialog by remember { mutableStateOf(false) }
    var showItemsPicker by remember { mutableStateOf(false) }
    val dirFlags = remember { mutableStateMapOf<String, Boolean>() }

    LaunchedEffect(shareId) {
        val s = withIO { SharesClient.getShare(shareId) } ?: return@LaunchedEffect
        share = s
        name = s.name
        expiry = s.toExpiryOption()
        paths = s.roots.map { it.realPath }
        dirFlags.clear()
        dirFlags.putAll(s.roots.associate { it.realPath to it.isDir })
        link = SharesClient.buildLink(s)
    }


    val current = share
    if (current == null) {
        // Unknown share id: nothing to edit.
        PScaffold(
            topBar = { PTopAppBar(onNavigateBack = { navController.navigateUp() }, title = stringResource(Res.string.edit_share_link)) },
        ) { }
        return
    }

    val save: () -> Unit = {
        scope.launch {
            isLoading = true
            val updated = withIO {
                SharesClient.updateShare(current.id, name, expiry.expiresAt(TimeHelper.now()), realPaths = paths)
            }
            isLoading = false
            if (updated != null) navController.popBackStack()
        }
    }

    PScaffold(
        topBar = {
            PTopAppBar(
                onNavigateBack = { navController.navigateUp() },
                title = stringResource(Res.string.edit_share_link),
                actions = {
                    PTextButton(text = stringResource(Res.string.save), onClick = save)
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            TopSpace()
            Text(
                text = stringResource(Res.string.share_link_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            VerticalSpace(8.dp)
            ClipboardTextField(
                value = name,
                label = stringResource(Res.string.name),
                placeholder = stringResource(Res.string.share_name_placeholder),
                onValueChange = { name = it },
            )
            VerticalSpace(16.dp)
            SharePathsEditor(paths, dirFlags, onRemove = { paths = paths - it }, onAdd = { showItemsPicker = true })
            VerticalSpace(16.dp)
            Text(stringResource(Res.string.share_expiry), style = MaterialTheme.typography.titleSmall)
            VerticalSpace(8.dp)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ShareExpiry.entries.forEach { e ->
                    PFilterChip(
                        selected = expiry == e,
                        onClick = { expiry = e },
                        label = { Text(stringResource(e.label)) },
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
            }
            VerticalSpace(8.dp)
            CornerCopyCard(label = stringResource(Res.string.share_link), text = link.toBreakableUrl())
            VerticalSpace(16.dp)
            ActionButtons {
                IconTextQrCodeButton { showQr = true }
                IconTextShareButton { shareText(link) }
                IconTextForwardButton { showForwardDialog = true }
            }
            VerticalSpace(16.dp)
            POutlinedButton(
                text = stringResource(Res.string.delete),
                type = ButtonType.DANGER,
                onClick = {
                    scope.launch {
                        confirmActionAsync(
                            Res.string.delete,
                            Res.string.confirm_to_delete,
                            callback = {
                                scope.launch {
                                    withIO { SharesClient.deleteShare(current.id) }
                                    navController.popBackStack()
                                }
                            },
                            danger = true
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            BottomSpace(paddingValues)
        }
    }

    if (showQr) {
        WebAddressBarQrDialog(url = link, onClose = { showQr = false })
    }

    if (showItemsPicker) {
        ShareItemsPickerSheet(
            initialSelected = paths.associateWith { dirFlags[it] == true },
            onDismiss = { showItemsPicker = false },
            onConfirm = { picked ->
                dirFlags.putAll(picked)
                paths = (paths + picked.keys).distinct()
                showItemsPicker = false
            },
        )
    }

    if (showForwardDialog) {
        ForwardTargetDialog(
            onDismiss = { showForwardDialog = false },
            onTargetsSelected = { targets ->
                showForwardDialog = false
                scope.launch {
                    ShareSendClient.sendShare(targets, current.id)
                    DialogHelper.showSuccess(Res.string.sent)
                }
            },
        )
    }
}

/** Human-readable expiry status of a share, e.g. "Expired" / "Expires …" / "Never". */
@Composable
fun ShareRecord.expiryLabel(): String {
    val expiresAt = this.expiresAt
    return when {
        isExpired -> stringResource(Res.string.share_expired)
        expiresAt != null -> stringResource(Res.string.share_expires_on, expiresAt.formatDateTime())
        else -> stringResource(Res.string.share_expiry_never)
    }
}

/** Pick the expiry chip closest to the share's remaining lifetime (null = NEVER). */
private fun ShareRecord.toExpiryOption(): ShareExpiry {
    val expiresAt = expiresAt ?: return ShareExpiry.NEVER
    val remainingHours = (expiresAt - TimeHelper.now()).inWholeHours
    return ShareExpiry.entries.filter { it != ShareExpiry.NEVER }
        .minByOrNull { abs(it.hours - remainingHours) }
        ?: ShareExpiry.NEVER
}
