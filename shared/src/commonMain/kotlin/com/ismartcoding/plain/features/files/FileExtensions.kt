package com.ismartcoding.plain.features.files

import com.ismartcoding.plain.models.File
import com.ismartcoding.plain.lib.extensions.getFilenameExtension

val File.extension: String get() = path.getFilenameExtension()
