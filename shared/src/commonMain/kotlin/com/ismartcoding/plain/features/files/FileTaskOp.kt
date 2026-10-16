package com.ismartcoding.plain.features.files

data class FileTaskOp(val src: String, val dst: String, val overwrite: Boolean = false)
