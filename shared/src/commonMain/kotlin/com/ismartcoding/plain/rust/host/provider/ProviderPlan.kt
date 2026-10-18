package com.ismartcoding.plain.rust.host.provider

import com.ismartcoding.plain.helpers.ContentWhere
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ProviderPlan(
    val clauses: List<String> = emptyList(),
    val args: List<String> = emptyList(),
    val idsColumn: String? = null,
    val ids: List<String> = emptyList(),
    val trash: Boolean? = null,
) {
    fun where(): ContentWhere = where(ids)

    fun wheres(): List<ContentWhere> =
        if (idsColumn != null && ids.isNotEmpty()) ids.chunked(2000).map(::where) else listOf(where())

    private fun where(selectedIds: List<String>): ContentWhere {
        val where = ContentWhere()
        clauses.forEach { where.add(it) }
        where.args.addAll(args)
        idsColumn?.let { column -> if (selectedIds.isNotEmpty()) where.addIn(column, selectedIds) }
        where.trash = trash
        return where
    }
}

internal fun JsonObject.providerPlan(): ProviderPlan = JsonHelper.jsonDecodeFromElement<ProviderPlanRequest>(this).plan
