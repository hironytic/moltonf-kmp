package com.hironytic.moltonfkmp.ui.watching

import com.hironytic.moltonfkmp.story.Assault
import com.hironytic.moltonfkmp.story.Avatar
import com.hironytic.moltonfkmp.story.Character
import com.hironytic.moltonfkmp.story.Guard
import com.hironytic.moltonfkmp.story.Judge
import com.hironytic.moltonfkmp.story.Period
import com.hironytic.moltonfkmp.story.PeriodType
import com.hironytic.moltonfkmp.story.Role
import com.hironytic.moltonfkmp.story.StartMirror
import com.hironytic.moltonfkmp.story.Story
import com.hironytic.moltonfkmp.story.StoryElement
import com.hironytic.moltonfkmp.story.Talk
import com.hironytic.moltonfkmp.story.TalkType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

private fun avatar(id: String, name: String = id) = Avatar(id, name, name, null)

private fun character(id: String, role: Role, aliveUntil: Int = 99) = Character(avatar(id), role, aliveUntil)

private fun talk(elementId: String, talkType: TalkType, avatarId: String, time: Int = 0) = Talk(
    elementId = elementId,
    talkType = talkType,
    avatarId = avatarId,
    xname = avatarId,
    time = time,
    talkNo = null,
    messageLines = listOf("msg"),
)

/** Builds a [Story] whose `periods` list index matches each period's `day`, as real archives do. */
private fun storyWithPeriods(landId: String, vararg elementsPerDay: List<StoryElement>) = Story(
    villageFullName = "村",
    baseURI = "https://example.com/",
    landId = landId,
    graveIconURI = "img/grave.png",
    periods = elementsPerDay.mapIndexed { day, elements -> Period(PeriodType.PROGRESS, day = day, elements = elements) },
    avatarList = emptyList(),
)

class TalkVisibilityTest {
    @Test
    fun unknownPlayerCharacter_showsRawElementsUnfiltered() {
        val villager = talk("t1", TalkType.PUBLIC, "gerd")
        val wolfTalk = talk("t2", TalkType.WOLF, "gerd")
        val s = storyWithPeriods("wolfg", listOf(villager, wolfTalk))

        val result = currentElements(s, emptyMap(), playerCharacter = "unknown", dayProgress = 0, currentDay = 0)

        assertEquals(listOf(villager.elementId, wolfTalk.elementId), result.map { it.elementId })
    }

    @Test
    fun currentDayBeyondDayProgress_showsNothing() {
        val villager = talk("t1", TalkType.PUBLIC, "gerd")
        val s = storyWithPeriods("wolfg", emptyList(), emptyList(), listOf(villager))
        val characterMap = mapOf("gerd" to character("gerd", Role.INNOCENT))

        val result = currentElements(s, characterMap, playerCharacter = "gerd", dayProgress = 1, currentDay = 2)

        assertEquals(emptyList(), result)
    }

    @Test
    fun publicTalk_isAlwaysVisible() {
        val publicTalk = talk("t1", TalkType.PUBLIC, "gerd")
        val s = storyWithPeriods("wolfg", listOf(publicTalk))
        val characterMap = mapOf("gerd" to character("gerd", Role.INNOCENT))

        val result = currentElements(s, characterMap, playerCharacter = "gerd", dayProgress = 0, currentDay = 0)

        assertEquals(listOf(publicTalk.elementId), result.map { it.elementId })
    }

    @Test
    fun wolfTalk_isHiddenFromVillagers_butVisibleToWolves() {
        val wolfTalk = talk("t1", TalkType.WOLF, "wolfAvatar")
        val s = storyWithPeriods("wolfg", listOf(wolfTalk))

        val villagerMap = mapOf("gerd" to character("gerd", Role.INNOCENT))
        assertEquals(emptyList(), currentElements(s, villagerMap, "gerd", dayProgress = 0, currentDay = 0))

        val wolfMap = mapOf("wolfAvatar" to character("wolfAvatar", Role.WOLF))
        assertEquals(
            listOf(wolfTalk.elementId),
            currentElements(s, wolfMap, "wolfAvatar", dayProgress = 0, currentDay = 0).map { it.elementId },
        )
    }

    @Test
    fun privateTalk_isVisibleOnlyToItsSpeaker() {
        val privateTalk = talk("t1", TalkType.PRIVATE, "gerd")
        val s = storyWithPeriods("wolfg", listOf(privateTalk))

        val speakerMap = mapOf("gerd" to character("gerd", Role.INNOCENT))
        assertEquals(
            listOf(privateTalk.elementId),
            currentElements(s, speakerMap, "gerd", dayProgress = 0, currentDay = 0).map { it.elementId },
        )

        val otherMap = mapOf("gerd" to character("gerd", Role.INNOCENT), "otto" to character("otto", Role.INNOCENT))
        assertEquals(emptyList(), currentElements(s, otherMap, "otto", dayProgress = 0, currentDay = 0))
    }

