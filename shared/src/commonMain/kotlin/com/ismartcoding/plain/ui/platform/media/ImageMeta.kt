package com.ismartcoding.plain.ui.platform.media

import com.ismartcoding.plain.ui.i18n.LocaleHelper
import com.ismartcoding.plain.platform.system.formatDateTime

import com.ismartcoding.plain.i18n.*

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import org.jetbrains.compose.resources.stringResource
import com.ismartcoding.plain.lib.extensions.getFilenameExtension
import com.ismartcoding.plain.models.ImageFileInfo
import com.ismartcoding.plain.helpers.FormatHelper
import com.ismartcoding.plain.ui.base.PListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class ImageMetaTexts(
    val flash: String,
    val exposureProgram: String,
    val meteringMode: String,
    val whiteBalance: String,
)

fun formatImageMetaTexts(mt: ImageFileInfo): ImageMetaTexts = ImageMetaTexts(
    flash = getFlashText(mt.flash),
    exposureProgram = getExposureProgramText(mt.exposureProgram),
    meteringMode = getMeteringModeText(mt.meteringMode),
    whiteBalance = getWhiteBalanceText(mt.whiteBalance),
)

private fun getExposureProgramText(exposureProgram: Int): String {
    return when (exposureProgram) {
        1 -> LocaleHelper.getString(Res.string.exposure_program_manual)
        2 -> LocaleHelper.getString(Res.string.exposure_program_normal)
        3 -> LocaleHelper.getString(Res.string.exposure_program_aperture_priority)
        4 -> LocaleHelper.getString(Res.string.exposure_program_shutter_priority)
        5 -> LocaleHelper.getString(Res.string.exposure_program_creative)
        6 -> LocaleHelper.getString(Res.string.exposure_program_action)
        7 -> LocaleHelper.getString(Res.string.exposure_program_portrait)
        8 -> LocaleHelper.getString(Res.string.exposure_program_landscape)
        else -> ""
    }
}

private fun getMeteringModeText(meteringMode: Int): String {
    return when (meteringMode) {
        1 -> LocaleHelper.getString(Res.string.metering_mode_average)
        2 -> LocaleHelper.getString(Res.string.metering_mode_center_weight_average)
        4 -> LocaleHelper.getString(Res.string.metering_mode_multi_spot)
        255 -> LocaleHelper.getString(Res.string.metering_mode_other)
        6 -> LocaleHelper.getString(Res.string.metering_mode_partial)
        5 -> LocaleHelper.getString(Res.string.metering_mode_pattern)
        3 -> LocaleHelper.getString(Res.string.metering_mode_spot)
        else -> ""
    }
}

private fun getWhiteBalanceText(whiteBalance: Int): String {
    return when (whiteBalance) {
        0 -> LocaleHelper.getString(Res.string.white_balance_auto)
        1 -> LocaleHelper.getString(Res.string.white_balance_manual)
        else -> ""
    }
}

private fun getFlashText(flash: Int): String {
    return when (flash) {
        1 -> LocaleHelper.getString(Res.string.flash_fired)
        4 -> LocaleHelper.getString(Res.string.flash_return_light_not_detected)
        6 -> LocaleHelper.getString(Res.string.flash_return_light_detected)
        8 -> LocaleHelper.getString(Res.string.flash_mode_compulsory_firing)
        16 -> LocaleHelper.getString(Res.string.flash_mode_compulsory_suppression)
        24 -> LocaleHelper.getString(Res.string.flash_mode_auto)
        32 -> LocaleHelper.getString(Res.string.flash_no_flash_function)
        64 -> LocaleHelper.getString(Res.string.flash_red_eye_supported)
        else -> ""
    }
}

@Composable
fun ImageMetaRows(
    path: String,
    loadMeta: suspend (String) -> ImageFileInfo?,
    formatTexts: (ImageFileInfo) -> ImageMetaTexts,
) {
    val extension = path.getFilenameExtension()
    if (setOf("gif", "svg").contains(extension)) {
        return
    }

    var meta by remember {
        mutableStateOf<ImageFileInfo?>(null)
    }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.Default) {
            meta = loadMeta(path)
        }
    }
    meta?.let { mt ->
        if (mt.isScreenshot) {
            return
        }
        val texts = formatTexts(mt)
        mt.takenAt?.let { takenAt ->
            PListItem(title = stringResource(Res.string.taken_at), value = takenAt.formatDateTime())
        }
        if (mt.resolutionX > 0 && mt.resolutionY > 0) {
            PListItem(title = stringResource(Res.string.resolution), value = "${mt.resolutionX} x ${mt.resolutionY}")
        }
        if (mt.make.isNotEmpty()) {
            PListItem(title = stringResource(Res.string.device_make), value = mt.make)
        }
        if (mt.model.isNotEmpty()) {
            PListItem(title = stringResource(Res.string.device_model), value = mt.model)
        }
        if (mt.colorSpace.isNotEmpty()) {
            PListItem(title = stringResource(Res.string.color_profile), value = mt.colorSpace)
        }
        if (mt.apertureValue > 0) {
            PListItem(title = stringResource(Res.string.aperture_value), value = FormatHelper.formatDouble(mt.apertureValue, 3))
        }
        if (mt.exposureTime.isNotEmpty()) {
            PListItem(title = stringResource(Res.string.exposure_time), value = mt.exposureTime)
        }
        if (mt.focalLength.isNotEmpty()) {
            PListItem(title = stringResource(Res.string.focal_length), value = mt.focalLength)
        }
        if (mt.isoSpeed > 0) {
            PListItem(title = stringResource(Res.string.iso_speed), value = mt.isoSpeed.toString())
        }
        if (mt.flash > 0) {
            PListItem(title = stringResource(Res.string.flash), value = texts.flash)
        }
        if (mt.fNumber > 0) {
            PListItem(title = stringResource(Res.string.f_number), value = "f/" + FormatHelper.formatDouble(mt.fNumber, 1))
        }
        PListItem(title = stringResource(Res.string.exposure_program), value = texts.exposureProgram)
        PListItem(title = stringResource(Res.string.metering_mode), value = texts.meteringMode)
        PListItem(title = stringResource(Res.string.white_balance), value = texts.whiteBalance)
        if (mt.creator.isNotEmpty()) {
            PListItem(title = stringResource(Res.string.creator), value = mt.creator)
        }
        if (mt.description.isNotEmpty()) {
            PListItem(title = stringResource(Res.string.description), value = mt.description)
        }
    }
}
