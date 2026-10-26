package com.ismartcoding.plain.ui.pomodoro

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sin

internal const val POMODORO_SAMPLE_RATE = 44100

internal fun pomodoroCompletionSamples(): ShortArray {
    val durationMs = 300
    val samples = (POMODORO_SAMPLE_RATE * durationMs / 1000.0).toInt()
    return ShortArray(samples) { i ->
        val time = i.toDouble() / POMODORO_SAMPLE_RATE
        val startFreq = 800.0
        val endFreq = 600.0
        val modulationTime = 0.1
        val frequency = if (time <= modulationTime) {
            startFreq * exp(ln(endFreq / startFreq) * (time / modulationTime))
        } else {
            endFreq
        }
        val startGain = 0.3
        val endGain = 0.01
        val gain = startGain * exp(ln(endGain / startGain) * (time / (durationMs / 1000.0)))
        val sample = (sin(2 * PI * frequency * time) * gain * Short.MAX_VALUE).toInt()
        sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
    }
}

internal fun pomodoroCompletionWav(): ByteArray {
    val samples = pomodoroCompletionSamples()
    val dataSize = samples.size * 2
    return ByteArray(44 + dataSize).apply {
        "RIFF".encodeToByteArray().copyInto(this, 0)
        writeLittleEndian(4, 36 + dataSize)
        "WAVEfmt ".encodeToByteArray().copyInto(this, 8)
        writeLittleEndian(16, 16)
        writeLittleEndian(20, 1, 2)
        writeLittleEndian(22, 1, 2)
        writeLittleEndian(24, POMODORO_SAMPLE_RATE)
        writeLittleEndian(28, POMODORO_SAMPLE_RATE * 2)
        writeLittleEndian(32, 2, 2)
        writeLittleEndian(34, 16, 2)
        "data".encodeToByteArray().copyInto(this, 36)
        writeLittleEndian(40, dataSize)
        samples.forEachIndexed { index, sample -> writeLittleEndian(44 + index * 2, sample.toInt(), 2) }
    }
}

private fun ByteArray.writeLittleEndian(offset: Int, value: Int, bytes: Int = 4) {
    repeat(bytes) { index -> this[offset + index] = (value ushr (index * 8)).toByte() }
}
