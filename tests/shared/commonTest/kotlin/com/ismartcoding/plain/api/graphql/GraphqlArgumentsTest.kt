package com.ismartcoding.plain.api.graphql

import com.ismartcoding.plain.features.dlna.sender.CastCommand
import com.ismartcoding.plain.features.dlna.sender.CastItem
import com.ismartcoding.plain.features.files.FileTaskOp
import com.ismartcoding.plain.features.files.FileTaskType
import com.ismartcoding.plain.features.media.actions.MediaAction
import com.ismartcoding.plain.graphql.Mutation
import com.ismartcoding.plain.graphql.Query
import com.ismartcoding.plain.lib.JsonHelper
import com.ismartcoding.plain.platform.system.DataType
import com.ismartcoding.plain.models.ImageEditorProject
import kotlin.time.Instant
import kotlinx.serialization.json.*
import kotlin.test.*

class GraphqlArgumentsTest {
    @Test
    fun valuesRemainSeparateFromTheDocumentAndRoundTripThroughTheEnvelope() {
        val input = "a\"b\\c\n) { deleteNotes(query: \"all:true\") }"
        val request = Query.chatCount(input)
        assertEquals(Query.chatCount("ordinary").query, request.query)
        assertFalse(request.query.contains(input))
        assertTrue(request.query.contains("chatCount(query: \$query)"))
        val envelope = JsonHelper.jsonDecode<JsonObject>(JsonHelper.jsonEncode(request))
        assertEquals(input, envelope.getValue("variables").jsonObject.getValue("query").jsonPrimitive.content)
        assertEquals(request.query, envelope.getValue("query").jsonPrimitive.content)
        val paths = listOf("a\"", "b\\c", "line\nbreak")
        assertEquals(paths, Mutation.deleteFiles(paths).variables.getValue("paths").jsonArray.map { it.jsonPrimitive.content })
    }

    @Test
    fun optionalUpdateArgumentsAreOmittedAndFalseIsRetained() {
        assertEquals(setOf("id"), Mutation.updateFeed("id", null, null, null).variables.keys)
        val variables = Mutation.updateFeed("id", "", null, false).variables
        assertEquals(JsonPrimitive(""), variables["name"])
        assertEquals(JsonPrimitive(false), variables["fetchContent"])
        assertFalse("url" in variables)
        assertEquals(JsonNull, Mutation.syncFeeds("").variables["id"])
    }

    @Test
    fun nullAndEmptyRecipientsRemainDistinct() {
        assertEquals(JsonNull, Mutation.deliverChatItem("id", null).variables["recipients"])
        assertEquals(JsonArray(emptyList()), Mutation.deliverChatItem("id", emptyList()).variables["recipients"])
    }

    @Test
    fun batchAliasesAndVariablesStayUniqueWhenIdsRepeat() {
        val request = Query.chatSnapshot(listOf("a\"", "a\""))
        assertTrue(request.query.contains("message0:chatItem(id: \$id0)"))
        assertTrue(request.query.contains("message1:chatItem(id: \$id1)"))
        assertEquals(mapOf("id0" to JsonPrimitive("a\""), "id1" to JsonPrimitive("a\"")), request.variables)
    }

    @Test
    fun onlyMoveIncludesDestination() {
        for (action in MediaAction.entries) {
            val request = Mutation.mediaAction(action, "IMAGE", "ids:=1", "a\"b")
            assertEquals(action == MediaAction.MOVE, "destDir" in request.variables)
            if (action == MediaAction.MOVE) assertEquals(JsonPrimitive("a\"b"), request.variables["destDir"])
        }
    }

