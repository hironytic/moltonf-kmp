package com.hironytic.moltonfkmp.navigation

import kotlinx.serialization.Serializable

sealed interface Route {
    @Serializable
    data object SelectWorkspace : Route

    @Serializable
    data object NewWorkspace : Route

    /** Nested navigation graph for the Watching feature; carries the workspace to load. */
    @Serializable
    data class Watching(val workspaceId: String) : Route

    /** Start destination of the [Watching] graph: the day-by-day log view. */
    @Serializable
    data object WatchingHome : Route

    /** Full-screen destination of the [Watching] graph: the talk thread opened from a link tap. */
    @Serializable
    data object TalkThread : Route
}
