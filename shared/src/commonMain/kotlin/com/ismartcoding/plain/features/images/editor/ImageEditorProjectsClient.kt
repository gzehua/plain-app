package com.ismartcoding.plain.features.images.editor

import com.ismartcoding.plain.util.ID
import com.ismartcoding.plain.features.images.editor.ImageEditorProjectSummary
import com.ismartcoding.plain.api.graphql.models.ImageEditorProject
import com.ismartcoding.plain.lib.JsonHelper
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
import com.ismartcoding.plain.rust.request.*
import kotlinx.serialization.json.*

object ImageEditorProjectsClient {
    private const val SUMMARY_FIELDS = "id thumbnail canvasWidth canvasHeight layerCount updatedAt"
    private const val FIELDS = "$SUMMARY_FIELDS stateB64 createdAt"

    suspend fun listAsync(): List<ImageEditorProjectSummary> =
        RustContentApi.query("imageEditorProjects { $SUMMARY_FIELDS }")
            .getValue("imageEditorProjects").jsonArray.map { value ->
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
