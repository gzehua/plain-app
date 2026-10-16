package com.ismartcoding.plain.features.files

object ZipBrowserHelper {
    const val ZIP_SEPARATOR = "!zip!/"

    fun isZipPath(path: String): Boolean = path.contains(ZIP_SEPARATOR)

    fun getZipFilePath(path: String): String = path.substringBefore(ZIP_SEPARATOR)

    fun getInternalPath(path: String): String = path.substringAfter(ZIP_SEPARATOR, "")

    fun joinPath(zipFilePath: String, internalPath: String): String =
        "$zipFilePath$ZIP_SEPARATOR$internalPath"

    /** Returns the display name for the breadcrumb/title of a zip virtual path. */
    fun getDisplayName(zipVirtualPath: String): String {
        val internalPath = getInternalPath(zipVirtualPath)
        val trimmed = internalPath.trimEnd('/')
        return if (trimmed.isEmpty()) {
            getZipFilePath(zipVirtualPath).substringAfterLast("/")
        } else {
            trimmed.substringAfterLast("/")
        }
    }
}
