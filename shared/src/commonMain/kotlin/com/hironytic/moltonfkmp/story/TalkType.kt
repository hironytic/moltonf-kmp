package com.hironytic.moltonfkmp.story

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TalkType {
    @SerialName("public") PUBLIC,
    @SerialName("wolf") WOLF,
    @SerialName("private") PRIVATE,
    @SerialName("grave") GRAVE,
}
