package com.hironytic.moltonfkmp.ui.watching

import com.hironytic.moltonfkmp.story.StoryElement

/**
 * An item displayed on the Watching screen: either a [StoryElement] straight from the
 * [com.hironytic.moltonfkmp.story.Story], or a message synthesized for the current
 * viewpoint character (e.g. role introduction, seer's judgement, hunter's guard result).
 */
sealed interface WatchingElement {
    val elementId: String

    data class Element(val storyElement: StoryElement) : WatchingElement {
        override val elementId: String get() = storyElement.elementId
    }

    data class Message(
        override val elementId: String,
        val messageLines: List<String>,
    ) : WatchingElement
}
