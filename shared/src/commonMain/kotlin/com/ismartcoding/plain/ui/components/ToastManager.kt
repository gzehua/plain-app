package com.ismartcoding.plain.ui.components

import androidx.compose.runtime.mutableStateOf

class ToastMessage(
    val message: String,
    val type: ToastType = ToastType.INFO,
    val durationMs: Long = 2000L,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
)

object ToastManager {
    val current = mutableStateOf<ToastMessage?>(null)

    fun showToast(
        message: String,
        type: ToastType = ToastType.INFO,
        durationMs: Long = 2000L,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null,
    ) {
        current.value = ToastMessage(message, type, durationMs, actionLabel, onAction)
    }

    fun showInfoToast(message: String, durationMs: Long = 2000L) {
        showToast(message, ToastType.INFO, durationMs)
    }

    fun showSuccessToast(message: String, durationMs: Long = 2000L) {
        showToast(message, ToastType.SUCCESS, durationMs)
    }

    fun showWarningToast(message: String, durationMs: Long = 2000L) {
        showToast(message, ToastType.WARNING, durationMs)
    }

    fun showErrorToast(message: String, durationMs: Long = 2000L) {
        showToast(message, ToastType.ERROR, durationMs)
    }
}

