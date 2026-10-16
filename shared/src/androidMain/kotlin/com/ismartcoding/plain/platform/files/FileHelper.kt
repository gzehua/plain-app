package com.ismartcoding.plain.platform.files

import com.ismartcoding.plain.helpers.PathHelper

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.ismartcoding.plain.appContext
import com.ismartcoding.plain.extensions.newFile
import com.ismartcoding.plain.lib.extensions.getFileName
import com.ismartcoding.plain.lib.extensions.getFilenameFromPath
import com.ismartcoding.plain.lib.extensions.hasPermission
import com.ismartcoding.plain.lib.extensions.scanFileByConnection
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.platform.isRPlus
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

object FileHelper {
    fun hasStoragePermission(context: Context): Boolean {
        return if (isRPlus()) {
            Environment.isExternalStorageManager()
        } else {
            context.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    fun copyFile(
        context: Context,
        pathFrom: Uri,
        pathTo: String,
    ) {
        context.contentResolver.openInputStream(pathFrom).use { input ->
            if (input != null) {
                val targetPath = Paths.get(pathTo)
                Files.copy(input, targetPath, StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }

    fun copyFileToDownloads(path: String, newName: String = ""): String {
        return copyFileToPublicDir(path, Environment.DIRECTORY_DOWNLOADS, newName)
    }

    fun copyFileToPublicDir(path: String, dirName: String, newName: String = ""): String {
        try {
            val fileName = newName.ifEmpty { path.getFilenameFromPath() }
            val file = createPublicFile(fileName, dirName)
            File(path).copyTo(file)
            appContext.scanFileByConnection(file, null)
            return file.absolutePath
        } catch (ex: Exception) {
            ex.printStackTrace()
            LogCat.e(ex.toString())
        }
        return ""
    }

    fun copyFileToDownloads(context: Context, uri: Uri): String {
        try {
            val fileName = uri.getFileName(context)
            val file = createPublicFile(fileName, Environment.DIRECTORY_DOWNLOADS)
            val outputStream = FileOutputStream(file)
            if (uri.scheme == "content") {
                val inputStream = context.contentResolver.openInputStream(uri)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
            } else {
                val client = OkHttpClient()
                val request = Request.Builder()
                    .url(uri.toString())
                    .build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val inputStream = response.body.byteStream()
                    inputStream?.copyTo(outputStream)
                    inputStream?.close()
                    outputStream.close()
                }
            }
            appContext.scanFileByConnection(file, null)
            return file.absolutePath
        } catch (ex: Exception) {
            ex.printStackTrace()
            LogCat.e(ex.toString())
        }
        return ""
    }

    private fun createPublicFile(fileName: String, dirName: String): File {
        val dir = PathHelper.getPlainPublicDir(dirName)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        var file = File(dir, fileName)
        if (file.exists()) {
            file = file.newFile()
        }
        return file
    }

}
