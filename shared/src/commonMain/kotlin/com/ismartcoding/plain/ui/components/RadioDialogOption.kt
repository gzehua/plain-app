package com.ismartcoding.plain.ui.components

data class RadioDialogOption(
    val text: String = "",
    val selected: Boolean = false,
    val onClick: () -> Unit = {},
)
