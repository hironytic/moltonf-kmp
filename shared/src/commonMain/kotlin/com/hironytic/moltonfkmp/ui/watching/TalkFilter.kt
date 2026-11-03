package com.hironytic.moltonfkmp.ui.watching

import com.hironytic.moltonfkmp.story.Assault
import com.hironytic.moltonfkmp.story.Talk
import com.hironytic.moltonfkmp.story.TalkType

/**
 * Display filter for talks. Anything not listed as hidden is shown, so the default
 * (empty) filter shows everything.
 */
data class TalkFilter(
    val hiddenAvatarIds: Set<String> = emptySet(),
    val hiddenTalkTypes: Set<TalkType> = emptySet(),
) {
    val isDefault: Boolean get() = hiddenAvatarIds.isEmpty() && hiddenTalkTypes.isEmpty()

    fun accepts(talkType: TalkType, avatarId: String): Boolean =
        talkType !in hiddenTalkTypes && avatarId !in hiddenAvatarIds
}

/** An element to display together with its index in the unfiltered list of the day. */
data class IndexedElement(val index: Int, val element: WatchingElement)

/**
 * Removes talks rejected by [filter]. An [Assault] is treated as the wolf talk it is displayed as.
 * Non-talk elements are always kept. Each result keeps its index in [elements], so that positions
 * such as the read position stay meaningful regardless of the filter.
 */
fun applyTalkFilter(elements: List<WatchingElement>, filter: TalkFilter): List<IndexedElement> =
    elements.mapIndexedNotNull { index, element ->
        val storyElement = (element as? WatchingElement.Element)?.storyElement
        val accepted = when (storyElement) {
            is Talk -> filter.accepts(storyElement.talkType, storyElement.avatarId)
            is Assault -> filter.accepts(TalkType.WOLF, storyElement.byWhom)
            else -> true
        }
        if (accepted) IndexedElement(index, element) else null
    }
