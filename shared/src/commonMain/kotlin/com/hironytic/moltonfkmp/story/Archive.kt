package com.hironytic.moltonfkmp.story

import kotlin.uuid.Uuid
import org.kobjects.ktxml.api.EventType
import org.kobjects.ktxml.api.XmlPullParser
import org.kobjects.ktxml.mini.MiniXmlPullParser

class InvalidArchiveException(message: String) : Exception(message)

private val TIME_REGEX = Regex("""^(\d{2}):(\d{2}):(\d{2})(?:\.(\d+))?""")

/**
 * Parses a "Jindolf XmlScheme" archive document into a [Story].
 * [xml] must already be decoded text (file reading/charset detection is the caller's concern).
 */
fun parseStory(xml: String): Story {
    val parser = MiniXmlPullParser(xml.iterator(), processNamespaces = false)
    return ArchiveParser(parser).parseStory()
}

private class ArchiveParser(private val parser: XmlPullParser) {
    private var publicTalkCount = 0

    fun parseStory(): Story {
        advanceToRootStartTag()
        if (parser.name != "village") {
            throw InvalidArchiveException("Root element must be \"village\" but was \"${parser.name}\".")
        }
        return parseVillage()
    }

    private fun advanceToRootStartTag() {
        while (true) {
            when (parser.next()) {
                EventType.START_TAG -> return
                EventType.END_DOCUMENT -> throw InvalidArchiveException("No root element found.")
                else -> {}
            }
        }
    }

    // --- generic helpers -------------------------------------------------

    private fun requiredAttr(name: String): String =
        optionalAttr(name)
            ?: throw InvalidArchiveException("Missing attribute \"$name\" in element \"${parser.name}\".")

    private fun optionalAttr(name: String): String? {
        for (i in 0 until parser.attributeCount) {
            if (parser.getAttributeName(i) == name) {
                return parser.getAttributeValue(i)
            }
        }
        return null
    }

    private fun requiredIntAttr(name: String): Int {
        val value = requiredAttr(name)
        return value.toIntOrNull()
            ?: throw InvalidArchiveException("Invalid integer value \"$value\" for attribute \"$name\".")
    }

    /**
     * Calls [onChild] for each direct child start tag of the element whose start tag is the
     * current event. [onChild] must fully consume the child (ending on its own END_TAG), e.g.
     * via [skipToEndTag] or a nested call to this function. Returns once this element's own
     * END_TAG has been consumed.
     */
    private inline fun eachChild(onChild: (name: String) -> Unit) {
        while (true) {
            when (parser.next()) {
                EventType.START_TAG -> onChild(parser.name)
                EventType.END_TAG -> return
                EventType.END_DOCUMENT -> throw InvalidArchiveException("Unexpected end of document.")
                else -> {}
            }
        }
    }

    /** Skips over the remainder of the element whose start tag is the current event. */
    private fun skipToEndTag() {
        var depth = 1
        while (depth > 0) {
            when (parser.next()) {
                EventType.START_TAG -> depth++
                EventType.END_TAG -> depth--
                EventType.END_DOCUMENT -> throw InvalidArchiveException("Unexpected end of document.")
                else -> {}
            }
        }
    }

    private fun parseTime(timeString: String): Int {
        val match = TIME_REGEX.find(timeString)
            ?: throw InvalidArchiveException("Invalid time string \"$timeString\".")
        val (hourPart, minutePart, secondPart, fraction) = match.destructured
        val millisecondPart = if (fraction.isEmpty()) 0 else fraction.take(3).padEnd(3, '0').toInt()
        return hourPart.toInt() * 3_600_000 +
            minutePart.toInt() * 60_000 +
            secondPart.toInt() * 1_000 +
            millisecondPart
    }

    private fun parseBoolean(value: String): Boolean = when (value) {
        "0", "false" -> false
        "1", "true" -> true
        else -> throw InvalidArchiveException("Invalid boolean value \"$value\".")
    }

    private fun parsePeriodType(value: String): PeriodType = when (value) {
        "prologue" -> PeriodType.PROLOGUE
        "progress" -> PeriodType.PROGRESS
        "epilogue" -> PeriodType.EPILOGUE
        else -> throw InvalidArchiveException("Invalid period type \"$value\".")
    }

