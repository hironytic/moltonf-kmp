package com.hironytic.moltonfkmp.storage

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.hironytic.moltonfkmp.story.Avatar
import com.hironytic.moltonfkmp.story.Story
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant
import kotlinx.coroutines.runBlocking

private fun sampleStory(fullName: String) = Story(
    villageFullName = fullName,
    baseURI = "https://example.com/",
    landId = "wolfg",
    graveIconURI = "img/grave.png",
    periods = emptyList(),
    avatarList = listOf(Avatar(avatarId = "gerd", fullName = "楽天家 ゲルト", shortName = "ゲルト")),
)

private fun newTestDatabase(): MoltonfDatabase {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    MoltonfDatabase.Schema.create(driver)
    return createMoltonfDatabase(driver)
}

class WorkspaceStoreTest {
    @Test
    fun addAndGetWorkspaceAndStory() = runBlocking {
        val store = WorkspaceStore(newTestDatabase())
        val story = sampleStory("G1 テストの村")
        val workspace = store.add(
            story,
            NewWorkspace(
                name = "workspace1",
                currentDay = 1,
                dayProgress = 1,
                playerCharacter = "gerd",
                lastModified = Instant.fromEpochMilliseconds(1_000),
            ),
        )

        assertEquals("workspace1", workspace.name)
        assertEquals(1, workspace.currentDay)
        assertEquals(1, workspace.dayProgress)
        assertEquals("gerd", workspace.playerCharacter)
        assertEquals(Instant.fromEpochMilliseconds(1_000), workspace.lastModified)

        assertEquals(workspace, store.getWorkspace(workspace.id))
        assertEquals(story, store.getStory(workspace.storyId))
    }

    @Test
    fun getWorkspacesOrdersByLastModifiedDescending() = runBlocking {
        val store = WorkspaceStore(newTestDatabase())
        val older = store.add(
            sampleStory("G1"),
            NewWorkspace("older", 0, null, "gerd", Instant.fromEpochMilliseconds(1_000)),
        )
        val newer = store.add(
            sampleStory("G2"),
            NewWorkspace("newer", 0, null, "gerd", Instant.fromEpochMilliseconds(2_000)),
        )

        val workspaces = store.getWorkspaces()
        assertEquals(listOf(newer.id, older.id), workspaces.map { it.id })
    }

    @Test
    fun updateWorkspacePersistsChanges() = runBlocking {
        val store = WorkspaceStore(newTestDatabase())
        val workspace = store.add(
            sampleStory("G1"),
            NewWorkspace("name", 0, null, "gerd", Instant.fromEpochMilliseconds(1_000)),
        )

        val updated = workspace.copy(
            currentDay = 5,
            dayProgress = null,
            lastModified = Instant.fromEpochMilliseconds(2_000),
        )
        store.update(updated)

        assertEquals(updated, store.getWorkspace(workspace.id))
    }

    @Test
    fun removeDeletesWorkspaceAndStory() = runBlocking {
        val store = WorkspaceStore(newTestDatabase())
        val workspace = store.add(
            sampleStory("G1"),
            NewWorkspace("name", 0, null, "gerd", Instant.fromEpochMilliseconds(1_000)),
        )

        store.remove(workspace)

        assertNull(store.getWorkspace(workspace.id))
        assertNull(store.getStory(workspace.storyId))
    }
}
