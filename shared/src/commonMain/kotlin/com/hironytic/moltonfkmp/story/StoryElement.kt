package com.hironytic.moltonfkmp.story

import kotlinx.serialization.Serializable

@Serializable
sealed interface StoryElement {
    val elementId: String
}
