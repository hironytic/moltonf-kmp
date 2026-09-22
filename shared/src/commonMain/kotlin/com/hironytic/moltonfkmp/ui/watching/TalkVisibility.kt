package com.hironytic.moltonfkmp.ui.watching

import com.hironytic.moltonfkmp.story.Assault
import com.hironytic.moltonfkmp.story.Character
import com.hironytic.moltonfkmp.story.CharacterMap
import com.hironytic.moltonfkmp.story.Counting
import com.hironytic.moltonfkmp.story.Counting2
import com.hironytic.moltonfkmp.story.Execution
import com.hironytic.moltonfkmp.story.Guard
import com.hironytic.moltonfkmp.story.Judge
import com.hironytic.moltonfkmp.story.Role
import com.hironytic.moltonfkmp.story.StartMirror
import com.hironytic.moltonfkmp.story.Story
import com.hironytic.moltonfkmp.story.StoryElement
import com.hironytic.moltonfkmp.story.SuddenDeath
import com.hironytic.moltonfkmp.story.Talk
import com.hironytic.moltonfkmp.story.TalkType

/**
 * Builds the list of elements to display for [currentDay], applying the spoiler-prevention
 * filtering that hides talks/events the viewpoint character ([playerCharacter]) shouldn't yet
 * see given [dayProgress], and synthesizing viewpoint-specific messages (role introduction,
 * seer's judgement, hunter's guard result, shaman's post-mortem reveal).
 *
 * `dayProgress == null` means "no restriction" (e.g. viewing a finished game in full) so every
 * element is shown as-is.
 */
fun currentElements(
    story: Story?,
    characterMap: CharacterMap,
    playerCharacter: String,
    dayProgress: Int?,
    currentDay: Int,
): List<WatchingElement> {
    if (story == null) return emptyList()
    val period = story.periods.getOrNull(currentDay) ?: return emptyList()
    val character = characterMap[playerCharacter] ?: return period.elements.map { WatchingElement.Element(it) }
    if (dayProgress != null && currentDay > dayProgress) return emptyList()

    return period.elements.flatMap { filterStoryElement(story, characterMap, dayProgress, it, character, currentDay) }
}

private fun filterStoryElement(
    story: Story,
    characterMap: CharacterMap,
    dayProgress: Int?,
    element: StoryElement,
    character: Character,
    currentDay: Int,
): List<WatchingElement> {
    fun filter(predicate: Boolean, vararg additional: WatchingElement?): List<WatchingElement> {
        val extra = additional.filterNotNull()
        return if (dayProgress == null || predicate) listOf(WatchingElement.Element(element)) + extra else extra
    }

    return when (element) {
        is Talk -> when (element.talkType) {
            TalkType.PUBLIC -> filter(isPublicTalkVisible())
            TalkType.PRIVATE -> filter(isPrivateTalkVisible(element, character))
            TalkType.WOLF -> filter(isWolfTalkVisible(story, character, currentDay))
            TalkType.GRAVE -> filter(isGraveTalkVisible(character, dayProgress))
        }

        is StartMirror ->
            filter(true, createPlayerCharacterMessage(element.elementId, character, characterMap))

        is SuddenDeath ->
            filter(true, createInformedMessage(element.elementId, currentDay, characterMap, character, element.avatarId))

        is Counting ->
            filter(true, createInformedMessage(element.elementId, currentDay, characterMap, character, element.victim))

        is Execution ->
            filter(true, createInformedMessage(element.elementId, currentDay, characterMap, character, element.victim))

        is Judge ->
            filter(
                isJudgeVisible(character),
                createJudgeMessage(element.elementId, story, currentDay, characterMap, character, element.target),
            )

        is Guard ->
            filter(
                isGuardVisible(character),
                createGuardedMessage(element.elementId, story, currentDay, characterMap, character, element.target),
            )

        is Counting2 -> filter(isCounting2Visible())

        is Assault -> filter(isAssaultVisible(story, character, currentDay))

        else -> listOf(WatchingElement.Element(element))
    }
}

/**
 * Whether [talk], spoken on [day], may be shown to [character] given [dayProgress].
 * Also used to decide whether a talk-mention link ([parseMessageSegments]) should resolve.
 */
fun isTalkVisible(story: Story, day: Int, talk: Talk, character: Character, dayProgress: Int?): Boolean {
    if (dayProgress == null) return true
    if (day > dayProgress) return false
    return when (talk.talkType) {
        TalkType.PUBLIC -> isPublicTalkVisible()
        TalkType.PRIVATE -> isPrivateTalkVisible(talk, character)
        TalkType.WOLF -> isWolfTalkVisible(story, character, day)
        TalkType.GRAVE -> isGraveTalkVisible(character, dayProgress)
    }
}

private fun isPublicTalkVisible(): Boolean = true

private fun isPrivateTalkVisible(talk: Talk, character: Character): Boolean =
    talk.avatarId == character.avatar.avatarId

private fun isWolfTalkVisible(story: Story, character: Character, currentDay: Int): Boolean =
    (character.role == Role.WOLF && character.aliveUntil >= currentDay) ||
        (story.landId == "wolfc" && character.role == Role.MADMAN)

