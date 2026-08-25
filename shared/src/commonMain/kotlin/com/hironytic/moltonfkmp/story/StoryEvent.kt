package com.hironytic.moltonfkmp.story

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface StoryEvent : StoryElement {
    val messageLines: List<String>
}

@Serializable
sealed interface StoryEventAnnounce : StoryEvent

@Serializable
sealed interface StoryEventOrder : StoryEvent

@Serializable
sealed interface StoryEventExtra : StoryEvent

// --- announce family ---

@Serializable
@SerialName("startEntry")
data class StartEntry(
    override val elementId: String,
    override val messageLines: List<String>,
) : StoryEventAnnounce

@Serializable
@SerialName("onStage")
data class OnStage(
    override val elementId: String,
    override val messageLines: List<String>,
    val entryNo: Int,
    val avatarId: String,
) : StoryEventAnnounce

@Serializable
@SerialName("startMirror")
data class StartMirror(
    override val elementId: String,
    override val messageLines: List<String>,
) : StoryEventAnnounce

@Serializable
@SerialName("openRole")
data class OpenRole(
    override val elementId: String,
    override val messageLines: List<String>,
    val roleHeads: Map<Role, Int>,
) : StoryEventAnnounce

@Serializable
@SerialName("murdered")
data class Murdered(
    override val elementId: String,
    override val messageLines: List<String>,
    val avatarIds: List<String>,
) : StoryEventAnnounce

@Serializable
@SerialName("startAssault")
data class StartAssault(
    override val elementId: String,
    override val messageLines: List<String>,
) : StoryEventAnnounce

@Serializable
@SerialName("survivor")
data class Survivor(
    override val elementId: String,
    override val messageLines: List<String>,
    val avatarIds: List<String>,
) : StoryEventAnnounce

@Serializable
@SerialName("counting")
data class Counting(
    override val elementId: String,
    override val messageLines: List<String>,
    val victim: String? = null,
    val votes: Map<String, String>,
) : StoryEventAnnounce

@Serializable
@SerialName("suddenDeath")
data class SuddenDeath(
    override val elementId: String,
    override val messageLines: List<String>,
    val avatarId: String,
) : StoryEventAnnounce

@Serializable
@SerialName("noMurder")
data class NoMurder(
    override val elementId: String,
    override val messageLines: List<String>,
) : StoryEventAnnounce

@Serializable
@SerialName("winVillage")
data class WinVillage(
    override val elementId: String,
    override val messageLines: List<String>,
) : StoryEventAnnounce

@Serializable
@SerialName("winWolf")
data class WinWolf(
    override val elementId: String,
    override val messageLines: List<String>,
) : StoryEventAnnounce

@Serializable
@SerialName("winHamster")
data class WinHamster(
    override val elementId: String,
    override val messageLines: List<String>,
) : StoryEventAnnounce

@Serializable
@SerialName("playerList")
data class PlayerList(
    override val elementId: String,
    override val messageLines: List<String>,
    val players: List<Player>,
) : StoryEventAnnounce

@Serializable
@SerialName("panic")
data class Panic(
    override val elementId: String,
    override val messageLines: List<String>,
) : StoryEventAnnounce

@Serializable
@SerialName("execution")
data class Execution(
    override val elementId: String,
    override val messageLines: List<String>,
    val victim: String? = null,
    val nominated: Map<String, Int>,
) : StoryEventAnnounce

@Serializable
@SerialName("vanish")
data class Vanish(
    override val elementId: String,
    override val messageLines: List<String>,
    val avatarId: String,
) : StoryEventAnnounce

@Serializable
@SerialName("checkout")
data class Checkout(
    override val elementId: String,
    override val messageLines: List<String>,
    val avatarId: String,
) : StoryEventAnnounce

@Serializable
@SerialName("shortMember")
data class ShortMember(
    override val elementId: String,
    override val messageLines: List<String>,
) : StoryEventAnnounce

// --- order family ---

@Serializable
@SerialName("askEntry")
data class AskEntry(
    override val elementId: String,
    override val messageLines: List<String>,
) : StoryEventOrder

@Serializable
@SerialName("askCommit")
data class AskCommit(
    override val elementId: String,
    override val messageLines: List<String>,
) : StoryEventOrder

@Serializable
@SerialName("noComment")
data class NoComment(
    override val elementId: String,
    override val messageLines: List<String>,
) : StoryEventOrder

@Serializable
@SerialName("stayEpilogue")
data class StayEpilogue(
    override val elementId: String,
    override val messageLines: List<String>,
) : StoryEventOrder

@Serializable
@SerialName("gameOver")
data class GameOver(
    override val elementId: String,
    override val messageLines: List<String>,
) : StoryEventOrder

// --- extra family ---

@Serializable
@SerialName("judge")
data class Judge(
    override val elementId: String,
    override val messageLines: List<String>,
    val byWhom: String,
    val target: String,
) : StoryEventExtra

@Serializable
@SerialName("guard")
data class Guard(
    override val elementId: String,
    override val messageLines: List<String>,
    val byWhom: String,
    val target: String,
) : StoryEventExtra

@Serializable
@SerialName("counting2")
data class Counting2(
    override val elementId: String,
    override val messageLines: List<String>,
    val votes: Map<String, String>,
) : StoryEventExtra

@Serializable
@SerialName("assault")
data class Assault(
    override val elementId: String,
    override val messageLines: List<String>,
    val byWhom: String,
    val target: String,
    val xname: String,
    val time: Int,
) : StoryEventExtra
