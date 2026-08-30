package com.hironytic.moltonfkmp

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hironytic.moltonfkmp.navigation.Route
import com.hironytic.moltonfkmp.ui.NotYetImplementedScreen
import com.hironytic.moltonfkmp.ui.selectworkspace.SelectWorkspaceScreen

@Composable
@Preview
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = Route.SelectWorkspace) {
            composable<Route.SelectWorkspace> {
                SelectWorkspaceScreen(
                    onCreateNewWorkspace = { navController.navigate(Route.NewWorkspace) },
                    onOpenWorkspace = { workspaceId -> navController.navigate(Route.Watching(workspaceId)) },
                )
            }
            composable<Route.NewWorkspace> {
                NotYetImplementedScreen(
                    title = "新規観戦データ作成",
                    onBack = { navController.popBackStack() },
                )
            }
            composable<Route.Watching> {
                NotYetImplementedScreen(
                    title = "観戦画面",
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
