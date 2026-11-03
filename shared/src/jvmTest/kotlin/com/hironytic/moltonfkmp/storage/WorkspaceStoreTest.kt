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
                readPosition = 7,
                playerCharacter = "gerd",
                lastModified = Instant.fromEpochMilliseconds(1_000),
            ),
        )

        assertEquals("workspace1", workspace.name)
        assertEquals(1, workspace.currentDay)
        assertEquals(1, workspace.dayProgress)
        assertEquals(7, workspace.readPosition)
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

class DatabaseMigrationTest {
    @Test
    fun migratesLegacyDatabaseWithoutUserVersion() {
        val file = kotlin.io.path.createTempFile("moltonf-migration", ".db").toFile()
        try {
            val url = "jdbc:sqlite:${file.absolutePath}"
            // Version 1 schema (before readPosition existed), with no user_version set.
            val legacy = JdbcSqliteDriver(url)
            legacy.execute(
                null,
                "CREATE TABLE storyRecord (id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, json TEXT NOT NULL)",
                0,
            )
            legacy.execute(
                null,
                "CREATE TABLE workspaceRecord (id TEXT NOT NULL PRIMARY KEY, name TEXT NOT NULL, " +
                    "storyId INTEGER NOT NULL, currentDay INTEGER NOT NULL, dayProgress INTEGER, " +
                    "playerCharacter TEXT NOT NULL, lastModified INTEGER NOT NULL)",
                0,
            )
            legacy.execute(null, "INSERT INTO workspaceRecord VALUES ('w1', 'n', 1, 2, 2, 'gerd', 1000)", 0)
            legacy.close()

            val driver = DatabaseDriverFactory(jdbcUrl = url, isNewDatabase = false).createDriver()
            val workspace = runBlocking { WorkspaceStore(createMoltonfDatabase(driver)).getWorkspace("w1") }
            assertEquals(0, workspace?.readPosition)
            assertEquals(2, workspace?.dayProgress)
            driver.close()
        } finally {
            file.delete()
        }
    }
}
