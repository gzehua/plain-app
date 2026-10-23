package com.ismartcoding.plain.ui.nearby

import com.ismartcoding.plain.features.nearby.pairingState

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import com.ismartcoding.plain.ui.base.PTextButton
import androidx.compose.runtime.*
import com.ismartcoding.plain.models.NearbyDevice
import com.ismartcoding.plain.features.nearby.RustPairingRuntime
import com.ismartcoding.plain.i18n.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import org.jetbrains.compose.resources.stringResource
import com.ismartcoding.plain.ui.components.TextFieldDialog

@Composable
fun PairPeerDialog(device: NearbyDevice, onDismiss: () -> Unit) {
    val scope = rememberCoroutineScope()
    var mode by remember { mutableStateOf("MANAGE") }
    var generation by remember { mutableStateOf<String?>(null) }
    var submitting by remember { mutableStateOf(false) }
    var disposed by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val state = generation?.let { NearbyViewModel.pairingStates[it] }
    val phase = state?.pairingState()?.phase
    val terminal = phase in setOf("SUCCEEDED", "FAILED", "CANCELLED")
    DisposableEffect(Unit) {
        onDispose {
            disposed = true
            generation?.let { scope.launch(NonCancellable) { RustPairingRuntime.cancelGeneration(it) } }
        }
    }
    LaunchedEffect(phase) {
        if (phase == "SUCCEEDED") onDismiss()
    }
    if (phase == "PASSWORD_REQUIRED") {
        TextFieldDialog(
            title = stringResource(Res.string.password),
            isPassword = true,
            errorText = error ?: "",
            validator = { !submitting },
            onDismissRequest = onDismiss,
            onConfirm = { value ->
                submitting = true
                scope.launch {
                    try { RustPairingRuntime.submit(generation!!, value) }
                    catch (cancelled: CancellationException) { throw cancelled }
                    catch (_: Exception) { error = "Pairing failed" }
                    finally { submitting = false }
                }
            },
        )
        return
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.pair_with_device, device.name)) },
        text = {
            Column {
                if (generation == null) {
                    listOf("MANAGE" to Res.string.pair_manage_device, "CHAT" to Res.string.pair_chat_only).forEach { (value, label) ->
                        Row {
                            RadioButton(selected = mode == value, onClick = { mode = value }, enabled = !submitting)
                            Text(stringResource(label))
                        }
                    }
                }
                if (generation != null && !terminal && phase != "PASSWORD_REQUIRED") Text(stringResource(Res.string.pending))
                state?.pairingState()?.verificationCode?.let { code ->
                    Text(stringResource(Res.string.login_compare_code))
                    Text(code)
                }
                (error ?: state?.pairingState()?.error)?.let { Text(it) }
            }
        },
        confirmButton = {
            if (generation == null) PTextButton(
                text = stringResource(Res.string.pair),
                enabled = !submitting,
                onClick = {
                    submitting = true
                    error = null
                    scope.launch {
                        try {
                            withContext(NonCancellable) {
                                if (generation == null) {
                                    val result = RustPairingRuntime.start(device, mode)
                                    val value = result.pairingState().generation
                                    generation = value
                                    if (disposed) RustPairingRuntime.cancelGeneration(value)
                                }
                            }
                        } catch (cancelled: CancellationException) { throw cancelled }
                    catch (_: Exception) { error = "Pairing failed" }
                        finally { submitting = false }
                    }
                },
            )
        },
        dismissButton = { PTextButton(text = stringResource(Res.string.cancel), onClick = onDismiss) },
    )
}
