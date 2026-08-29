package com.hironytic.moltonfkmp.storage

import app.cash.sqldelight.db.SqlDriver

expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}

fun createMoltonfDatabase(driver: SqlDriver): MoltonfDatabase =
    MoltonfDatabase(
        driver = driver,
        workspaceRecordAdapter = WorkspaceRecord.Adapter(lastModifiedAdapter = InstantColumnAdapter),
    )
