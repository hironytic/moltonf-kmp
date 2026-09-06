package com.hironytic.moltonfkmp.ui.newworkspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hironytic.moltonfkmp.storage.NewWorkspace
import com.hironytic.moltonfkmp.storage.WorkspaceStore
import com.hironytic.moltonfkmp.story.Character
import com.hironytic.moltonfkmp.story.CharacterMap
import com.hironytic.moltonfkmp.story.Role
import com.hironytic.moltonfkmp.story.Story
import com.hironytic.moltonfkmp.story.createCharacterMap
import kotlin.random.Random
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NewWorkspaceStep {
    SELECT_STORY,
    SELECT_TEAM,
    SELECT_ROLE_OF_VILLAGER,
    SELECT_ROLE_OF_WOLF,
    INPUT_NAME,
    CONFIRM,
}

enum class TeamOption { VILLAGER, WOLF, HAMSTER, ANYTHING }
enum class VillagerRoleOption { INNOCENT, SEER, SHAMAN, HUNTER, FRATER, LONGEST_SURVIVOR, ANYTHING }
enum class WolfRoleOption { WOLF, MADMAN, LONGEST_SURVIVOR, ANYTHING }

private val VILLAGER_ROLES = setOf(Role.INNOCENT, Role.SEER, Role.SHAMAN, Role.HUNTER, Role.FRATER)
private val WOLF_ROLES = setOf(Role.WOLF, Role.MADMAN)

