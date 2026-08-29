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
)
