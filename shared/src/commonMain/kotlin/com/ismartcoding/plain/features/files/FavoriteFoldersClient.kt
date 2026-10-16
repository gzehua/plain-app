package com.ismartcoding.plain.features.files

import com.ismartcoding.plain.api.graphql.models.FavoriteFolder
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.PlainRust
import com.ismartcoding.plain.rust.bridge.*
import com.ismartcoding.plain.features.feeds.*
import com.ismartcoding.plain.ui.feeds.*
import com.ismartcoding.plain.features.files.*
import com.ismartcoding.plain.ui.files.*
import com.ismartcoding.plain.features.notes.*
import com.ismartcoding.plain.ui.notes.*
import com.ismartcoding.plain.features.tags.*
import com.ismartcoding.plain.ui.tags.*
import com.ismartcoding.plain.rust.request.*
import com.ismartcoding.plain.rust.event.*
import com.ismartcoding.plain.rust.host.*
import com.ismartcoding.plain.rust.http.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.*

object FavoriteFoldersClient {
    private const val fields = "rootPath fullPath alias"
    private val lock = Mutex()
    private val state = MutableStateFlow<List<FavoriteFolder>>(emptyList())
    val items = state.asStateFlow()
    suspend fun refresh(): List<FavoriteFolder> = lock.withLock {
        apply(PlainRust.mainQuery("favoriteFolders { $fields }").getValue("favoriteFolders"))
    }
    suspend fun add(rootPath: String, fullPath: String): List<FavoriteFolder> = mutate("addFavoriteFolder(rootPath: ${gql(rootPath)}, fullPath: ${gql(fullPath)})","addFavoriteFolder")
    suspend fun remove(fullPath: String): List<FavoriteFolder> = mutate("removeFavoriteFolder(fullPath: ${gql(fullPath)})","removeFavoriteFolder")
    suspend fun rename(fullPath: String, alias: String): List<FavoriteFolder> = mutate("setFavoriteFolderAlias(fullPath: ${gql(fullPath)}, alias: ${gql(alias)})","setFavoriteFolderAlias")
    private suspend fun mutate(selection: String, key: String): List<FavoriteFolder> = lock.withLock {
        apply(PlainRust.mainMutate("$selection { $fields }").getValue(key))
    }
    private fun apply(value: JsonElement): List<FavoriteFolder> =
        JsonHelper.jsonDecodeFromElement<List<FavoriteFolder>>(value).also { state.value = it }
}
