package com.ismartcoding.plain.ui.platform.media

import com.ismartcoding.plain.platform.system.MinimalScrollHandle

import com.ismartcoding.plain.ui.media.extensions.collectAsStateValue

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.ismartcoding.plain.Prefs
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.lib.pdfviewer.PDFView
import com.ismartcoding.plain.lib.pdfviewer.listener.OnPageErrorListener
import com.ismartcoding.plain.lib.pdfviewer.util.FitPolicy
import com.ismartcoding.plain.ui.theme.DarkTheme

@Composable
actual fun PdfView(
    uri: String,
    modifier: Modifier,
) {
    val parsedUri = Uri.parse(uri)
    val darkTheme = Prefs.darkTheme.collectAsStateValue()
    val isDarkTheme = DarkTheme.isDarkTheme(darkTheme)
    val pdfFollowDarkTheme = Prefs.pdfFollowDarkTheme.collectAsStateValue()
    AndroidView(
        modifier = modifier,
        factory = { factoryContext ->
            val v = PDFView(factoryContext)
            v.fromUri(parsedUri)
                .defaultPage(0)
                .enableAnnotationRendering(true)
                .scrollHandle(MinimalScrollHandle(factoryContext))
                .spacing(10)
                .onPageError(object : OnPageErrorListener {
                    override fun onPageError(
                        page: Int,
                        t: Throwable?,
                    ) {
                        LogCat.e(page.toString())
                    }
                })
                .pageFitPolicy(FitPolicy.BOTH)
                .nightMode(pdfFollowDarkTheme && isDarkTheme)
                .load()
            v
        }
    )
}
