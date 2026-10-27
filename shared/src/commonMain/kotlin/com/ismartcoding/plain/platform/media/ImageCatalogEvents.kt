package com.ismartcoding.plain.platform.media

import com.ismartcoding.plain.rust.native.RustHostReports

import com.ismartcoding.plain.lib.coIO

internal fun reportImageCatalogChanged() {
    coIO { RustHostReports.imageCatalogChanged("null") }
}
