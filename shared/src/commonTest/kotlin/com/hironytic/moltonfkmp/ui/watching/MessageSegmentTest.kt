package com.hironytic.moltonfkmp.ui.watching

import com.hironytic.moltonfkmp.story.Talk
import com.hironytic.moltonfkmp.story.TalkType
import com.hironytic.moltonfkmp.story.TalkWithDay
import com.hironytic.moltonfkmp.story.createTalkMap
import com.hironytic.moltonfkmp.story.nullTalkMap
import com.hironytic.moltonfkmp.story.Period
import com.hironytic.moltonfkmp.story.PeriodType
import com.hironytic.moltonfkmp.story.Story
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

private fun talk(elementId: String, talkNo: Int?, time: Int) = Talk(
    elementId = elementId,
    talkType = TalkType.PUBLIC,
    avatarId = "gerd",
    xname = "gerd",
    time = time,
    talkNo = talkNo,
    messageLines = listOf("hello"),
)

private val alwaysVisible: (TalkWithDay) -> Boolean = { true }

class MessageSegmentTest {
    @Test
    fun plainText_isASingleGeneralSegment() {
        val segments = parseMessageSegments("hello world", currentDay = 1, talkMap = nullTalkMap(), isTalkVisible = alwaysVisible)
        assertEquals(listOf(MessageSegment.General("hello world")), segments)
    }

    @Test
    fun talkNoAnchor_becomesLinkToTalk_whenItResolves() {
        val target = talk("t1", talkNo = 42, time = 0)
        val story = Story(
            villageFullName = "村", baseURI = "https://example.com/", landId = "wolfg", graveIconURI = "img/grave.png",
            periods = listOf(Period(PeriodType.PROGRESS, day = 1, elements = listOf(target))),
            avatarList = emptyList(),
        )
        val talkMap = createTalkMap(story)

        val segments = parseMessageSegments(">>42 だと思う", currentDay = 1, talkMap = talkMap, isTalkVisible = alwaysVisible)

        assertEquals(2, segments.size)
        val link = assertIs<MessageSegment.LinkToTalk>(segments[0])
        assertEquals(">>42", link.text)
        assertEquals(listOf(TalkWithDay(1, target)), link.talks)
        assertEquals(MessageSegment.General(" だと思う"), segments[1])
    }

    @Test
    fun talkNoAnchor_staysGeneral_whenTalkDoesNotExist() {
        val segments = parseMessageSegments(">>999", currentDay = 1, talkMap = nullTalkMap(), isTalkVisible = alwaysVisible)
        assertEquals(listOf(MessageSegment.General(">>999")), segments)
    }

    @Test
    fun talkNoAnchor_staysGeneral_whenNotVisible() {
        val target = talk("t1", talkNo = 42, time = 0)
        val story = Story(
            villageFullName = "村", baseURI = "https://example.com/", landId = "wolfg", graveIconURI = "img/grave.png",
            periods = listOf(Period(PeriodType.PROGRESS, day = 1, elements = listOf(target))),
            avatarList = emptyList(),
        )
        val talkMap = createTalkMap(story)

        val segments = parseMessageSegments(">>42", currentDay = 1, talkMap = talkMap, isTalkVisible = { false })
        assertEquals(listOf(MessageSegment.General(">>42")), segments)
    }

    @Test
    fun timeMention_withExplicitDay_resolvesAgainstThatDay() {
        val target = talk("t1", talkNo = null, time = 12 * 3_600_000 + 34 * 60_000)
        val story = Story(
            villageFullName = "村", baseURI = "https://example.com/", landId = "wolfg", graveIconURI = "img/grave.png",
            periods = listOf(
                Period(PeriodType.PROGRESS, day = 1, elements = emptyList()),
                Period(PeriodType.PROGRESS, day = 3, elements = listOf(target)),
            ),
            avatarList = emptyList(),
        )
        val talkMap = createTalkMap(story)

        val segments = parseMessageSegments("3d12:34 のあれ", currentDay = 1, talkMap = talkMap, isTalkVisible = alwaysVisible)

        val link = assertIs<MessageSegment.LinkToTalk>(segments[0])
        assertEquals("3d12:34", link.text)
        assertEquals(listOf(TalkWithDay(3, target)), link.talks)
    }

    @Test
    fun timeMention_withoutDay_usesCurrentDay_andAcceptsCompactForm() {
        val target = talk("t1", talkNo = null, time = 12 * 3_600_000 + 34 * 60_000)
        val story = Story(
            villageFullName = "村", baseURI = "https://example.com/", landId = "wolfg", graveIconURI = "img/grave.png",
            periods = listOf(Period(PeriodType.PROGRESS, day = 2, elements = listOf(target))),
            avatarList = emptyList(),
        )
        val talkMap = createTalkMap(story)

        val segments = parseMessageSegments("1234", currentDay = 2, talkMap = talkMap, isTalkVisible = alwaysVisible)

        val link = assertIs<MessageSegment.LinkToTalk>(segments[0])
        assertEquals("1234", link.text)
        assertEquals(listOf(TalkWithDay(2, target)), link.talks)
    }

    @Test
    fun timeMention_withMultipleTalksAtSameMinute_listsAllVisibleOnes() {
        val a = talk("a", talkNo = null, time = 12 * 3_600_000)
        val b = talk("b", talkNo = null, time = 12 * 3_600_000 + 1_000)
        val story = Story(
            villageFullName = "村", baseURI = "https://example.com/", landId = "wolfg", graveIconURI = "img/grave.png",
            periods = listOf(Period(PeriodType.PROGRESS, day = 1, elements = listOf(a, b))),
            avatarList = emptyList(),
        )
        val talkMap = createTalkMap(story)

        val segments = parseMessageSegments("12:00", currentDay = 1, talkMap = talkMap, isTalkVisible = alwaysVisible)

        val link = assertIs<MessageSegment.LinkToTalk>(segments[0])
        assertEquals(listOf(TalkWithDay(1, a), TalkWithDay(1, b)), link.talks)
    }
}