class NewWorkspaceViewModel(
    private val workspaceStore: WorkspaceStore,
) : ViewModel() {
    private var story: Story? = null
    private var characterMap: CharacterMap = emptyMap()

    private val _step = MutableStateFlow(NewWorkspaceStep.SELECT_STORY)
    val step: StateFlow<NewWorkspaceStep> = _step.asStateFlow()

    private val _storyName = MutableStateFlow<String?>(null)
    val storyName: StateFlow<String?> = _storyName.asStateFlow()

    private val _archiveLoadError = MutableStateFlow(false)
    val archiveLoadError: StateFlow<Boolean> = _archiveLoadError.asStateFlow()

    private val _teamOptions = MutableStateFlow<List<TeamOption>>(emptyList())
    val teamOptions: StateFlow<List<TeamOption>> = _teamOptions.asStateFlow()

    private val _villagerRoleOptions = MutableStateFlow<List<VillagerRoleOption>>(emptyList())
    val villagerRoleOptions: StateFlow<List<VillagerRoleOption>> = _villagerRoleOptions.asStateFlow()

    private val _wolfRoleOptions = MutableStateFlow<List<WolfRoleOption>>(emptyList())
    val wolfRoleOptions: StateFlow<List<WolfRoleOption>> = _wolfRoleOptions.asStateFlow()

    private val _team = MutableStateFlow<TeamOption?>(null)
    val team: StateFlow<TeamOption?> = _team.asStateFlow()

    private val _villagerRole = MutableStateFlow<VillagerRoleOption?>(null)
    val villagerRole: StateFlow<VillagerRoleOption?> = _villagerRole.asStateFlow()

    private val _wolfRole = MutableStateFlow<WolfRoleOption?>(null)
    val wolfRole: StateFlow<WolfRoleOption?> = _wolfRole.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    val canForwardFromSelectTeamStep: StateFlow<Boolean> =
        combine(_team, _teamOptions) { team, options -> team != null && team in options }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val canForwardFromSelectRoleOfVillagerStep: StateFlow<Boolean> =
        combine(_villagerRole, _villagerRoleOptions) { role, options -> role != null && role in options }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val canForwardFromSelectRoleOfWolfStep: StateFlow<Boolean> =
        combine(_wolfRole, _wolfRoleOptions) { role, options -> role != null && role in options }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val canForwardFromInputNameStep: StateFlow<Boolean> =
        _name.map { it.isNotEmpty() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _registering = MutableStateFlow(false)
    val registering: StateFlow<Boolean> = _registering.asStateFlow()

    private val _registeredWorkspaceId = MutableStateFlow<String?>(null)
    val registeredWorkspaceId: StateFlow<String?> = _registeredWorkspaceId.asStateFlow()

    fun onArchiveLoaded(loadedStory: Story) {
        story = loadedStory
        characterMap = createCharacterMap(loadedStory)
        _storyName.value = loadedStory.villageFullName
        updateOptions()
        _step.value = NewWorkspaceStep.SELECT_TEAM
    }

    fun onArchiveLoadFailed() {
        _archiveLoadError.value = true
    }

    fun dismissArchiveLoadError() {
        _archiveLoadError.value = false
    }

    fun forwardFromSelectStoryStep() {
        if (story != null) {
            _step.value = NewWorkspaceStep.SELECT_TEAM
        }
    }

    private fun updateOptions() {
        val characters = characterMap.values
        _teamOptions.value = buildList {
            add(TeamOption.VILLAGER)
            add(TeamOption.WOLF)
            if (characters.any { it.role == Role.HAMSTER }) add(TeamOption.HAMSTER)
            add(TeamOption.ANYTHING)
        }
        _villagerRoleOptions.value = buildList {
            if (characters.any { it.role == Role.INNOCENT }) add(VillagerRoleOption.INNOCENT)
            if (characters.any { it.role == Role.SEER }) add(VillagerRoleOption.SEER)
            if (characters.any { it.role == Role.SHAMAN }) add(VillagerRoleOption.SHAMAN)
            if (characters.any { it.role == Role.HUNTER }) add(VillagerRoleOption.HUNTER)
            if (characters.any { it.role == Role.FRATER }) add(VillagerRoleOption.FRATER)
            add(VillagerRoleOption.LONGEST_SURVIVOR)
            add(VillagerRoleOption.ANYTHING)
        }
        _wolfRoleOptions.value = buildList {
            if (characters.any { it.role == Role.WOLF }) add(WolfRoleOption.WOLF)
            if (characters.any { it.role == Role.MADMAN }) add(WolfRoleOption.MADMAN)
            add(WolfRoleOption.LONGEST_SURVIVOR)
            add(WolfRoleOption.ANYTHING)
        }
    }

    fun backFromSelectTeamStep() {
        _step.value = NewWorkspaceStep.SELECT_STORY
    }

    fun selectTeam(option: TeamOption) {
        _team.value = option
        forwardFromSelectTeamStep()
    }

    fun forwardFromSelectTeamStep() {
        when (_team.value) {
            TeamOption.VILLAGER -> _step.value = NewWorkspaceStep.SELECT_ROLE_OF_VILLAGER
            TeamOption.WOLF -> _step.value = NewWorkspaceStep.SELECT_ROLE_OF_WOLF
            TeamOption.ANYTHING, TeamOption.HAMSTER -> moveToInputNameStep()
            null -> {}
        }
    }

    fun backFromSelectRoleOfVillagerStep() {
        _step.value = NewWorkspaceStep.SELECT_TEAM
    }

    fun selectVillagerRole(option: VillagerRoleOption) {
        _villagerRole.value = option
        forwardFromSelectRoleOfVillagerStep()
    }

    fun forwardFromSelectRoleOfVillagerStep() {
        moveToInputNameStep()
    }

    fun backFromSelectRoleOfWolfStep() {
        _step.value = NewWorkspaceStep.SELECT_TEAM
    }

    fun selectWolfRole(option: WolfRoleOption) {
        _wolfRole.value = option
        forwardFromSelectRoleOfWolfStep()
    }

    fun forwardFromSelectRoleOfWolfStep() {
        moveToInputNameStep()
    }

    private fun moveToInputNameStep() {
        val role = roleNameOf(_team.value, _villagerRole.value, _wolfRole.value)
        _name.value = (story?.villageFullName ?: "") + if (role.isNotEmpty()) "（$role）" else ""
        _step.value = NewWorkspaceStep.INPUT_NAME
    }

    private fun roleNameOf(
        team: TeamOption?,
        villagerRole: VillagerRoleOption?,
        wolfRole: WolfRoleOption?,
    ): String = when (team) {
        TeamOption.VILLAGER -> when (villagerRole) {
            VillagerRoleOption.INNOCENT -> "ただの村人"
            VillagerRoleOption.SEER -> "占い師"
            VillagerRoleOption.SHAMAN -> "霊能者"
            VillagerRoleOption.HUNTER -> "狩人"
            VillagerRoleOption.FRATER -> "共有者"
            else -> "村人側"
        }

        TeamOption.WOLF -> when (wolfRole) {
            WolfRoleOption.WOLF -> "人狼"
            WolfRoleOption.MADMAN -> "狂人"
            else -> "人狼側"
        }

        TeamOption.HAMSTER -> "ハムスター人間"
        else -> ""
    }

    fun updateName(newName: String) {
        _name.value = newName
    }

    fun backFromInputNameStep() {
        _step.value = when (_team.value) {
            TeamOption.VILLAGER -> NewWorkspaceStep.SELECT_ROLE_OF_VILLAGER
            TeamOption.WOLF -> NewWorkspaceStep.SELECT_ROLE_OF_WOLF
            else -> NewWorkspaceStep.SELECT_TEAM
        }
    }

    fun forwardFromInputNameStep() {
        _step.value = NewWorkspaceStep.CONFIRM
    }

    fun backFromConfirmStep() {
        _step.value = NewWorkspaceStep.INPUT_NAME
    }

    private fun shuffleCharacters(): List<Character> {
        val characters = characterMap.values.filter { it.avatar.avatarId != "gerd" }.toMutableList()
        for (i in characters.size - 1 downTo 1) {
            val j = Random.nextInt(i + 1)
            val tmp = characters[i]
            characters[i] = characters[j]
            characters[j] = tmp
        }
        return characters
    }

    private fun pickCharacter(): Character? {
        val characters = shuffleCharacters()
        return when (_team.value) {
            TeamOption.VILLAGER -> when (_villagerRole.value) {
                VillagerRoleOption.INNOCENT -> characters.find { it.role == Role.INNOCENT }
                VillagerRoleOption.SEER -> characters.find { it.role == Role.SEER }
                VillagerRoleOption.SHAMAN -> characters.find { it.role == Role.SHAMAN }
                VillagerRoleOption.HUNTER -> characters.find { it.role == Role.HUNTER }
                VillagerRoleOption.FRATER -> characters.find { it.role == Role.FRATER }
                VillagerRoleOption.LONGEST_SURVIVOR ->
                    characters.filter { it.role in VILLAGER_ROLES }.sortedByDescending { it.aliveUntil }.firstOrNull()

                VillagerRoleOption.ANYTHING -> characters.find { it.role in VILLAGER_ROLES }
                null -> null
            }

            TeamOption.WOLF -> when (_wolfRole.value) {
                WolfRoleOption.WOLF -> characters.find { it.role == Role.WOLF }
                WolfRoleOption.MADMAN -> characters.find { it.role == Role.MADMAN }
                WolfRoleOption.LONGEST_SURVIVOR ->
                    characters.filter { it.role in WOLF_ROLES }.sortedByDescending { it.aliveUntil }.firstOrNull()

                WolfRoleOption.ANYTHING -> characters.find { it.role in WOLF_ROLES }
                null -> null
            }

            TeamOption.HAMSTER -> characters.find { it.role == Role.HAMSTER }
            TeamOption.ANYTHING -> characters.firstOrNull()
            null -> null
        }
    }

    fun registerNewWorkspace() {
        val currentStory = story ?: return
        val character = pickCharacter() ?: return
        viewModelScope.launch {
            _registering.value = true
            val workspace = workspaceStore.add(
                currentStory,
                NewWorkspace(
                    name = _name.value,
                    currentDay = 0,
                    dayProgress = 0,
                    playerCharacter = character.avatar.avatarId,
                    lastModified = Clock.System.now(),
                ),
            )
            _registering.value = false
            _registeredWorkspaceId.value = workspace.id
        }
    }
}
