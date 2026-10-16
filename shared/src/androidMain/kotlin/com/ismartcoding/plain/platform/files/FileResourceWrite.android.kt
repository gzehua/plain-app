package com.ismartcoding.plain.platform.files

import android.net.Uri
import com.ismartcoding.plain.appContext
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.io.FileInputStream

internal actual suspend fun writeResourceFile(source: String, destination: String): Boolean {
    require(destination.startsWith("content://")) { "Unsupported writable resource" }
    FileInputStream(source).use { input ->
        val output = appContext.contentResolver.openOutputStream(Uri.parse(destination), "wt") ?: return false
        output.use {
            val buffer = ByteArray(64 * 1024)
            while (true) {
                currentCoroutineContext().ensureActive()
                val count = input.read(buffer)
                if (count < 0) break
                output.write(buffer, 0, count)
            }
        }
    }
    return true
}
