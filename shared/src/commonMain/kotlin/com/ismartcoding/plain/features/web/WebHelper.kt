package com.ismartcoding.plain.features.web

import com.ismartcoding.plain.ui.components.DialogHelper

import com.ismartcoding.plain.i18n.*
import com.ismartcoding.plain.platform.system.launchUrl

object WebHelper {
    fun open(url: String) {
        try {
            launchUrl(url)
        } catch (ex: Exception) {
            DialogHelper.showMessage(Res.string.no_browser_error)
        }
    }
}
