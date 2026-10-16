package com.ismartcoding.plain.ui.extensions

import androidx.compose.runtime.*
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.features.files.FilePaths
import com.ismartcoding.plain.features.files.FilePathResolver
import kotlinx.coroutines.CancellationException

suspend fun String.getFinalPath(): String = FilePaths.resolve(this)

@Composable
fun rememberFilePath(reference: String): String {
    val path by produceState(if (FilePathResolver.needsResolution(reference)) "" else reference, reference) {
        value = try { FilePaths.resolve(reference) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (error: Exception) { LogCat.e(error.toString()); "" }
    }
    return path
}
