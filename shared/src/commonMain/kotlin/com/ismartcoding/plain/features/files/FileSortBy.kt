package com.ismartcoding.plain.features.files



enum class FileSortBy {
    // TAKEN_AT has no ASC variant on purpose: it exists for the "group by taken
    // date" photo view, which always shows newest-day first. Plain date/size/
    // name sorts are the general-purpose symmetric pair.
    DATE_ASC,
    DATE_DESC,
    SIZE_ASC,
    SIZE_DESC,
    NAME_ASC,
    NAME_DESC,
    TAKEN_AT_DESC,
    ;

}
