package com.hironytic.moltonfkmp.ui.watching

import com.hironytic.moltonfkmp.story.TalkMap
import com.hironytic.moltonfkmp.story.TalkWithDay

sealed interface MessageSegment {
    val text: String

    data class General(override val text: String) : MessageSegment
    data class LinkToTalk(override val text: String, val talks: List<TalkWithDay>) : MessageSegment
}

private val talkNoAnchorRegex = Regex(""">>([0-9]+)""")
private val timeMentionRegex = Regex("""(([0-9]+)d)?([0-9][0-9]):?([0-9][0-9])""", RegexOption.IGNORE_CASE)

/**
 * Splits [text] into segments, turning `>>123`-style talk-number anchors and
 * `3d12:34`/`12:34`-style time mentions into [MessageSegment.LinkToTalk] segments
 * when they resolve to a talk that is currently visible.
 */
fun parseMessageSegments(
    text: String,
    currentDay: Int?,
    talkMap: TalkMap,
    isTalkVisible: (TalkWithDay) -> Boolean,
): List<MessageSegment> {
    val segments = mutableListOf<MessageSegment>()
    var begin = 0
    var cur = 0

    fun pushGeneral() {
        if (begin < cur) {
            segments.add(MessageSegment.General(text.substring(begin, cur)))
            begin = cur
        }
    }

    while (cur < text.length) {
        var isMatched = false

        if (text[cur] == '>') {
            val match = talkNoAnchorRegex.matchAt(text, cur)
            if (match != null) {
                val talkNo = match.groupValues[1].toIntOrNull()
                val talkWithDay = talkNo?.let { talkMap.getTalkByTalkNo(it) }
                if (talkWithDay != null && isTalkVisible(talkWithDay)) {
                    pushGeneral()
                    val matchedText = match.value
                    segments.add(MessageSegment.LinkToTalk(matchedText, listOf(talkWithDay)))
                    cur += matchedText.length
                    begin = cur
                    isMatched = true
                }
            }
        } else if (text[cur] in '0'..'9') {
            val match = timeMentionRegex.matchAt(text, cur)
            if (match != null) {
                val dayInMessage = match.groupValues[2].toIntOrNull()
                val hour = match.groupValues[3].toIntOrNull()
                val minute = match.groupValues[4].toIntOrNull()
                if (hour != null && minute != null) {
                    val day = dayInMessage ?: currentDay
                    if (day != null) {
                        val talks = talkMap.getTalkByTime(day, hour, minute)
                            .map { TalkWithDay(day, it) }
                            .filter(isTalkVisible)
                        if (talks.isNotEmpty()) {
                            pushGeneral()
                            val matchedText = match.value
                            segments.add(MessageSegment.LinkToTalk(matchedText, talks))
                            cur += matchedText.length
                            begin = cur
                            isMatched = true
                        }
                    }
                }
            }
        }

        if (!isMatched) {
            cur += 1
        }
    }
    pushGeneral()
    return segments
}