    private fun parseTalkType(value: String): TalkType = when (value) {
        "public" -> TalkType.PUBLIC
        "wolf" -> TalkType.WOLF
        "private" -> TalkType.PRIVATE
        "grave" -> TalkType.GRAVE
        else -> throw InvalidArchiveException("Invalid talk type \"$value\".")
    }

    private fun parseRole(value: String): Role = when (value) {
        "innocent" -> Role.INNOCENT
        "wolf" -> Role.WOLF
        "seer" -> Role.SEER
        "shaman" -> Role.SHAMAN
        "madman" -> Role.MADMAN
        "hunter" -> Role.HUNTER
        "frater" -> Role.FRATER
        "hamster" -> Role.HAMSTER
        else -> throw InvalidArchiveException("Invalid role \"$value\".")
    }

    // --- village / avatarList / period ------------------------------------

    private fun parseVillage(): Story {
        val villageFullName = requiredAttr("fullName")
        val baseURI = requiredAttr("xml:base")
        val landId = requiredAttr("landId")
        val graveIconURI = requiredAttr("graveIconURI")

        var avatarList: List<Avatar>? = null
        val periods = mutableListOf<Period>()
        eachChild { name ->
            when (name) {
                "avatarList" -> avatarList = parseAvatarList()
                "period" -> periods.add(parsePeriod())
                else -> skipToEndTag()
            }
        }

        return Story(
            villageFullName = villageFullName,
            baseURI = baseURI,
            landId = landId,
            graveIconURI = graveIconURI,
            periods = periods,
            avatarList = avatarList
                ?: throw InvalidArchiveException("Missing child element \"avatarList\" in element \"village\"."),
        )
    }

    private fun parseAvatarList(): List<Avatar> {
        val avatars = mutableListOf<Avatar>()
        eachChild { name ->
            if (name == "avatar") avatars.add(parseAvatar()) else skipToEndTag()
        }
        return avatars
    }

    private fun parseAvatar(): Avatar {
        val avatarId = requiredAttr("avatarId")
        val fullName = requiredAttr("fullName")
        val shortName = requiredAttr("shortName")
        val faceIconURI = optionalAttr("faceIconURI")
        skipToEndTag()
        return Avatar(avatarId, fullName, shortName, faceIconURI)
    }

    private fun parsePeriod(): Period {
        val type = parsePeriodType(requiredAttr("type"))
        val day = requiredIntAttr("day")
        val elements = mutableListOf<StoryElement>()
        eachChild { name ->
            val element = parseStoryElement(name)
            if (element != null) elements.add(element) else skipToEndTag()
        }
        return Period(type, day, elements)
    }

    private fun parseStoryElement(name: String): StoryElement? {
        val elementId = Uuid.random().toString()
        return when (name) {
            "talk" -> parseTalk(elementId)
            "startEntry" -> parseStartEntry(elementId)
            "onStage" -> parseOnStage(elementId)
            "startMirror" -> parseStartMirror(elementId)
            "openRole" -> parseOpenRole(elementId)
            "murdered" -> parseMurdered(elementId)
            "startAssault" -> parseStartAssault(elementId)
            "survivor" -> parseSurvivor(elementId)
            "counting" -> parseCounting(elementId)
            "suddenDeath" -> parseSuddenDeath(elementId)
            "noMurder" -> parseNoMurder(elementId)
            "winVillage" -> parseWinVillage(elementId)
            "winWolf" -> parseWinWolf(elementId)
            "winHamster" -> parseWinHamster(elementId)
            "playerList" -> parsePlayerList(elementId)
            "panic" -> parsePanic(elementId)
            "execution" -> parseExecution(elementId)
            "vanish" -> parseVanish(elementId)
            "checkout" -> parseCheckout(elementId)
            "shortMember" -> parseShortMember(elementId)
            "askEntry" -> parseAskEntry(elementId)
            "askCommit" -> parseAskCommit(elementId)
            "noComment" -> parseNoComment(elementId)
            "stayEpilogue" -> parseStayEpilogue(elementId)
            "gameOver" -> parseGameOver(elementId)
            "judge" -> parseJudge(elementId)
            "guard" -> parseGuard(elementId)
            "counting2" -> parseCounting2(elementId)
            "assault" -> parseAssault(elementId)
            else -> null
        }
    }

