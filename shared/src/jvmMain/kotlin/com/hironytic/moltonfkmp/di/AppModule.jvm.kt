package com.hironytic.moltonfkmp.di

import com.hironytic.moltonfkmp.storage.DatabaseDriverFactory
import java.io.File
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformModule: Module = module {
    single {
        val databaseFile = File(File(System.getProperty("user.home"), ".moltonf"), "moltonf.db")
        val isNewDatabase = !databaseFile.exists()
        databaseFile.parentFile.mkdirs()
        DatabaseDriverFactory(
            jdbcUrl = "jdbc:sqlite:${databaseFile.absolutePath}",
            isNewDatabase = isNewDatabase,
        )
    }
}
