package com.hironytic.moltonfkmp.ui.watching

import com.hironytic.moltonfkmp.story.Talk
import com.hironytic.moltonfkmp.story.TalkType
import com.hironytic.moltonfkmp.story.TalkWithDay
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private fun talk(elementId: String) = Talk(
    elementId = elementId,
    talkType = TalkType.PUBLIC,
    avatarId = "gerd",
    xname = "gerd",
    time = 0,
    talkNo = null,
    messageLines = listOf("msg $elementId"),
)

private class SequentialIds {
    private var next = 0
    fun nextId(): String = "id${next++}"
}

class TalkThreadTest {
    @Test
    fun createTalkThreadRoot_startsWithTheLinkAlreadyExpanded() {
        val source = talk("source")
        val target = talk("target")
        val ids = SequentialIds()

        val root = createTalkThreadRoot(
            id = "root",
            day = 1,
            talk = source,
            linkKey = "L1",
            targets = listOf(TalkWithDay(2, target)),
            nextId = ids::nextId,
        )

        assertEquals(source, root.talk)
        assertEquals(listOf(target), root.expandedLinks.getValue("L1").map { it.talk })

        val flat = root.flatten()
        assertEquals(listOf(0, 1), flat.map { it.depth })
        assertEquals(listOf(source.elementId, target.elementId), flat.map { it.entry.talk.elementId })
        assertEquals(emptyList(), flat[0].path)
        assertEquals(listOf(flat[1].entry.id), flat[1].path)
    }

    @Test
    fun toggleLink_onRoot_collapsesAnAlreadyExpandedLink() {
        val source = talk("source")
        val target = talk("target")
        val ids = SequentialIds()
        val root = createTalkThreadRoot("root", 1, source, "L1", listOf(TalkWithDay(2, target)), ids::nextId)

        val collapsed = root.toggleLink(path = emptyList(), linkKey = "L1", targets = emptyList(), nextId = ids::nextId)

        assertTrue(collapsed.expandedLinks.isEmpty())
        assertEquals(listOf(source.elementId), collapsed.flatten().map { it.entry.talk.elementId })
    }

    @Test
    fun toggleLink_onNestedEntry_insertsDirectlyBelowIt_andCollapsesOnSecondTap() {
        val source = talk("source")
        val target = talk("target")
        val grandchild = talk("grandchild")
        val ids = SequentialIds()

        val root = createTalkThreadRoot("root", 1, source, "L1", listOf(TalkWithDay(2, target)), ids::nextId)
        val targetEntryId = root.expandedLinks.getValue("L1").single().id

        val expanded = root.toggleLink(
            path = listOf(targetEntryId),
            linkKey = "L2",
            targets = listOf(TalkWithDay(3, grandchild)),
            nextId = ids::nextId,
        )

        val flat = expanded.flatten()
        assertEquals(
            listOf(source.elementId, target.elementId, grandchild.elementId),
            flat.map { it.entry.talk.elementId },
        )
        assertEquals(listOf(0, 1, 2), flat.map { it.depth })
        // The grandchild's path must start with the target entry's id, so a later toggle can find it again.
        assertEquals(targetEntryId, flat[2].path.first())

        val collapsedAgain = expanded.toggleLink(
            path = listOf(targetEntryId),
            linkKey = "L2",
            targets = emptyList(),
            nextId = ids::nextId,
        )
        assertEquals(
            listOf(source.elementId, target.elementId),
            collapsedAgain.flatten().map { it.entry.talk.elementId },
        )
    }

    @Test
    fun toggleLink_collapsingAnEntry_removesEverythingNestedUnderIt() {
        val source = talk("source")
        val target = talk("target")
        val grandchild = talk("grandchild")
        val ids = SequentialIds()

        val root = createTalkThreadRoot("root", 1, source, "L1", listOf(TalkWithDay(2, target)), ids::nextId)
        val targetEntryId = root.expandedLinks.getValue("L1").single().id
        val expanded = root.toggleLink(listOf(targetEntryId), "L2", listOf(TalkWithDay(3, grandchild)), ids::nextId)

        // Collapse the top-level link; the grandchild nested under `target` must disappear too.
        val collapsed = expanded.toggleLink(emptyList(), "L1", emptyList(), ids::nextId)

        assertEquals(listOf(source.elementId), collapsed.flatten().map { it.entry.talk.elementId })
    }

    @Test
    fun multipleTargetsForOneLink_areAllInsertedInOrder() {
        val source = talk("source")
        val t1 = talk("t1")
        val t2 = talk("t2")
        val ids = SequentialIds()

        val root = createTalkThreadRoot("root", 1, source, "L1", listOf(TalkWithDay(1, t1), TalkWithDay(1, t2)), ids::nextId)

        assertEquals(
            listOf(source.elementId, t1.elementId, t2.elementId),
            root.flatten().map { it.entry.talk.elementId },
        )
    }
}