    // --- text content ------------------------------------------------------

    private fun parseMessageLines(): List<String> {
        val lines = mutableListOf<String>()
        eachChild { name ->
            if (name == "li") lines.add(parseLi()) else skipToEndTag()
        }
        return lines
    }

    /** Reads the mixed content of an `<li>` element: text interspersed with `<rawdata>`. */
    private fun parseLi(): String {
        val text = StringBuilder()
        while (true) {
            when (parser.next()) {
                EventType.TEXT, EventType.CDSECT -> text.append(parser.text)
                EventType.START_TAG -> {
                    if (parser.name == "rawdata") {
                        text.append(parser.nextText())
                    } else {
                        skipToEndTag()
                    }
                }
                EventType.END_TAG -> return text.toString()
                EventType.END_DOCUMENT -> throw InvalidArchiveException("Unexpected end of document.")
                else -> {}
            }
        }
    }

    // --- talk ----------------------------------------------------------------

    private fun parseTalk(elementId: String): Talk {
        val talkType = parseTalkType(requiredAttr("type"))
        val avatarId = requiredAttr("avatarId")
        val xname = requiredAttr("xname")
        val time = parseTime(requiredAttr("time"))
        val messageLines = parseMessageLines()
        val talkNo = if (talkType == TalkType.PUBLIC) {
            publicTalkCount += 1
            publicTalkCount
        } else {
            null
        }
        return Talk(elementId, talkType, avatarId, xname, time, talkNo, messageLines)
    }

    // --- announce family -------------------------------------------------

    private fun parseStartEntry(elementId: String) = StartEntry(elementId, parseMessageLines())
    private fun parseStartMirror(elementId: String) = StartMirror(elementId, parseMessageLines())
    private fun parseStartAssault(elementId: String) = StartAssault(elementId, parseMessageLines())
    private fun parseNoMurder(elementId: String) = NoMurder(elementId, parseMessageLines())
    private fun parseWinVillage(elementId: String) = WinVillage(elementId, parseMessageLines())
    private fun parseWinWolf(elementId: String) = WinWolf(elementId, parseMessageLines())
    private fun parseWinHamster(elementId: String) = WinHamster(elementId, parseMessageLines())
    private fun parsePanic(elementId: String) = Panic(elementId, parseMessageLines())
    private fun parseShortMember(elementId: String) = ShortMember(elementId, parseMessageLines())

    private fun parseOnStage(elementId: String): OnStage {
        val entryNo = requiredIntAttr("entryNo")
        val avatarId = requiredAttr("avatarId")
        return OnStage(elementId, parseMessageLines(), entryNo, avatarId)
    }

    private fun parseSuddenDeath(elementId: String): SuddenDeath {
        val avatarId = requiredAttr("avatarId")
        return SuddenDeath(elementId, parseMessageLines(), avatarId)
    }

    private fun parseVanish(elementId: String): Vanish {
        val avatarId = requiredAttr("avatarId")
        return Vanish(elementId, parseMessageLines(), avatarId)
    }

    private fun parseCheckout(elementId: String): Checkout {
        val avatarId = requiredAttr("avatarId")
        return Checkout(elementId, parseMessageLines(), avatarId)
    }

    private fun parseOpenRole(elementId: String): OpenRole {
        val messageLines = mutableListOf<String>()
        val roleHeads = mutableMapOf<Role, Int>()
        eachChild { name ->
            when (name) {
                "li" -> messageLines.add(parseLi())
                "roleHeads" -> {
                    val role = parseRole(requiredAttr("role"))
                    val heads = requiredIntAttr("heads")
                    skipToEndTag()
                    roleHeads[role] = heads
                }
                else -> skipToEndTag()
            }
        }
        return OpenRole(elementId, messageLines, roleHeads)
    }

    private fun parseMurdered(elementId: String): Murdered {
        val messageLines = mutableListOf<String>()
        val avatarIds = mutableListOf<String>()
        eachChild { name ->
            when (name) {
                "li" -> messageLines.add(parseLi())
                "avatarRef" -> {
                    avatarIds.add(requiredAttr("avatarId"))
                    skipToEndTag()
                }
                else -> skipToEndTag()
            }
        }
        return Murdered(elementId, messageLines, avatarIds)
    }

