package com.ismartcoding.plain.platform.media

import android.content.Context
import android.graphics.Bitmap
import com.ismartcoding.plain.ui.media.thumbnail.ThumbnailGenerator
import java.io.File

object BitmapHelper {
    suspend fun decodeBitmapFromFileAsync(
        context: Context,
        path: String,
        reqWidth: Int,
        reqHeight: Int,
    ): Bitmap? {
        return ThumbnailGenerator.getBitmapAsync(context, File(path), reqWidth, reqHeight)
    }
}
