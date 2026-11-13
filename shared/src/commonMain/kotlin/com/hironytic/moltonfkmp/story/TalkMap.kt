package com.hironytic.moltonfkmp.story

data class TalkWithDay(
    val day: Int,
    val talk: Talk,
)

interface TalkMap {
    fun getTalkByTalkNo(talkNo: Int): TalkWithDay?
    fun getTalkByTime(day: Int, hour: Int, minute: Int): List<Talk>

    /**
     * Returns how many talks of the same [TalkType] were made on [day] up to and including the
     * talk identified by [elementId] (1-based), or null if no such talk exists.
     */
    fun getSpeechCount(day: Int, elementId: String): Int?
}

fun createTalkMap(story: Story): TalkMap {
    fun timeKey(day: Int, hour: Int, minute: Int): String = "$day-$hour-$minute"

    val talkNoMap = mutableMapOf<Int, TalkWithDay>()
    val timeMap = mutableMapOf<String, MutableList<Talk>>()
    val speechCountMap = mutableMapOf<String, Int>()

    for (period in story.periods) {
        val day = period.day
        val typeCounts = mutableMapOf<TalkType, Int>()
        for (element in period.elements) {
            if (element is Talk) {
                val count = (typeCounts[element.talkType] ?: 0) + 1
                typeCounts[element.talkType] = count
                speechCountMap["$day/${element.elementId}"] = count
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

        override fun getSpeechCount(day: Int, elementId: String): Int? = speechCountMap["$day/$elementId"]
    }
}

fun nullTalkMap(): TalkMap = object : TalkMap {
    override fun getTalkByTalkNo(talkNo: Int): TalkWithDay? = null
    override fun getTalkByTime(day: Int, hour: Int, minute: Int): List<Talk> = emptyList()
    override fun getSpeechCount(day: Int, elementId: String): Int? = null
}
