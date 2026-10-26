package com.ismartcoding.plain.ui.settings

import com.ismartcoding.plain.features.settings.AppUpdateDownloads

import com.ismartcoding.plain.ui.media.extensions.collectAsStateValue

import com.ismartcoding.plain.platform.system.AppFeatureType
import com.ismartcoding.plain.platform.system.has
import com.ismartcoding.plain.i18n.*
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.ismartcoding.plain.ui.base.PTextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import com.ismartcoding.plain.Prefs
import com.ismartcoding.plain.lib.extensions.formatBytes
import com.ismartcoding.plain.ui.components.ButtonSize
import com.ismartcoding.plain.platform.system.formatDateTime
import com.ismartcoding.plain.ui.base.PFilledButton
import com.ismartcoding.plain.ui.base.VerticalSpace
import com.ismartcoding.plain.ui.theme.dialogSheetBackground
import kotlinx.coroutines.launch
import kotlin.time.Instant
import com.ismartcoding.plain.ui.resources.Res as UiRes
import com.ismartcoding.plain.ui.resources.rocket as ui_drawable_rocket

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateDialog(updateVM: UpdateViewModel) {
    if (!AppFeatureType.CHECK_UPDATES.has()) return
    val updateInfo = Prefs.updateInfo.collectAsStateValue()
    val newVersion = updateInfo.newVersion
    val newVersionPublishDate = updateInfo.publishDate
    val newVersionLog = updateInfo.log
    val newVersionSize = updateInfo.size
    val scope = rememberCoroutineScope()

    if (updateVM.updateDialogVisible.value && newVersionPublishDate.isNotBlank()) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.dialogSheetBackground,
            modifier = Modifier.heightIn(max = 400.dp),
            onDismissRequest = { updateVM.updateDialogVisible.value = false },
            icon = {
                Icon(
                    painter = painterResource(UiRes.drawable.ui_drawable_rocket),
                    contentDescription = stringResource(Res.string.change_log),
                )
            },
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = "Release $newVersion")
                    VerticalSpace(dp = 16.dp)
                    Text(
                        text = "${Instant.parse(newVersionPublishDate).formatDateTime()} ${newVersionSize.formatBytes()}",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    VerticalSpace(dp = 16.dp)
                }
            },
            text = {
                SelectionContainer {
                    Text(
                        modifier = Modifier.verticalScroll(rememberScrollState()),
                        text = newVersionLog,
                    )
                }
            },
            confirmButton = {
                PFilledButton(
                    text = stringResource(Res.string.update),
                    buttonSize = ButtonSize.MEDIUM,
                    onClick = {
                        updateVM.hideDialog()
                        AppUpdateDownloads.start()
                    },
                )
            },
            dismissButton = {
                PTextButton(
                    text = stringResource(Res.string.skip_this_version),
                    onClick = {
                        scope.launch {
                            Prefs.setSkipVersion(newVersion.toString())
                            updateVM.hideDialog()
                        }
                    },
                )
            },
        )
    }
}
