package com.ismartcoding.plain.features.files

import com.ismartcoding.plain.rust.graphql.GraphqlRequest
import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import com.ismartcoding.plain.models.FavoriteFolder
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.*

object FavoriteFoldersClient {
    private val lock = Mutex()
    private val state = MutableStateFlow<List<FavoriteFolder>>(emptyList())
    val items = state.asStateFlow()
    suspend fun refresh(): List<FavoriteFolder> = lock.withLock {
        apply(RustGraphqlApi.graphql(Query.favoriteFolders()).getValue("favoriteFolders"))
    }
    suspend fun add(rootPath: String, fullPath: String): List<FavoriteFolder> = mutate(Mutation.addFavoriteFolder(rootPath, fullPath),"addFavoriteFolder")
    suspend fun remove(fullPath: String): List<FavoriteFolder> = mutate(Mutation.removeFavoriteFolder(fullPath),"removeFavoriteFolder")
    suspend fun rename(fullPath: String, alias: String): List<FavoriteFolder> = mutate(Mutation.setFavoriteFolderAlias(fullPath, alias),"setFavoriteFolderAlias")
    private suspend fun mutate(selection: GraphqlRequest, key: String): List<FavoriteFolder> = lock.withLock {
        apply(RustGraphqlApi.graphql(selection).getValue(key))
    }
    private fun apply(value: JsonElement): List<FavoriteFolder> =
        JsonHelper.jsonDecodeFromElement<List<FavoriteFolder>>(value).also { state.value = it }
}
