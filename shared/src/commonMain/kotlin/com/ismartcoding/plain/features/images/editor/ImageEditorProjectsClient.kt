package com.ismartcoding.plain.features.images.editor

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import kotlinx.serialization.Serializable
import kotlin.time.Instant
import com.ismartcoding.plain.lib.ID
import com.ismartcoding.plain.models.ImageEditorProject
import com.ismartcoding.plain.lib.JsonHelper
import kotlinx.serialization.json.*

object ImageEditorProjectsClient {
    suspend fun listAsync(): List<ImageEditorProjectSummary> =
        RustGraphqlApi.graphql(Query.imageEditorProjects())
            .getValue("imageEditorProjects").let { JsonHelper.jsonDecodeFromElement<List<ProjectSummary>>(it) }.map { row ->
                ImageEditorProjectSummary(ID(row.id), row.thumbnail, row.canvasWidth, row.canvasHeight, row.layerCount, row.updatedAt)
            }

    suspend fun getByIdAsync(id: String): ImageEditorProject? =
        RustGraphqlApi.graphql(Query.imageEditorProject(id))
            .getValue("imageEditorProject").takeUnless { it is JsonNull }?.project()

    suspend fun addOrUpdateAsync(id: String, updateItem: ImageEditorProject.() -> Unit): ImageEditorProject {
        val project = (if (id.isEmpty()) null else getByIdAsync(id)) ?: ImageEditorProject(id)
        project.updateItem()
        return RustGraphqlApi.graphql(Mutation.saveImageEditorProject(id, project))
            .getValue("saveImageEditorProject").project()
    }

    suspend fun deleteAsync(id: String) {
        RustGraphqlApi.graphql(Mutation.deleteImageEditorProject(id))
    }

    private fun JsonElement.project(): ImageEditorProject = JsonHelper.jsonDecodeFromElement<ImageEditorProject>(this)
}

@Serializable
private data class ProjectSummary(val id: String, val thumbnail: String?, val canvasWidth: Int, val canvasHeight: Int, val layerCount: Int, val updatedAt: Instant)
