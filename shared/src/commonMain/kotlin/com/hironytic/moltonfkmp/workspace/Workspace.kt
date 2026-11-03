package com.hironytic.moltonfkmp.workspace

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Workspace(
    val id: String,
    val name: String,
    val storyId: Long,
    val currentDay: Int,
    val dayProgress: Int? = null,
    /** Index of the furthest list item seen on the day `dayProgress`; meaningless for other days. */
    val readPosition: Int = 0,
    val playerCharacter: String,
    val lastModified: Instant,
)
