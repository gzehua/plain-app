package com.ismartcoding.plain.rust.http

import com.ismartcoding.plain.rust.http.PlainResponse

data class ApiResult(val response: PlainResponse?, val exception: Throwable? = null, val success: Boolean? = null) {
    fun isOk(): Boolean {
        return success ?: (response?.isOk() == true)
    }

}
