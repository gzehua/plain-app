package com.ismartcoding.plain.features

import com.ismartcoding.plain.data.ID
import com.ismartcoding.plain.features.imageeditor.ImageEditorProjectSummary
import com.ismartcoding.plain.graphql.models.ImageEditorProject
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.rust.bridge.*
import com.ismartcoding.plain.rust.content.*
import com.ismartcoding.plain.rust.event.*
import com.ismartcoding.plain.rust.host.*
import com.ismartcoding.plain.rust.http.*
import com.ismartcoding.plain.rust.request.*
import kotlinx.serialization.json.*

object ImageEditorProjectHelper {
    private const val SUMMARY_FIELDS = "id thumbnail canvasWidth canvasHeight layerCount updatedAt"
    private const val FIELDS = "$SUMMARY_FIELDS stateB64 createdAt"

    suspend fun listAsync(limit: Int): List<ImageEditorProjectSummary> =
        RustContentApi.query("imageEditorProjectItems(offset: 0, limit: $limit, query: \"\") { $SUMMARY_FIELDS }")
            .getValue("imageEditorProjectItems").jsonArray.map { value ->
                val row = value.jsonObject
                ImageEditorProjectSummary(ID(row.string("id")), row["thumbnail"]?.jsonPrimitive?.contentOrNull,
                    row.getValue("canvasWidth").jsonPrimitive.int, row.getValue("canvasHeight").jsonPrimitive.int,
                    row.getValue("layerCount").jsonPrimitive.int, row.instant("updatedAt"))
            }

    suspend fun getByIdAsync(id: String): ImageEditorProject? =
        RustContentApi.query("imageEditorProject(id: ${gql(id)}) { $FIELDS }")
            .getValue("imageEditorProject").takeUnless { it is JsonNull }?.project()

    suspend fun addOrUpdateAsync(id: String, updateItem: ImageEditorProject.() -> Unit): ImageEditorProject {
        val project = (if (id.isEmpty()) null else getByIdAsync(id)) ?: ImageEditorProject(id)
        project.updateItem()
        val thumbnail = project.thumbnail?.let(::gql) ?: "null"
        val input = "{ stateB64: ${gql(project.stateB64)}, thumbnail: $thumbnail, canvasWidth: ${project.canvasWidth}, canvasHeight: ${project.canvasHeight}, layerCount: ${project.layerCount} }"
        return RustContentApi.mutate("saveImageEditorProject(id: ${gql(id)}, input: $input) { $FIELDS }")
            .getValue("saveImageEditorProject").project()
    }

    suspend fun deleteAsync(id: String) {
        RustContentApi.mutate("deleteImageEditorProject(id: ${gql(id)})")
    }

    suspend fun broadcastUpdate(pid: String, updateB64: String) {
        RustContentApi.mutate("broadcastImageEditorUpdate(id: ${gql(pid)}, update: ${gql(updateB64)})")
    }

    private fun JsonElement.project(): ImageEditorProject = JsonHelper.jsonDecodeFromElement<ImageEditorProject>(this)
}
