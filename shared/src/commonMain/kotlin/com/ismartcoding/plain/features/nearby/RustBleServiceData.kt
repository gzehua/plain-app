package com.ismartcoding.plain.features.nearby


import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.native.RustBleBridge

object RustBleServiceData {
    fun decode(data: ByteArray?): BleAdvertisement? {
        if (data == null) return null
        return JsonHelper.jsonDecode(checkNotNull(RustBleBridge.call(9, 0, 0, 0, 0, false, data)).decodeToString())
    }

}
