package com.ismartcoding.plain.ui.components

class ConfirmDialog(
    val title: String,
    val message: String,
    val confirmButton: Pair<String, () -> Unit>,
    val dismissButton: Pair<String, () -> Unit>?,
    val danger: Boolean = false,
)
