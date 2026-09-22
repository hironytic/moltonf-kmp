package com.hironytic.moltonfkmp.story

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private fun talk(elementId: String, talkNo: Int?, time: Int) = Talk(
    elementId = elementId,
    talkType = TalkType.PUBLIC,
    avatarId = "gerd",
    xname = "gerd",
    time = time,
    talkNo = talkNo,
    messageLines = listOf("hello"),
)

private fun story(vararg periods: Period) = Story(
    villageFullName = "村",
    baseURI = "https://example.com/",
    landId = "wolfg",
    graveIconURI = "img/grave.png",
    periods = periods.toList(),
    avatarList = emptyList(),
)

class TalkMapTest {
    @Test
    fun getTalkByTalkNo_findsTalkAcrossPeriods() {
        val talk1 = talk("t1", talkNo = 1, time = 12 * 3_600_000)
        val talk2 = talk("t2", talkNo = 2, time = 13 * 3_600_000)
        val talkMap = createTalkMap(
            story(
                Period(PeriodType.PROGRESS, day = 1, elements = listOf(talk1)),
                Period(PeriodType.PROGRESS, day = 2, elements = listOf(talk2)),
            )
        )

        assertEquals(TalkWithDay(1, talk1), talkMap.getTalkByTalkNo(1))
        assertEquals(TalkWithDay(2, talk2), talkMap.getTalkByTalkNo(2))
        assertNull(talkMap.getTalkByTalkNo(999))
    }

    @Test
    fun getTalkByTalkNo_ignoresTalksWithoutTalkNo() {
        val talkMap = createTalkMap(
            story(Period(PeriodType.PROGRESS, day = 1, elements = listOf(talk("t1", talkNo = null, time = 0))))
        )
        assertNull(talkMap.getTalkByTalkNo(1))
    }

    @Test
    fun getTalkByTime_findsAllTalksAtTheSameMinute_onTheSameDay() {
        val time = 12 * 3_600_000 + 34 * 60_000
        val talkA = talk("a", talkNo = 1, time = time)
        val talkB = talk("b", talkNo = 2, time = time + 30_000) // same minute, different second
        val other = talk("c", talkNo = 3, time = time + 60_000) // next minute

        val talkMap = createTalkMap(
            story(Period(PeriodType.PROGRESS, day = 1, elements = listOf(talkA, talkB, other)))
        )

        val result = talkMap.getTalkByTime(1, 12, 34)
        assertEquals(2, result.size)
        assertTrue(talkA in result)
        assertTrue(talkB in result)
    }

    @Test
    fun getTalkByTime_distinguishesDay() {
        val time = 12 * 3_600_000
        val talkDay1 = talk("a", talkNo = 1, time = time)
        val talkDay2 = talk("b", talkNo = 2, time = time)

        val talkMap = createTalkMap(
            story(
                Period(PeriodType.PROGRESS, day = 1, elements = listOf(talkDay1)),
                Period(PeriodType.PROGRESS, day = 2, elements = listOf(talkDay2)),
            )
        )

        assertEquals(listOf(talkDay1), talkMap.getTalkByTime(1, 12, 0))
        assertEquals(listOf(talkDay2), talkMap.getTalkByTime(2, 12, 0))
    }

    @Test
    fun nullTalkMap_returnsNothing() {
        val talkMap = nullTalkMap()
        assertNull(talkMap.getTalkByTalkNo(1))
        assertEquals(emptyList(), talkMap.getTalkByTime(1, 0, 0))
    }
}
