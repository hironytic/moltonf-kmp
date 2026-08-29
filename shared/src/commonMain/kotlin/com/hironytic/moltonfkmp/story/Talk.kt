package com.hironytic.moltonfkmp.story

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("talk")
data class Talk(
    override val elementId: String,
    val talkType: TalkType,
    val avatarId: String,
    val xname: String,
    val time: Int,
    val talkNo: Int? = null,
    val messageLines: List<String>,
) : StoryElement
