package com.ismartcoding.plain.platform.system

/**
 * Swift-implemented share controller. iOS share sheets
 * (`UIActivityViewController`) must be presented from a `UIViewController`
 * and use ObjC APIs, so the real UI lives in Swift and is bridged through
 * this interface.
 *
 * Kotlin calls [shareText] / [shareFile] / [shareFiles] when commonMain
 * invokes the `shareText` / `shareFile` / `shareFiles` expect fun. Swift
 * presents the share sheet and dismisses it on completion.
 */
interface IosShareController {
    fun shareText(text: String)
    fun shareFile(path: String, mimeType: String)
    fun shareFiles(paths: List<String>, mimeTypes: List<String>)
    fun openFileExternal(path: String)
}
