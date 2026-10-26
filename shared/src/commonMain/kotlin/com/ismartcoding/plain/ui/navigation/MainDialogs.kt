package com.ismartcoding.plain.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.ismartcoding.plain.ui.base.PTextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import kotlinx.coroutines.delay
import com.ismartcoding.plain.ui.components.DialogHelper
import com.ismartcoding.plain.ui.components.ToastManager
import com.ismartcoding.plain.ui.platform.system.keepScreenOn
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ismartcoding.plain.ui.components.ButtonSize
import com.ismartcoding.plain.ui.components.PToast
import com.ismartcoding.plain.ui.theme.dialogSheetBackground

@Composable
fun MainDialogs() {
    val loading by DialogHelper.loading
    val confirmation by DialogHelper.confirmation
    val toast by ToastManager.current

    DisposableEffect(loading) {
        keepScreenOn(loading)
        onDispose { keepScreenOn(false) }
    }
    LaunchedEffect(toast) {
        toast?.let {
            delay(it.durationMs)
            if (ToastManager.current.value === it) ToastManager.current.value = null
        }
    }

    if (loading) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(MaterialTheme.colorScheme.background, shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
    }
    confirmation?.let {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.dialogSheetBackground,
            onDismissRequest = { DialogHelper.confirmation.value = null },
            title = {
                if (it.title.isNotEmpty()) {
                    Text(it.title)
                }
            },
            text = { Text(it.message) },
            confirmButton = {
                PTextButton(
                    text = it.confirmButton.first,
                    buttonSize = ButtonSize.MEDIUM,
                    onClick = {
                        DialogHelper.confirmation.value = null
                        it.confirmButton.second()
                    },
                    contentColor = if (it.danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                )
            },
            dismissButton = {
                it.dismissButton?.let { dismiss ->
                    PTextButton(
                        text = dismiss.first,
                        onClick = {
                            DialogHelper.confirmation.value = null
                            dismiss.second()
                        },
                    )
                }
            },
        )
    }
    toast?.let { message ->
        PToast(
            message = message.message, type = message.type, actionLabel = message.actionLabel,
            onAction = { ToastManager.current.value = null; message.onAction?.invoke() },
            onDismiss = { ToastManager.current.value = null },
        )
    }
}
