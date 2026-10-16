package com.ismartcoding.plain.lib.extensions

/**
 * Detect whether [header] is the `ftyp` box of a HEIF/HEIC/AVIF container.
 * The bytes are the first 12 bytes of the file.
 */
fun isHeifHeader(header: ByteArray): Boolean {
    if (header.size < 12) return false
    return header[4] == 0x66.toByte() && // 'f'
        header[5] == 0x74.toByte() && // 't'
        header[6] == 0x79.toByte() && // 'y'
        header[7] == 0x70.toByte() && // 'p'
        header.copyOfRange(8, 12).decodeToString() in listOf("heic", "heix", "hevc", "hevx", "avif")
}
