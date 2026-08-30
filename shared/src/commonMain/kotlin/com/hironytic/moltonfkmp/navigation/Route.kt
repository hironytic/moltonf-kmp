package com.hironytic.moltonfkmp.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object SelectWorkspace : Route

    @Serializable
    data object NewWorkspace : Route

    @Serializable
    data class Watching(val workspaceId: String) : Route
}
