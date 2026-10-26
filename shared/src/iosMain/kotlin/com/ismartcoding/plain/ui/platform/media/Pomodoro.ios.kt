@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package com.ismartcoding.plain.ui.platform.media

import com.ismartcoding.plain.lib.coMain
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.lib.toNSData
import com.ismartcoding.plain.platform.notifications.sendLocalNotification
import com.ismartcoding.plain.ui.pomodoro.pomodoroCompletionWav
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.AVFAudio.AVAudioPlayer
import platform.AVFAudio.AVAudioPlayerDelegateProtocol
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.AVAudioSessionCategoryPlayAndRecord
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.darwin.NSObject

private var completionSoundPlayer: AVAudioPlayer? = null
private val completionSoundDelegate = CompletionSoundDelegate()

private class CompletionSoundDelegate : NSObject(), AVAudioPlayerDelegateProtocol {
    override fun audioPlayerDidFinishPlaying(player: AVAudioPlayer, successfully: Boolean) {
        coMain {
            if (!successfully) LogCat.e("Pomodoro completion sound did not finish successfully")
            if (completionSoundPlayer == player) completionSoundPlayer = null
        }
    }

    override fun audioPlayerDecodeErrorDidOccur(player: AVAudioPlayer, error: NSError?) {
        coMain {
            LogCat.e("Pomodoro completion sound decode failed: ${error?.localizedDescription.orEmpty()}")
            if (completionSoundPlayer == player) completionSoundPlayer = null
        }
    }
}

internal actual suspend fun postPomodoroNotification(title: String, message: String) {
    sendLocalNotification(NSUUID().UUIDString, title, message, playSound = false)
}

internal actual suspend fun playPomodoroSound(path: String?) {
    withContext(Dispatchers.Main) {
        memScoped {
            val nativeError = alloc<ObjCObjectVar<NSError?>>()
            nativeError.value = null
            val session = AVAudioSession.sharedInstance()
            if (session.category != AVAudioSessionCategoryPlayback && session.category != AVAudioSessionCategoryPlayAndRecord) {
                check(session.setCategory(AVAudioSessionCategoryPlayback, nativeError.ptr)) {
                    "Pomodoro audio session: ${nativeError.value?.localizedDescription}"
                }
            }
            val player = if (path == null) {
                AVAudioPlayer(data = pomodoroCompletionWav().toNSData(), error = nativeError.ptr)
            } else {
                AVAudioPlayer(contentsOfURL = NSURL.fileURLWithPath(path), error = nativeError.ptr)
            } ?: error("Pomodoro completion sound: ${nativeError.value?.localizedDescription}")
            player.delegate = completionSoundDelegate
            check(player.prepareToPlay()) { "Pomodoro completion sound preparation failed" }
            completionSoundPlayer?.stop()
            completionSoundPlayer = player
            if (!player.play()) {
                completionSoundPlayer = null
                error("Pomodoro completion sound playback failed")
            }
        }
    }
}
