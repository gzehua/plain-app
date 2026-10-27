package com.ismartcoding.plain.features.images.editor

import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.lib.ID
import com.ismartcoding.plain.models.ImageEditorProject
import com.ismartcoding.plain.rust.graphql.RustGraphqlApi
import kotlin.time.Instant
import kotlinx.serialization.Serializable

object ImageEditorProjectsClient {
    suspend fun listAsync(): List<ImageEditorProjectSummary> =
        RustGraphqlApi.read<List<ProjectSummary>>(Query.imageEditorProjects()).map { row ->
                ImageEditorProjectSummary(ID(row.id), row.thumbnail, row.canvasWidth, row.canvasHeight, row.layerCount, row.updatedAt)
            }

    suspend fun getByIdAsync(id: String): ImageEditorProject? =
        RustGraphqlApi.read(Query.imageEditorProject(id))

    suspend fun addOrUpdateAsync(id: String, updateItem: ImageEditorProject.() -> Unit): ImageEditorProject {
        val project = (if (id.isEmpty()) null else getByIdAsync(id)) ?: ImageEditorProject(id)
        project.updateItem()
        return RustGraphqlApi.read(Mutation.saveImageEditorProject(id, project))
    }

    suspend fun deleteAsync(id: String) {
        RustGraphqlApi.graphql(Mutation.deleteImageEditorProject(id))
    }
}

@Serializable
private data class ProjectSummary(
    val id: String,
    val thumbnail: String?,
    val canvasWidth: Int,
    val canvasHeight: Int,
    val layerCount: Int,
    val updatedAt: Instant,
)
