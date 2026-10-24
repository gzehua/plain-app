package com.ismartcoding.plain.ui.feeds

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FeedsCatalogTest {
    private val sampleJson =
        """
        [
          {"id":"tech_cn","feeds":[
            {"name":"First feed","url":"https://example.org/first","site":"example.org"},
            {"name":"Second feed","url":"https://example.org/second","site":"example.org"}
          ]},
          {"id":"news_en","feeds":[
            {"name":"News feed","url":"https://example.org/news","site":"example.org"},
            {"name":"Duplicate feed","url":"https://example.org/news","site":"example.org"}
          ]}
        ]
        """.trimIndent()

    @Test
    fun parses_categories_and_feeds() {
        val categories = parseCatalog(sampleJson)
        assertEquals(2, categories.size)
        assertEquals("tech_cn", categories[0].id)
        assertEquals(2, categories[0].feeds.size)
        assertEquals("First feed", categories[0].feeds[0].name)
        assertEquals("example.org", categories[0].feeds[0].site)
    }

    @Test
    fun drops_duplicate_urls_across_rows() {
        val categories = parseCatalog(sampleJson)
        assertEquals(1, categories[1].feeds.size)
        assertEquals("News feed", categories[1].feeds[0].name)
    }

    @Test
    fun all_category_ids_are_known() {
        val known =
            setOf(
                CatalogCategoryIds.TECH_CN,
                CatalogCategoryIds.DEV_CN,
                CatalogCategoryIds.TECH_EN,
                CatalogCategoryIds.NEWS_EN,
                CatalogCategoryIds.READS,
            )
        parseCatalog(sampleJson).forEach {
            assertTrue(it.id in known, "unknown category id: " + it.id)
        }
    }
}
