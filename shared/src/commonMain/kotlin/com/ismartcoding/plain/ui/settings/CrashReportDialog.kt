package com.ismartcoding.plain.ui.settings

import com.ismartcoding.plain.rust.native.RustNativeApi


import com.ismartcoding.plain.i18n.*

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.ismartcoding.plain.ui.base.PTextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import com.ismartcoding.plain.ui.components.ButtonSize
import com.ismartcoding.plain.features.files.TextFileType
import androidx.navigation.NavHostController
import com.ismartcoding.plain.ui.components.DialogHelper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import com.ismartcoding.plain.ui.base.PFilledButton
import com.ismartcoding.plain.ui.navigation.navigateTextFile
import com.ismartcoding.plain.ui.theme.dialogSheetBackground

@Composable
fun CrashReportDialog(
    crashReport: String,
    navController: NavHostController?,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var saving by remember { mutableStateOf(false) }
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.dialogSheetBackground,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.crash_report_title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = stringResource(Res.string.crash_report_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        },
        confirmButton = {
            PFilledButton(
                text = stringResource(Res.string.view),
                buttonSize = ButtonSize.MEDIUM,
                enabled = !saving,
                isLoading = saving,
                onClick = {
                    scope.launch {
                        saving = true
                        try {
                            val path = RustNativeApi.createCrashReport(crashReport)
                            navController?.navigateTextFile(path, type = TextFileType.CRASH_REPORT)
                            onDismiss()
                        } catch (error: CancellationException) {
                            throw error
                        } catch (error: Exception) {
                            DialogHelper.showMessage(error.message.orEmpty())
                        } finally {
                            saving = false
                        }
                    }
                })
        },
        dismissButton = {
            PTextButton(text = stringResource(Res.string.cancel), onClick = onDismiss)
        },
    )
}
