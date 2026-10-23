package com.ismartcoding.plain.features.files

import com.ismartcoding.plain.rust.graphql.RustGraphqlApi

object FileLocationClient {
    suspend fun lastPath(): FilePathData = RustGraphqlApi.readSavedValue("last_file_path", FilePathData("", "", ""))
    suspend fun saveLastPath(value: FilePathData) = RustGraphqlApi.writeSavedValue("last_file_path", value)
    suspend fun recentSaveDirs(): List<String> = RustGraphqlApi.readSavedValue("recent_save_dirs", emptyList())
    suspend fun recordSaveDir(path: String) = RustGraphqlApi.writeSavedValue("recent_save_dirs", (listOf(path) + recentSaveDirs().filterNot { it == path }).take(5))
}
