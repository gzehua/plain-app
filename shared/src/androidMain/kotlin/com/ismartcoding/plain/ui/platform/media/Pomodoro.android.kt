package com.ismartcoding.plain.ui.platform.media

import com.ismartcoding.plain.platform.permissions.Permission
import com.ismartcoding.plain.platform.permissions.isGranted
import com.ismartcoding.plain.platform.system.appResourceDrawable

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import android.net.Uri
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ismartcoding.plain.Constants
import com.ismartcoding.plain.MainActivity
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.ui.platform.notifications.NotificationHelper
import com.ismartcoding.plain.lib.coMain
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.ui.theme.BrandPrimary
import com.ismartcoding.plain.ui.pomodoro.POMODORO_SAMPLE_RATE
import com.ismartcoding.plain.ui.pomodoro.pomodoroCompletionSamples
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private var customSoundPlayer: MediaPlayer? = null
private var customSoundPreparation: CancellableContinuation<Unit>? = null

@SuppressLint("MissingPermission")
internal actual suspend fun postPomodoroNotification(title: String, message: String) {
    val context = appContext
    NotificationHelper.ensureDefaultChannel()

    val intent = Intent(context, MainActivity::class.java).apply {
        `package` = context.packageName
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_IMMUTABLE
    )

    val notificationId = NotificationHelper.generateId()
    val notification = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(appResourceDrawable("notification"))
        .setColor(BrandPrimary.toArgb())
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setSilent(true)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build()

    try {
        if (Permission.POST_NOTIFICATIONS.isGranted()) {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }
    } catch (e: Exception) {
        LogCat.e("Failed to show Pomodoro notification: ${e.message}")
    }
}

internal actual suspend fun playPomodoroSound(path: String?) {
    withContext(Dispatchers.Main) {
        customSoundPlayer?.let { releaseCustomSoundPlayer(it) }
        if (path == null) {
            withContext(Dispatchers.IO) { playNotificationSound() }
        } else {
            playCustomSound(path)
        }
    }
}

private suspend fun playCustomSound(path: String): Unit = suspendCancellableCoroutine { continuation ->
    val player = MediaPlayer()
    customSoundPlayer = player
    customSoundPreparation = continuation
    continuation.invokeOnCancellation {
        coMain { releaseCustomSoundPlayer(player) }
    }
    player.setOnPreparedListener { prepared ->
        if (customSoundPlayer !== prepared) return@setOnPreparedListener
        if (!continuation.isActive) {
            releaseCustomSoundPlayer(prepared)
            return@setOnPreparedListener
        }
        try {
            prepared.start()
        } catch (error: Exception) {
            releaseCustomSoundPlayer(prepared, error)
            return@setOnPreparedListener
        }
        prepared.setOnPreparedListener(null)
        customSoundPreparation = null
        continuation.resume(Unit)
    }
    player.setOnCompletionListener { releaseCustomSoundPlayer(it) }
    player.setOnErrorListener { failed, what, extra ->
        releaseCustomSoundPlayer(failed, IllegalStateException("Pomodoro completion sound failed ($what, $extra)"))
        true
    }
    try {
        player.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        if (path.startsWith("content://")) player.setDataSource(appContext, Uri.parse(path))
        else player.setDataSource(path)
        player.prepareAsync()
    } catch (error: Exception) {
        releaseCustomSoundPlayer(player, error)
    }
}

private fun releaseCustomSoundPlayer(player: MediaPlayer, error: Exception? = null) {
    if (customSoundPlayer !== player) return
    val preparation = customSoundPreparation
    customSoundPlayer = null
    customSoundPreparation = null
    player.release()
    if (error != null) {
        if (preparation?.isActive == true) preparation.resumeWithException(error)
        else LogCat.e("Pomodoro completion sound", error)
    } else if (preparation?.isActive == true) {
        preparation.resume(Unit)
    }
}

private fun playNotificationSound() {
    val sampleRate = POMODORO_SAMPLE_RATE
    val audioBuffer = pomodoroCompletionSamples()

    val audioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    val audioFormat = AudioFormat.Builder()
        .setSampleRate(sampleRate)
        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
        .build()

    val bufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT,
    )

    val audioTrack = AudioTrack.Builder()
        .setAudioAttributes(audioAttributes)
        .setAudioFormat(audioFormat)
        .setBufferSizeInBytes(maxOf(bufferSize, audioBuffer.size * 2))
        .setTransferMode(AudioTrack.MODE_STATIC)
        .build()

    audioTrack.write(audioBuffer, 0, audioBuffer.size)
    audioTrack.setNotificationMarkerPosition(audioBuffer.size)
    audioTrack.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
        override fun onMarkerReached(track: AudioTrack?) {
            track?.stop()
            track?.release()
        }

        override fun onPeriodicNotification(track: AudioTrack?) {}
    })

    audioTrack.play()
}
