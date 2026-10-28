package com.ismartcoding.plain.ui.tools

import com.ismartcoding.plain.i18n.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.ismartcoding.plain.ui.components.ButtonSize
import com.ismartcoding.plain.ui.components.ButtonType
import com.ismartcoding.plain.platform.permissions.Permission
import com.ismartcoding.plain.platform.permissions.isGranted
import com.ismartcoding.plain.ui.platform.media.SoundMeterRecorder
import com.ismartcoding.plain.events.RequestPermissionsEvent
import com.ismartcoding.plain.helpers.FormatHelper
import com.ismartcoding.plain.lib.sendEvent
import com.ismartcoding.plain.ui.base.*
import com.ismartcoding.plain.ui.components.*
import com.ismartcoding.plain.ui.theme.dialogSheetBackground
import com.ismartcoding.plain.ui.base.PTextButton
import kotlin.math.abs
import kotlin.time.TimeSource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoundMeterPage(navController: NavHostController) {
    var decibelValuesDialogVisible = remember { mutableStateOf(false) }
    val total = remember { mutableFloatStateOf(0f) }
    val count = remember { mutableIntStateOf(0) }
    val min = remember { mutableFloatStateOf(0f) }
    val avg = remember { mutableFloatStateOf(0f) }
    val max = remember { mutableFloatStateOf(0f) }
    val isRunning = remember { mutableStateOf(false) }
    val decibel = remember { mutableFloatStateOf(0f) }
    val samples = remember { mutableStateListOf<SoundMeterSample>() }
    var selectedSample by remember { mutableStateOf<SoundMeterSample?>(null) }
    val clock = remember { TimeSource.Monotonic.markNow() }
    val decibelValueStrings = stringArrayResource(Res.array.decibel_values)
    val decibelValueString by remember(decibel.floatValue) {
        derivedStateOf { if (decibel.floatValue > 0) decibelValueStrings.getOrNull((decibel.floatValue / 10).toInt() - 1) ?: "" else "" }
    }

    SoundMeterRecorder(isRunning, decibel, total, count, min, avg, max) { value ->
        val now = clock.elapsedNow().inWholeMilliseconds / 1000f
        if (samples.isNotEmpty() && now - samples.last().seconds > 1f) samples.clear()
        while (samples.isNotEmpty() && (now - samples.first().seconds > 12f || samples.size >= 128)) samples.removeAt(0)
        selectedSample?.let { if (it !in samples) selectedSample = null }
        samples.add(SoundMeterSample(now, value))
    }

    PScaffold(topBar = {
        PTopAppBar(
            title = stringResource(Res.string.sound_meter), actions = {
                PCapsuleMoreClose(
                    onClose = { navController.navigateUp() },
                    onMore = { decibelValuesDialogVisible.value = true },
                )
            })
    }, content = { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                PCard {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = FormatHelper.formatFloat(abs(decibel.floatValue), digits = 1),
                                style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp, fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            HorizontalSpace(dp = 8.dp)
                            Text(
                                modifier = Modifier.padding(bottom = 8.dp),
                                text = "dB",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = decibelValueString,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            minLines = 2,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        selectedSample?.let { sample ->
                            Text(
                                "${FormatHelper.formatFloat(sample.decibels, digits = 1)} dB · −${FormatHelper.formatFloat((samples.last().seconds - sample.seconds).coerceAtLeast(0f), digits = 1)}s",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        SoundMeterChart(samples, selectedSample, { selectedSample = it }, Modifier.fillMaxWidth().height(128.dp).padding(top = 16.dp))
                    }
                }
            }
            item {
                PCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        listOf(
                            stringResource(Res.string.min) to min.floatValue,
                            stringResource(Res.string.avg) to avg.floatValue,
                            stringResource(Res.string.max) to max.floatValue,
                        ).forEach { (label, value) ->
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(FormatHelper.formatFloat(value, digits = 1), style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    PFilledButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(if (isRunning.value) Res.string.stop else Res.string.start),
                        onClick = {
                            if (isRunning.value) isRunning.value = false
                            else if (Permission.RECORD_AUDIO.isGranted()) isRunning.value = true
                            else sendEvent(RequestPermissionsEvent(Permission.RECORD_AUDIO))
                        },
                    )
                    if (isRunning.value || count.intValue > 0) {
                        PFilledButton(
                            modifier = Modifier.weight(1f),
                            text = stringResource(Res.string.reset),
                            type = ButtonType.DANGER,
                            onClick = {
                                total.floatValue = 0f; count.intValue = 0; decibel.floatValue = 0f
                                min.floatValue = 0f; max.floatValue = 0f; avg.floatValue = 0f
                                samples.clear()
                                selectedSample = null
                            },
                        )
                    }
                }
            }
            item { BottomSpace(paddingValues) }
        }
    })

    if (decibelValuesDialogVisible.value) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.dialogSheetBackground,
            onDismissRequest = { decibelValuesDialogVisible.value = false },
            confirmButton = { PTextButton(text = stringResource(Res.string.close), buttonSize = ButtonSize.MEDIUM, onClick = { decibelValuesDialogVisible.value = false }) },
            title = { Text(text = stringResource(Res.string.decibel_values), style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    decibelValueStrings.forEach {
                        SelectionContainer { Text(text = it, style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface), modifier = Modifier.padding(bottom = 8.dp)) }
                    }
                }
            })
    }
}