    @Test
    fun graveTalk_isVisibleOnlyAfterTheSpeakerCharacterHasDied() {
        val graveTalk = talk("t1", TalkType.GRAVE, "gerd")
        val s = storyWithPeriods("wolfg", emptyList(), emptyList(), listOf(graveTalk))

        // gerd died on day 1 (aliveUntil = 1); viewer's own character (otto) is still alive.
        val aliveViewer = mapOf(
            "gerd" to character("gerd", Role.INNOCENT, aliveUntil = 1),
            "otto" to character("otto", Role.INNOCENT, aliveUntil = 99),
        )
        assertEquals(emptyList(), currentElements(s, aliveViewer, "otto", dayProgress = 2, currentDay = 2))

        val deadViewer = mapOf(
            "gerd" to character("gerd", Role.INNOCENT, aliveUntil = 1),
            "otto" to character("otto", Role.INNOCENT, aliveUntil = 1),
        )
        assertEquals(
            listOf(graveTalk.elementId),
            currentElements(s, deadViewer, "otto", dayProgress = 2, currentDay = 2).map { it.elementId },
        )
    }

    @Test
    fun dayProgressNull_disablesAllFiltering() {
        val wolfTalk = talk("t1", TalkType.WOLF, "wolfAvatar")
        val s = storyWithPeriods("wolfg", listOf(wolfTalk))
        val villagerMap = mapOf("gerd" to character("gerd", Role.INNOCENT))

        val result = currentElements(s, villagerMap, "gerd", dayProgress = null, currentDay = 0)

        assertEquals(listOf(wolfTalk.elementId), result.map { it.elementId })
    }

    @Test
    fun startMirror_synthesizesPlayerCharacterIntroductionMessage() {
        val mirror = StartMirror(elementId = "m1", messageLines = listOf("鏡開始"))
        val s = storyWithPeriods("wolfg", listOf(mirror))
        val characterMap = mapOf("gerd" to character("gerd", Role.SEER))

        val result = currentElements(s, characterMap, "gerd", dayProgress = 0, currentDay = 0)

        assertEquals(2, result.size)
        assertEquals(WatchingElement.Element(mirror), result[0])
        val message = assertIs<WatchingElement.Message>(result[1])
        assertTrue(message.messageLines[0].contains("占い師"))
    }

    @Test
    fun judge_isVisibleOnlyToSeer_andSynthesizesJudgementMessage() {
        val judge = Judge(elementId = "j1", messageLines = listOf("占い"), byWhom = "seer", target = "wolfAvatar")
        val s = storyWithPeriods("wolfg", listOf(judge))

        val seerMap = mapOf(
            "seer" to character("seer", Role.SEER),
            "wolfAvatar" to character("wolfAvatar", Role.WOLF),
        )
        val seerResult = currentElements(s, seerMap, "seer", dayProgress = 0, currentDay = 0)
        assertEquals(2, seerResult.size)
        val message = assertIs<WatchingElement.Message>(seerResult[1])
        assertEquals(listOf("wolfAvatar は人狼のようだ。"), message.messageLines)

        val villagerMap = mapOf(
            "gerd" to character("gerd", Role.INNOCENT),
            "seer" to character("seer", Role.SEER),
            "wolfAvatar" to character("wolfAvatar", Role.WOLF),
        )
        assertEquals(emptyList(), currentElements(s, villagerMap, "gerd", dayProgress = 0, currentDay = 0))
    }

    @Test
    fun guard_synthesizesGuardedMessage_onlyWhenAssaultTargetWasGuarded() {
        val guard = Guard(elementId = "g1", messageLines = listOf("護衛"), byWhom = "hunter", target = "gerd")
        val assault = Assault(
            elementId = "a1", messageLines = listOf("襲撃"), byWhom = "wolfAvatar", target = "gerd",
            xname = "wolfAvatar", time = 0,
        )
        // landId "wolfe" (not "wolfg"): in land G the hunter never learns whether the guard succeeded.
        val s = storyWithPeriods("wolfe", listOf(guard, assault))
        val hunterMap = mapOf(
            "hunter" to character("hunter", Role.HUNTER),
            "gerd" to character("gerd", Role.INNOCENT),
        )

        val result = currentElements(s, hunterMap, "hunter", dayProgress = 0, currentDay = 0)

        // The guard event itself, the assault (hidden: hunter isn't a wolf), and the synthesized message.
        val messages = result.filterIsInstance<WatchingElement.Message>()
        assertEquals(1, messages.size)
        assertEquals(listOf("gerd を人狼の襲撃から守った。"), messages[0].messageLines)
    }
}
