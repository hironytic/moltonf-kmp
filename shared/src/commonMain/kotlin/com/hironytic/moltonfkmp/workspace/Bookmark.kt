package com.hironytic.moltonfkmp.workspace

import kotlinx.serialization.Serializable

/**
 * A bookmark on a talk, with the user's comment.
 *
 * The talk is identified by the [day] it belongs to and its [elementId] within the story.
 */
@Serializable
data class Bookmark(
    val id: String,
    val day: Int,
    val elementId: String,
    val comment: String = "",
)
