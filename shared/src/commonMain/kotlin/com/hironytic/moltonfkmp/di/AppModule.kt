package com.hironytic.moltonfkmp.di

import com.hironytic.moltonfkmp.storage.DatabaseDriverFactory
import com.hironytic.moltonfkmp.storage.WorkspaceStore
import com.hironytic.moltonfkmp.storage.createMoltonfDatabase
import com.hironytic.moltonfkmp.ui.newworkspace.NewWorkspaceViewModel
import com.hironytic.moltonfkmp.ui.selectworkspace.SelectWorkspaceViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val commonModule: Module = module {
    single { createMoltonfDatabase(get<DatabaseDriverFactory>().createDriver()) }
    single { WorkspaceStore(get()) }
    viewModelOf(::SelectWorkspaceViewModel)
    viewModelOf(::NewWorkspaceViewModel)
}

internal expect val platformModule: Module
