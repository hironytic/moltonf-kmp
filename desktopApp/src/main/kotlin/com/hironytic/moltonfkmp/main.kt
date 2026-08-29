package com.hironytic.moltonfkmp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.hironytic.moltonfkmp.storage.DatabaseDriverFactory
import com.hironytic.moltonfkmp.storage.WorkspaceStore
import com.hironytic.moltonfkmp.storage.createMoltonfDatabase
import java.io.File

fun main() {
    val databaseFile = File(File(System.getProperty("user.home"), ".moltonf"), "moltonf.db")
    databaseFile.parentFile.mkdirs()
    val workspaceStore = WorkspaceStore(
        createMoltonfDatabase(DatabaseDriverFactory("jdbc:sqlite:${databaseFile.absolutePath}").createDriver())
    )

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "moltonf-kmp",
        ) {
            App()
        }
    }
}