    private fun parseSurvivor(elementId: String): Survivor {
        val messageLines = mutableListOf<String>()
        val avatarIds = mutableListOf<String>()
        eachChild { name ->
            when (name) {
                "li" -> messageLines.add(parseLi())
                "avatarRef" -> {
                    avatarIds.add(requiredAttr("avatarId"))
                    skipToEndTag()
                }
                else -> skipToEndTag()
            }
        }
        return Survivor(elementId, messageLines, avatarIds)
    }

    private fun parseCounting(elementId: String): Counting {
        val victim = optionalAttr("victim")
        val messageLines = mutableListOf<String>()
        val votes = mutableMapOf<String, String>()
        eachChild { name ->
            when (name) {
                "li" -> messageLines.add(parseLi())
                "vote" -> {
                    val byWhom = requiredAttr("byWhom")
                    val target = requiredAttr("target")
                    skipToEndTag()
                    votes[byWhom] = target
                }
                else -> skipToEndTag()
            }
        }
        return Counting(elementId, messageLines, victim, votes)
    }

    private fun parsePlayerList(elementId: String): PlayerList {
        val messageLines = mutableListOf<String>()
        val players = mutableListOf<Player>()
        eachChild { name ->
            when (name) {
                "li" -> messageLines.add(parseLi())
                "playerInfo" -> players.add(parsePlayerInfo())
                else -> skipToEndTag()
            }
        }
        return PlayerList(elementId, messageLines, players)
    }

    private fun parsePlayerInfo(): Player {
        val playerId = requiredAttr("playerId")
        val avatarId = requiredAttr("avatarId")
        val survive = parseBoolean(requiredAttr("survive"))
        val role = parseRole(requiredAttr("role"))
        val uri = optionalAttr("uri")
        skipToEndTag()
        return Player(playerId, avatarId, survive, role, uri)
    }

    private fun parseExecution(elementId: String): Execution {
        val victim = optionalAttr("victim")
        val messageLines = mutableListOf<String>()
        val nominated = mutableMapOf<String, Int>()
        eachChild { name ->
            when (name) {
                "li" -> messageLines.add(parseLi())
                "nominated" -> {
                    val avatarId = requiredAttr("avatarId")
                    val count = requiredIntAttr("count")
                    skipToEndTag()
                    nominated[avatarId] = count
                }
                else -> skipToEndTag()
            }
        }
        return Execution(elementId, messageLines, victim, nominated)
    }

    // --- order family ------------------------------------------------------

    private fun parseAskEntry(elementId: String) = AskEntry(elementId, parseMessageLines())
    private fun parseAskCommit(elementId: String) = AskCommit(elementId, parseMessageLines())
    private fun parseNoComment(elementId: String) = NoComment(elementId, parseMessageLines())
    private fun parseStayEpilogue(elementId: String) = StayEpilogue(elementId, parseMessageLines())
    private fun parseGameOver(elementId: String) = GameOver(elementId, parseMessageLines())

    // --- extra family ------------------------------------------------------

    private fun parseJudge(elementId: String): Judge {
        val byWhom = requiredAttr("byWhom")
        val target = requiredAttr("target")
        return Judge(elementId, parseMessageLines(), byWhom, target)
    }

    private fun parseGuard(elementId: String): Guard {
        val byWhom = requiredAttr("byWhom")
        val target = requiredAttr("target")
        return Guard(elementId, parseMessageLines(), byWhom, target)
    }

    private fun parseCounting2(elementId: String): Counting2 {
        val messageLines = mutableListOf<String>()
        val votes = mutableMapOf<String, String>()
        eachChild { name ->
            when (name) {
                "li" -> messageLines.add(parseLi())
                "vote" -> {
                    val byWhom = requiredAttr("byWhom")
                    val target = requiredAttr("target")
                    skipToEndTag()
                    votes[byWhom] = target
                }
                else -> skipToEndTag()
            }
        }
        return Counting2(elementId, messageLines, votes)
    }

    private fun parseAssault(elementId: String): Assault {
        val byWhom = requiredAttr("byWhom")
        val target = requiredAttr("target")
        val xname = requiredAttr("xname")
        val time = parseTime(requiredAttr("time"))
        return Assault(elementId, parseMessageLines(), byWhom, target, xname, time)
    }
}