    @Test
    fun dlnaCommandsBindTypedInputsAndKeepResponseMetadataOffTheWire() {
        val item = CastItem("a\"b", "title", durationMs = 4_000, audio = true)
        val commands = listOf(
            CastCommand.StartScan to "startDlnaScan",
            CastCommand.StopScan to "stopDlnaScan",
            CastCommand.Select("id") to "selectDlnaDevice",
            CastCommand.Cast(item) to "castDlnaMedia",
            CastCommand.Play to "playDlna",
            CastCommand.Pause to "pauseDlna",
            CastCommand.Seek(42) to "seekDlna",
            CastCommand.Stop to "stopDlna",
            CastCommand.Exit to "exitDlna",
            CastCommand.Add(item) to "addDlnaItem",
            CastCommand.Remove("path") to "removeDlnaItem",
            CastCommand.RemoveAt(1) to "removeDlnaItemAt",
            CastCommand.Reorder(1, 2) to "reorderDlnaItems",
            CastCommand.Clear to "clearDlnaItems",
        )
        for ((command, expected) in commands) {
            val request = Mutation.dlna(command)
            assertEquals(expected, request.field)
            assertEquals(expected, request.query.substringAfter('{').trimStart().substringBefore('(').substringBefore(' '))
            assertFalse("field" in JsonHelper.jsonDecode<JsonObject>(JsonHelper.jsonEncode(request)))
        }
        val input = Mutation.dlna(CastCommand.Cast(item)).variables.getValue("item").jsonObject
        assertEquals(JsonPrimitive(item.path), input["path"])
        assertEquals(JsonPrimitive(4_000L), input["durationMs"])
        assertEquals(JsonPrimitive(true), input["audio"])
    }

    @Test
    fun mediaSelectionsKeepTypeSpecificFields() {
        assertTrue(Query.mediaRows(DataType.AUDIO, "", 10, 0, "NAME_ASC").query.contains("albumFileId"))
        assertTrue(Query.mediaRows(DataType.IMAGE, "", 10, 0, "NAME_ASC").query.contains("takenAt"))
        assertFalse(Query.mediaRows(DataType.DOC, "", 10, 0, "NAME_ASC").query.contains("isFavorite"))
        assertTrue(Query.mediaCount(DataType.VIDEO, "").query.contains("videoCount(query: \$query)"))
    }

    @Test
    fun variableRequestsOwnNamesAndPreserveDefaultInputFields() {
        val path = "a\"b\\c\n"
        assertEquals(mapOf("path" to JsonPrimitive(path), "name" to JsonPrimitive("audio.mp3")), Query.audioDuration(path).variables)
        assertEquals(mapOf("path" to JsonPrimitive(path)), Mutation.cacheAudioCover(path).variables)
        val request = Mutation.createFileTask(FileTaskType.COPY, listOf(FileTaskOp("a", "b")))
        assertEquals(JsonPrimitive(false), request.variables.getValue("ops").jsonArray.single().jsonObject["overwrite"])
        assertEquals(setOf("type", "title", "ops"), request.variables.keys)
    }

    @Test
    fun structuredInputsExcludeOutputFieldsAndPreserveDatesAndNulls() {
        val created = Mutation.saveNote("", "title", "body")
        assertEquals("createNote", created.field)
        assertEquals(setOf("input"), created.variables.keys)
        assertEquals(setOf("title", "content"), created.variables.getValue("input").jsonObject.keys)
        val edited = Mutation.saveNote("id", "", "body")
        assertEquals("updateNote", edited.field)
        assertEquals(JsonPrimitive("id"), edited.variables["id"])
        val project = Mutation.saveImageEditorProject("id", ImageEditorProject("id")).variables.getValue("input").jsonObject
        assertEquals(setOf("stateB64", "thumbnail", "canvasWidth", "canvasHeight", "layerCount"), project.keys)
        assertEquals(JsonNull, project["thumbnail"])
        val expiresAt = Instant.parse("2026-01-01T00:00:00.123456789Z")
        assertEquals(JsonPrimitive(expiresAt.toString()), Mutation.createShare("share", listOf("path"), true, expiresAt).variables["expiresAt"])
        assertEquals(JsonNull, Mutation.updateShare("id", "name", null, null).variables["realPaths"])
        assertEquals(JsonNull, Query.shareLink("id", null).variables["host"])
    }
}