private fun isGraveTalkVisible(character: Character, dayProgress: Int?): Boolean =
    dayProgress == null || character.aliveUntil < dayProgress

private fun isJudgeVisible(character: Character): Boolean = character.role == Role.SEER

private fun isGuardVisible(character: Character): Boolean = character.role == Role.HUNTER

private fun isCounting2Visible(): Boolean = false

private fun isAssaultVisible(story: Story, character: Character, currentDay: Int): Boolean =
    isWolfTalkVisible(story, character, currentDay)

private fun createPlayerCharacterMessage(
    idBase: String,
    character: Character,
    characterMap: CharacterMap,
): WatchingElement.Message {
    val fullName = character.avatar.fullName
    val messageLines: List<String> = when (character.role) {
        Role.INNOCENT -> listOf(
            "あなたは $fullName、ただの村人です。しかしあなたの推理力や発言が、村人側の勝利の鍵となるかもしれません。",
        )

        Role.WOLF -> listOf(
            "あなたは $fullName、人狼です。村人を人狼と同数以下まで減らせば勝利です。村人に悟られないように、慎重に邪魔者を排除していきましょう。",
        )

        Role.SEER -> listOf(
            "あなたは $fullName、占い師です。毎夜、誰かひとりを占うことができます。それにより、相手が人狼か人間かを知ることができます。",
        )

        Role.SHAMAN -> listOf(
            "あなたは $fullName、霊能者です。処刑によって命を失ったものが、人間であったか人狼であったかを知ることができます。",
        )

        Role.HUNTER -> listOf(
            "あなたは $fullName、狩人です。毎夜、ひとりだけを、人狼の襲撃から守ることができます。人狼の行動を読み、村人たちを人狼から守って下さい。",
        )

        Role.FRATER -> buildList {
            add("あなたは $fullName、共有者です。もうひとりの共有者が誰であるかを知る事ができます。")
            val otherFrater = characterMap.values.firstOrNull {
                it.role == Role.FRATER && it.avatar.avatarId != character.avatar.avatarId
            }
            if (otherFrater != null) {
                add("")
                add("もうひとりの共有者は、${otherFrater.avatar.fullName} です。")
            }
        }

        Role.MADMAN -> listOf(
            "あなたは $fullName、人狼の繁栄を望む狂人です。人狼の勝利があなたの勝利となります。",
            "",
            "人狼の勝利のため、存分に議論をかきまわして下さい。",
        )

        Role.HAMSTER -> listOf(
            "あなたは $fullName、ハムスター人間です。人狼に襲撃されても死亡しませんが、占い師に占われると死亡します。",
            "",
            "人狼の全滅時、もしくは村人の数が人狼の数より少なくなった時に生存していればあなたの勝利になります。",
        )
    }
    return WatchingElement.Message("${idBase}_player-character", messageLines)
}

private fun createGuardedMessage(
    idBase: String,
    story: Story,
    currentDay: Int,
    characterMap: CharacterMap,
    character: Character,
    targetId: String,
): WatchingElement.Message? {
    if (character.role != Role.HUNTER) return null

    // In land G, the hunter cannot be aware whether he/she guarded correctly or not.
    if (story.landId == "wolfg") return null

    if (character.aliveUntil < currentDay) return null

    val period = story.periods.getOrNull(currentDay) ?: return null
    val assault = period.elements.filterIsInstance<Assault>().firstOrNull() ?: return null
    if (assault.target != targetId) return null

    val guarded = characterMap[targetId] ?: return null
    return WatchingElement.Message(
        "${idBase}_guarded",
        listOf("${guarded.avatar.fullName} を人狼の襲撃から守った。"),
    )
}

private fun createJudgeMessage(
    idBase: String,
    story: Story,
    currentDay: Int,
    characterMap: CharacterMap,
    character: Character,
    targetId: String,
): WatchingElement.Message? {
    if (character.role != Role.SEER) return null
    if (character.aliveUntil < currentDay) return null

    val target = characterMap[targetId] ?: return null
    val messageLines = if (target.role == Role.WOLF) {
        listOf("${target.avatar.fullName} は人狼のようだ。")
    } else if (story.landId == "wolfe") {
        listOf("${target.avatar.fullName} は人狼ではないようだ。")
    } else {
        listOf("${target.avatar.fullName} は人間のようだ。")
    }
    return WatchingElement.Message("${idBase}_judgement", messageLines)
}

private fun createInformedMessage(
    idBase: String,
    currentDay: Int,
    characterMap: CharacterMap,
    character: Character,
    targetId: String?,
): WatchingElement.Message? {
    if (targetId == null) return null
    if (currentDay < 3) return null
    if (character.role != Role.SHAMAN) return null
    if (character.aliveUntil < currentDay) return null

    val target = characterMap[targetId] ?: return null
    val messageLines = if (target.role == Role.WOLF) {
        listOf("${target.avatar.fullName} は人狼だった。")
    } else {
        listOf("${target.avatar.fullName} は人狼ではなかった。")
    }
    return WatchingElement.Message("${idBase}_informed", messageLines)
}
