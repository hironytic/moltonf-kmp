package com.hironytic.moltonfkmp.storage

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

actual class DatabaseDriverFactory(
    private val jdbcUrl: String = "jdbc:sqlite:moltonf.db",
    private val isNewDatabase: Boolean = true,
) {
    actual fun createDriver(): SqlDriver {
        val driver: SqlDriver = JdbcSqliteDriver(jdbcUrl)
        val latestVersion = MoltonfDatabase.Schema.version
        if (isNewDatabase) {
            MoltonfDatabase.Schema.create(driver)
        } else {
            // Databases created before versioning was introduced have user_version 0 and are version 1.
            val currentVersion = readUserVersion(driver).coerceAtLeast(1)
            if (currentVersion < latestVersion) {
                MoltonfDatabase.Schema.migrate(driver, currentVersion, latestVersion)
            }
        }
        writeUserVersion(driver, latestVersion)
        return driver
    }

    private fun readUserVersion(driver: SqlDriver): Long =
        driver.executeQuery(
            identifier = null,
            sql = "PRAGMA user_version",
            mapper = { cursor ->
                QueryResult.Value(if (cursor.next().value) cursor.getLong(0) ?: 0L else 0L)
            },
            parameters = 0,
        ).value

    private fun writeUserVersion(driver: SqlDriver, version: Long) {
        driver.execute(null, "PRAGMA user_version = $version", 0)
    }
}
