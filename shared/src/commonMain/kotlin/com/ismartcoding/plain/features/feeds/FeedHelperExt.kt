package com.ismartcoding.plain.features.feeds

import com.ismartcoding.plain.rust.request.RustContentApi
import com.ismartcoding.plain.rust.request.gql
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull

suspend fun FeedsClient.importAsync(content: String) { RustContentApi.mutate("importFeeds(content: ${gql(content)})") }
suspend fun FeedsClient.exportAsync(): String = RustContentApi.mutate("exportFeeds").getValue("exportFeeds").jsonPrimitive.content
