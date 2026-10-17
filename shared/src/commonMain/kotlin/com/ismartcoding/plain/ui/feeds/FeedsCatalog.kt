package com.ismartcoding.plain.ui.feeds

import com.ismartcoding.plain.i18n.Res
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.lib.withIO
import kotlinx.serialization.Serializable

@Serializable
data class CatalogFeed(
    val name: String,
    val url: String,
    val site: String,
)

@Serializable
data class CatalogCategory(
    val id: String,
    val feeds: List<CatalogFeed>,
)

object CatalogCategoryIds {
    const val TECH_CN = "tech_cn"
    const val DEV_CN = "dev_cn"
    const val TECH_EN = "tech_en"
    const val NEWS_EN = "news_en"
    const val READS = "reads"
}

fun parseCatalog(json: String): List<CatalogCategory> =
    JsonHelper.jsonDecode<List<CatalogCategory>>(json).map {
        it.copy(feeds = it.feeds.distinctBy { feed -> feed.url })
    }

object FeedsCatalog {
    private const val FILE = "files/feeds_catalog.json"

    suspend fun loadAsync(): List<CatalogCategory> =
        withIO { parseCatalog(Res.readBytes(FILE).decodeToString()) }
}
