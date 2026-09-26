package com.hironytic.moltonfkmp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavBackStackEntry
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import com.hironytic.moltonfkmp.navigation.Route
import com.hironytic.moltonfkmp.ui.newworkspace.NewWorkspaceScreen
import com.hironytic.moltonfkmp.ui.selectworkspace.SelectWorkspaceScreen
import com.hironytic.moltonfkmp.ui.theme.MoltonfTheme
import com.hironytic.moltonfkmp.ui.watching.TalkThreadScreen
import com.hironytic.moltonfkmp.ui.watching.WatchingScreen
import com.hironytic.moltonfkmp.ui.watching.WatchingViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
@Preview
fun App() {
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components { add(KtorNetworkFetcherFactory()) }
            .build()
    }

    MoltonfTheme {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = Route.SelectWorkspace) {
            composable<Route.SelectWorkspace> {
                SelectWorkspaceScreen(
                    onCreateNewWorkspace = { navController.navigate(Route.NewWorkspace) },
                    onOpenWorkspace = { workspaceId -> navController.navigate(Route.Watching(workspaceId)) },
                )
            }
            composable<Route.NewWorkspace> {
                NewWorkspaceScreen(
                    onExit = { navController.popBackStack() },
                    onRegistered = { workspaceId ->
                        navController.navigate(Route.Watching(workspaceId)) {
                            popUpTo(Route.SelectWorkspace)
                        }
                    },
                )
            }
            navigation<Route.Watching>(startDestination = Route.WatchingHome) {
                composable<Route.WatchingHome> { backStackEntry ->
                    val viewModel = watchingViewModel(navController, backStackEntry)
                    WatchingScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onOpenTalkThread = { navController.navigate(Route.TalkThread) },
                    )
                }
                composable<Route.TalkThread> { backStackEntry ->
                    val viewModel = watchingViewModel(navController, backStackEntry)
                    TalkThreadScreen(
                        viewModel = viewModel,
                        onClose = { navController.popBackStack() },
                    )
                }
            }
        }
    }
}

/**
 * Resolves the [WatchingViewModel] scoped to the enclosing `Route.Watching` graph, so the
 * WatchingHome and TalkThread screens share the same instance (and therefore the same loaded
 * story and talk-thread state).
 */
@Composable
private fun watchingViewModel(
    navController: androidx.navigation.NavController,
    backStackEntry: NavBackStackEntry,
): WatchingViewModel {
    val parentEntry = remember(backStackEntry) { navController.getBackStackEntry<Route.Watching>() }
    val workspaceId = parentEntry.toRoute<Route.Watching>().workspaceId
    return koinViewModel(viewModelStoreOwner = parentEntry) { parametersOf(workspaceId) }
}
