package com.ismartcoding.plain.platform.system

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageInstaller
import com.ismartcoding.plain.appContext
import java.io.File
import java.io.FileInputStream

internal object PackageInstallSession {
    fun write(apks: List<File>) {
        val sources = mutableListOf<FileInputStream>()
        try {
            apks.forEach { sources += it.inputStream() }
            val installer = appContext.packageManager.packageInstaller
            val sessionId = installer.createSession(PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL))
            installer.openSession(sessionId).use { session ->
                try {
                    sources.forEachIndexed { index, source ->
                        session.openWrite("apk_$index.apk", 0, source.channel.size()).use { out ->
                            source.copyTo(out)
                            session.fsync(out)
                        }
                    }
                    val callback = Intent(appContext, PackageInstallReceiver::class.java)
                    val pending = PendingIntent.getBroadcast(
                        appContext, sessionId, callback,
                        PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                    )
                    session.commit(pending.intentSender)
                } catch (error: Exception) {
                    runCatching { session.abandon() }
                    throw error
                }
            }
        } finally {
            sources.forEach { source -> runCatching { source.close() } }
        }
    }
}
