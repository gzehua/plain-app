package com.ismartcoding.plain.features.files

import com.ismartcoding.plain.features.settings.SavedValuesClient

object FileLocationClient {
    suspend fun lastPath(): FilePathData = SavedValuesClient.get("last_file_path", FilePathData("", "", ""))

    suspend fun saveLastPath(value: FilePathData) = SavedValuesClient.set("last_file_path", value)

    suspend fun recentSaveDirs(): List<String> = SavedValuesClient.get("recent_save_dirs", emptyList())

    suspend fun recordSaveDir(path: String) = SavedValuesClient.set("recent_save_dirs", (listOf(path) + recentSaveDirs().filterNot { it == path }).take(5))
}
