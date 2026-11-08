package com.hironytic.moltonfkmp.story

import kotlinx.serialization.Serializable

@Serializable
data class Story(
    val version: Int = 1,
    val villageFullName: String,
    val baseURI: String,
    val landId: String,
    val graveIconURI: String,
    val periods: List<Period>,
    val avatarList: List<Avatar>,
    /**
     * Update time of the village (when each day starts), in milliseconds since midnight like [Talk.time].
     * Null for stories persisted before this value was stored; use [Story.effectiveCommitTime] to read it.
     */
    val commitTime: Int? = null,
)

/**
 * The village's update time, or the time of the first talk on day 1 if [Story.commitTime] is unknown.
 */
fun Story.effectiveCommitTime(): Int? = commitTime
    ?: periods.firstOrNull { it.day == 1 }?.elements?.firstNotNullOfOrNull { it as? Talk }?.time
