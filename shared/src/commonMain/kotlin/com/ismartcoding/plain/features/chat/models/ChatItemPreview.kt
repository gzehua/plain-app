package com.ismartcoding.plain.features.chat.models

import com.ismartcoding.plain.models.ChatItem
import com.ismartcoding.plain.i18n.Res
import com.ismartcoding.plain.i18n.file
import com.ismartcoding.plain.i18n.files
import com.ismartcoding.plain.i18n.image
import com.ismartcoding.plain.i18n.images
import com.ismartcoding.plain.i18n.message
import com.ismartcoding.plain.i18n.shared_folder
import com.ismartcoding.plain.i18n.video
import com.ismartcoding.plain.i18n.videos
import com.ismartcoding.plain.ui.i18n.LocaleHelper
import com.ismartcoding.plain.ui.resources.file as ui_drawable_file
import com.ismartcoding.plain.ui.resources.files as ui_drawable_files
import com.ismartcoding.plain.ui.resources.image as ui_drawable_image
import com.ismartcoding.plain.ui.resources.video as ui_drawable_video

fun ChatItem.getMessagePreview(): String {
    return when (messageContent.type) {
        MessageType.TEXT -> {
            val textMessage = messageContent.value as? MessageText
            textMessage?.text?.take(50) ?: LocaleHelper.getString(Res.string.message)
        }

        MessageType.IMAGES -> {
            val imagesMessage = messageContent.value as? MessageImages
            val items = imagesMessage?.items ?: emptyList()
            val videoCount = items.count { it.durationMs > 0 }
            val imageCount = items.size - videoCount
            when {
                imageCount > 0 && videoCount > 0 -> {
                    val imgPart = if (imageCount > 1) "$imageCount ${LocaleHelper.getString(Res.string.images)}" else LocaleHelper.getString(Res.string.image)
                    val vidPart = if (videoCount > 1) "$videoCount ${LocaleHelper.getString(Res.string.videos)}" else LocaleHelper.getString(Res.string.video)
                    "$imgPart, $vidPart"
                }

                videoCount > 0 -> {
                    if (videoCount > 1) "$videoCount ${LocaleHelper.getString(Res.string.videos)}" else LocaleHelper.getString(Res.string.video)
                }

                else -> {
                    if (imageCount > 1) "$imageCount ${LocaleHelper.getString(Res.string.images)}" else LocaleHelper.getString(Res.string.image)
                }
            }
        }

        MessageType.FILES -> {
            val filesMessage = messageContent.value as? MessageFiles
            val count = filesMessage?.items?.size ?: 0
            if (count > 1) "$count ${LocaleHelper.getString(Res.string.files)}" else LocaleHelper.getString(Res.string.file)
        }

        MessageType.SHARE -> LocaleHelper.getString(Res.string.shared_folder)

        else -> LocaleHelper.getString(Res.string.message)
    }
}
