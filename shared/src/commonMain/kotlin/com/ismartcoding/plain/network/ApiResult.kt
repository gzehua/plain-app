package com.ismartcoding.plain.network

data class ApiResult(val exception: Throwable? = null, val success: Boolean = false) {
    fun isOk(): Boolean = success
}
