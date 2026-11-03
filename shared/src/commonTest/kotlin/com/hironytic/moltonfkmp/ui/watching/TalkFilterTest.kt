package com.hironytic.moltonfkmp.ui.watching

import com.hironytic.moltonfkmp.story.Talk
import com.hironytic.moltonfkmp.story.TalkType
import kotlin.test.Test
import kotlin.test.assertEquals

class TalkFilterTest {
    private fun talk(id: String, type: TalkType, avatar: String) = WatchingElement.Element(
        Talk(id, type, avatar, "x", 0, null, listOf("hi")),
    )

    private val elements = listOf(
        talk("1", TalkType.PUBLIC, "a"),
        talk("2", TalkType.WOLF, "a"),
        talk("3", TalkType.PUBLIC, "b"),
        talk("4", TalkType.PRIVATE, "b"),
        WatchingElement.Message("m", listOf("msg")),
    )

    private fun ids(list: List<IndexedElement>) = list.map { it.element.elementId }

    @Test
    fun defaultFilterKeepsEverything() {
        assertEquals(elements.map { it.elementId }, ids(applyTalkFilter(elements, TalkFilter())))
    }

    @Test
    fun hidesByAvatar() {
        assertEquals(listOf("3", "4", "m"), ids(applyTalkFilter(elements, TalkFilter(hiddenAvatarIds = setOf("a")))))
    }

    @Test
    fun hidesByTalkType() {
        val filter = TalkFilter(hiddenTalkTypes = setOf(TalkType.WOLF, TalkType.PRIVATE))
        assertEquals(listOf("1", "3", "m"), ids(applyTalkFilter(elements, filter)))
    }

    @Test
    fun keepsIndexInUnfilteredList() {
        val filter = TalkFilter(hiddenAvatarIds = setOf("a"))
        assertEquals(listOf(2, 3, 4), applyTalkFilter(elements, filter).map { it.index })
    }
}
