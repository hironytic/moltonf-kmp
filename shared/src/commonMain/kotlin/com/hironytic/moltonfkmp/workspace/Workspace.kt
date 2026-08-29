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
    val playerCharacter: String,
    val lastModified: Instant,
)
