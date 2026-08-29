package com.hironytic.moltonfkmp.story

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class Role {
    @SerialName("innocent") INNOCENT,
    @SerialName("wolf") WOLF,
    @SerialName("seer") SEER,
    @SerialName("shaman") SHAMAN,
    @SerialName("madman") MADMAN,
    @SerialName("hunter") HUNTER,
    @SerialName("frater") FRATER,
    @SerialName("hamster") HAMSTER,
}
