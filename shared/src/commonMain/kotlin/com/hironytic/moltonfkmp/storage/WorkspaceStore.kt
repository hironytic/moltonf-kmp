package com.hironytic.moltonfkmp.storage

import com.hironytic.moltonfkmp.story.Story
import com.hironytic.moltonfkmp.workspace.Workspace
import kotlin.time.Instant
import kotlin.uuid.Uuid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

/** The fields needed to create a new [Workspace]; `id` and `storyId` are generated on [WorkspaceStore.add]. */
data class NewWorkspace(
    val name: String,
    val currentDay: Int,
    val dayProgress: Int? = null,
    val playerCharacter: String,
    val lastModified: Instant,
)

/**
 * Storage for workspaces and the stories they refer to, backed by SQLDelight.
 */
class WorkspaceStore(
    private val database: MoltonfDatabase,
    private val json: Json = Json,
) {
    suspend fun add(story: Story, workspace: NewWorkspace): Workspace = withContext(Dispatchers.Default) {
        database.transactionWithResult {
            database.storyQueries.insertStory(json.encodeToString(story))
            val storyId = database.storyQueries.lastInsertRowId().executeAsOne()
            val id = Uuid.random().toString()
            database.workspaceQueries.insertWorkspace(
                id = id,
                name = workspace.name,
                storyId = storyId,
                currentDay = workspace.currentDay.toLong(),
                dayProgress = workspace.dayProgress?.toLong(),
                playerCharacter = workspace.playerCharacter,
                lastModified = workspace.lastModified,
            )
            Workspace(
                id = id,
                name = workspace.name,
                storyId = storyId,
                currentDay = workspace.currentDay,
                dayProgress = workspace.dayProgress,
                playerCharacter = workspace.playerCharacter,
                lastModified = workspace.lastModified,
            )
        }
    }

    suspend fun update(workspace: Workspace) = withContext(Dispatchers.Default) {
        database.workspaceQueries.updateWorkspace(
            name = workspace.name,
            storyId = workspace.storyId,
            currentDay = workspace.currentDay.toLong(),
            dayProgress = workspace.dayProgress?.toLong(),
            playerCharacter = workspace.playerCharacter,
            lastModified = workspace.lastModified,
            id = workspace.id,
        )
    }

    suspend fun remove(workspace: Workspace) = withContext(Dispatchers.Default) {
        database.transaction {
            database.workspaceQueries.deleteWorkspaceById(workspace.id)
            database.storyQueries.deleteStoryById(workspace.storyId)
        }
    }

    suspend fun getWorkspaces(): List<Workspace> = withContext(Dispatchers.Default) {
        database.workspaceQueries.selectWorkspacesOrderByLastModifiedDesc()
            .executeAsList()
            .map { it.toWorkspace() }
    }

    suspend fun getWorkspace(id: String): Workspace? = withContext(Dispatchers.Default) {
        database.workspaceQueries.selectWorkspaceById(id).executeAsOneOrNull()?.toWorkspace()
    }

    suspend fun getStory(id: Long): Story? = withContext(Dispatchers.Default) {
        database.storyQueries.selectStoryById(id).executeAsOneOrNull()?.let {
            json.decodeFromString<Story>(it.json)
        }
    }

    private fun WorkspaceRecord.toWorkspace() = Workspace(
        id = id,
        name = name,
        storyId = storyId,
        currentDay = currentDay.toInt(),
        dayProgress = dayProgress?.toInt(),
        playerCharacter = playerCharacter,
        lastModified = lastModified,
    )
}
