package com.hironytic.moltonfkmp.story

data class Character(
    val avatar: Avatar,
    val role: Role,
    /** The day this character survives until that day */
    val aliveUntil: Int,
)

typealias CharacterMap = Map<String, Character>

fun createCharacterMap(story: Story): CharacterMap {
    class MutableCharacter(val avatar: Avatar, var role: Role, var aliveUntil: Int)

    val map = story.avatarList.associateTo(LinkedHashMap()) {
        it.avatarId to MutableCharacter(it, Role.INNOCENT, aliveUntil = 1)
    }

    for (period in story.periods) {
        for (element in period.elements) {
            when (element) {
                is Survivor -> {
                    for (avatarId in element.avatarIds) {
                        map[avatarId]?.aliveUntil = period.day
                    }
                }

                is PlayerList -> {
                    for (player in element.players) {
                        map[player.avatarId]?.let {
                            it.role = player.role
                            if (player.survive) {
                                it.aliveUntil = period.day
                            }
                        }
                    }
                }

                else -> {}
            }
        }
    }

    return map.mapValues { (_, it) -> Character(it.avatar, it.role, it.aliveUntil) }
}
