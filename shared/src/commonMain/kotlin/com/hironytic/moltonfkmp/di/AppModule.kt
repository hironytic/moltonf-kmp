package com.hironytic.moltonfkmp.di

import com.hironytic.moltonfkmp.storage.DatabaseDriverFactory
import com.hironytic.moltonfkmp.storage.WorkspaceStore
import com.hironytic.moltonfkmp.storage.createMoltonfDatabase
import com.hironytic.moltonfkmp.ui.newworkspace.NewWorkspaceViewModel
import com.hironytic.moltonfkmp.ui.selectworkspace.SelectWorkspaceViewModel
import com.hironytic.moltonfkmp.ui.watching.WatchingViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

internal val commonModule: Module = module {
    single { createMoltonfDatabase(get<DatabaseDriverFactory>().createDriver()) }
    single { WorkspaceStore(get()) }
    viewModelOf(::SelectWorkspaceViewModel)
    viewModelOf(::NewWorkspaceViewModel)
    viewModel { (workspaceId: String) -> WatchingViewModel(get(), workspaceId) }
}

internal expect val platformModule: Module
