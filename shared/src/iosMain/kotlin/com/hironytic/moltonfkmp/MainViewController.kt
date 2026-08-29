package com.hironytic.moltonfkmp

import androidx.compose.ui.window.ComposeUIViewController
import com.hironytic.moltonfkmp.storage.DatabaseDriverFactory
import com.hironytic.moltonfkmp.storage.WorkspaceStore
import com.hironytic.moltonfkmp.storage.createMoltonfDatabase

private val workspaceStore: WorkspaceStore =
    WorkspaceStore(createMoltonfDatabase(DatabaseDriverFactory().createDriver()))

fun MainViewController() = ComposeUIViewController { App() }