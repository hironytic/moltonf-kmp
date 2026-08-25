package com.hironytic.moltonfkmp.story

import kotlinx.serialization.Serializable

@Serializable
data class Player(
    val playerId: String,
    val avatarId: String,
    val survive: Boolean,
    val role: Role,
    val uri: String? = null,
)
