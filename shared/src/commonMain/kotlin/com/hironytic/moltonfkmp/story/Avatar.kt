package com.hironytic.moltonfkmp.story

import kotlinx.serialization.Serializable

@Serializable
data class Avatar(
    val avatarId: String,
    val fullName: String,
    val shortName: String,
    val faceIconURI: String? = null,
)
