package com.hironytic.moltonfkmp.story

data class TalkWithDay(
    val day: Int,
    val talk: Talk,
)

interface TalkMap {
    fun getTalkByTalkNo(talkNo: Int): TalkWithDay?
    fun getTalkByTime(day: Int, hour: Int, minute: Int): List<Talk>
}

fun createTalkMap(story: Story): TalkMap {
    fun timeKey(day: Int, hour: Int, minute: Int): String = "$day-$hour-$minute"

    val talkNoMap = mutableMapOf<Int, TalkWithDay>()
    val timeMap = mutableMapOf<String, MutableList<Talk>>()

    for (period in story.periods) {
        val day = period.day
        for (element in period.elements) {
            if (element is Talk) {
                val talkNo = element.talkNo
                if (talkNo != null) {
                    talkNoMap[talkNo] = TalkWithDay(day, element)
                }
                val key = timeKey(day, hourPartOf(element.time), minutePartOf(element.time))
                timeMap.getOrPut(key) { mutableListOf() }.add(element)
            }
        }
    }

    return object : TalkMap {
        override fun getTalkByTalkNo(talkNo: Int): TalkWithDay? = talkNoMap[talkNo]

        override fun getTalkByTime(day: Int, hour: Int, minute: Int): List<Talk> =
            timeMap[timeKey(day, hour, minute)] ?: emptyList()
    }
}

fun nullTalkMap(): TalkMap = object : TalkMap {
    override fun getTalkByTalkNo(talkNo: Int): TalkWithDay? = null
    override fun getTalkByTime(day: Int, hour: Int, minute: Int): List<Talk> = emptyList()
}
