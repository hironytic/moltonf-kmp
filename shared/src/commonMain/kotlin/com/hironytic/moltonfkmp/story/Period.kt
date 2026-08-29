package com.hironytic.moltonfkmp.story

import kotlinx.serialization.Serializable

@Serializable
data class Period(
    val type: PeriodType,
    val day: Int,
    val elements: List<StoryElement>,
)
