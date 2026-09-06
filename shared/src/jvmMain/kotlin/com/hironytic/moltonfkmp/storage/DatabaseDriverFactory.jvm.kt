package com.hironytic.moltonfkmp.storage

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

actual class DatabaseDriverFactory(
    private val jdbcUrl: String = "jdbc:sqlite:moltonf.db",
    private val isNewDatabase: Boolean = true,
) {
    actual fun createDriver(): SqlDriver {
        val driver: SqlDriver = JdbcSqliteDriver(jdbcUrl)
        if (isNewDatabase) {
            MoltonfDatabase.Schema.create(driver)
        }
        return driver
    }
}
