package com.ismartcoding.plain.ui.files

import com.ismartcoding.plain.ui.files.getTextId

import com.ismartcoding.plain.features.files.FileSortBy
import com.ismartcoding.plain.features.files.FileSortBy.*
import com.ismartcoding.plain.i18n.*
import org.jetbrains.compose.resources.StringResource

fun FileSortBy.getTextId(): StringResource {
        return when (this) {
            NAME_ASC -> {
                Res.string.name_asc
            }
            NAME_DESC -> {
                Res.string.name_desc
            }
            DATE_ASC -> {
                Res.string.oldest_date_first
            }
            DATE_DESC -> {
                Res.string.newest_date_first
            }
            SIZE_ASC -> {
                Res.string.smallest_first
            }
            SIZE_DESC -> {
                Res.string.largest_first
            }
            TAKEN_AT_DESC -> {
                Res.string.group_by_taken_at
            }
        }
    }
