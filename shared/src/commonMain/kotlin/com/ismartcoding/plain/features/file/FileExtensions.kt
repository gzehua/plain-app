package com.ismartcoding.plain.features.file

import com.ismartcoding.plain.graphql.models.File
import com.ismartcoding.plain.lib.extensions.getFilenameExtension

val File.extension: String get() = path.getFilenameExtension()
