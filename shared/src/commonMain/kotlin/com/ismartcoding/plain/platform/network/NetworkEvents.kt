package com.ismartcoding.plain.platform.network

import com.ismartcoding.plain.rust.native.RustHostReports

import com.ismartcoding.plain.lib.coIO
import com.ismartcoding.plain.lib.logcat.LogCat
import com.ismartcoding.plain.rust.RustRuntime
import kotlinx.coroutines.CancellationException

object NetworkEvents {
    fun changed() {
        if (!RustRuntime.isCoreInitialized) return
        coIO {
            try { RustHostReports.networkInterfacesChanged("null") }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { LogCat.e("Network change report failed", error) }
        }
    }
}
