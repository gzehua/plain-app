package com.ismartcoding.plain.ui.audio.player

import com.ismartcoding.plain.i18n.*

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ismartcoding.plain.lib.TimeHelper
import com.ismartcoding.plain.features.audio.AudioSleepTimer
import com.ismartcoding.plain.ui.components.ButtonType
import com.ismartcoding.plain.ui.base.PFilledButton
import com.ismartcoding.plain.ui.base.PModalBottomSheet
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerPage(onDismissRequest: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val durationMs by AudioSleepTimer.durationMs
    val endsAt by AudioSleepTimer.endsAt
    var remainingTimeMs by remember(endsAt) { mutableLongStateOf(maxOf(0L, endsAt - TimeHelper.nowMillis())) }
    var selectedTimeMinutes by remember { mutableIntStateOf(15) }
    val timerActive = remainingTimeMs > 0

    LaunchedEffect(Unit) {
        sheetState.expand()
    }

    LaunchedEffect(endsAt) {
        while (remainingTimeMs > 0) {
            delay(1000)
            remainingTimeMs = maxOf(0L, endsAt - TimeHelper.nowMillis())
        }
    }

    PModalBottomSheet(onDismissRequest = onDismissRequest, sheetState = sheetState) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .padding(bottom = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = stringResource(Res.string.sleep_timer),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (timerActive) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        SleepTimerActiveContent(remainingTimeMs, durationMs)
                        Spacer(modifier = Modifier.weight(1f))
                        PFilledButton(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(Res.string.cancel_timer),
                            onClick = {
                                AudioSleepTimer.cancel()
                            }, type = ButtonType.DANGER
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        SleepTimerSelectionContent(selectedTimeMinutes) { selectedTimeMinutes = it }
                        Spacer(modifier = Modifier.weight(1f))
                        PFilledButton(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(Res.string.start_timer),
                            onClick = {
                                AudioSleepTimer.start(selectedTimeMinutes * 60 * 1000L)
                            })
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}
