package com.hironytic.moltonfkmp.ui.watching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hironytic.moltonfkmp.storage.WorkspaceStore
import com.hironytic.moltonfkmp.story.CharacterMap
import com.hironytic.moltonfkmp.story.PeriodType
import com.hironytic.moltonfkmp.story.Story
import com.hironytic.moltonfkmp.story.Talk
import com.hironytic.moltonfkmp.story.TalkMap
import com.hironytic.moltonfkmp.story.TalkWithDay
import com.hironytic.moltonfkmp.story.createCharacterMap
import com.hironytic.moltonfkmp.story.createTalkMap
import com.hironytic.moltonfkmp.story.nullTalkMap
import com.hironytic.moltonfkmp.workspace.Workspace
import kotlin.time.Clock
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WatchableDay(val day: Int, val text: String)

sealed interface WatchingUiState {
    data object Loading : WatchingUiState

    data class Loaded(
        val story: Story,
        val characterMap: CharacterMap,
        val talkMap: TalkMap,
        val playerCharacter: String,
        val dayProgress: Int?,
        val watchableDays: List<WatchableDay>,
        val currentDay: Int,
        val canMoveToNextDay: Boolean,
        val elements: List<WatchingElement>,
    ) : WatchingUiState
}

private const val PROLOGUE_NAME = "プロローグ"
private const val EPILOGUE_NAME = "エピローグ"

class WatchingViewModel(
    private val workspaceStore: WorkspaceStore,
    private val workspaceId: String,
) : ViewModel() {
    private lateinit var workspace: Workspace
    private var story: Story? = null
    private var characterMap: CharacterMap = emptyMap()
    private var talkMap: TalkMap = nullTalkMap()

    private val _uiState = MutableStateFlow<WatchingUiState>(WatchingUiState.Loading)
    val uiState: StateFlow<WatchingUiState> = _uiState.asStateFlow()

    private val _talkThread = MutableStateFlow<TalkThreadEntry?>(null)
    val talkThread: StateFlow<TalkThreadEntry?> = _talkThread.asStateFlow()

    init {
        viewModelScope.launch {
            val loadedWorkspace = workspaceStore.getWorkspace(workspaceId) ?: return@launch
            workspace = loadedWorkspace
            val loadedStory = workspaceStore.getStory(loadedWorkspace.storyId) ?: return@launch
            story = loadedStory
            characterMap = createCharacterMap(loadedStory)
            talkMap = createTalkMap(loadedStory)
            publishState()
        }
    }

    private fun publishState() {
        val currentStory = story ?: return
        val dayProgress = workspace.dayProgress
        val periods = if (dayProgress == null) currentStory.periods else currentStory.periods.take(dayProgress + 1)
        val watchableDays = periods.mapIndexed { day, period ->
            val text = when (period.type) {
                PeriodType.PROLOGUE -> PROLOGUE_NAME
                PeriodType.EPILOGUE -> EPILOGUE_NAME
                PeriodType.PROGRESS -> "${day}日目"
            }
            WatchableDay(day, text)
        }
        val currentDay = workspace.currentDay
        _uiState.value = WatchingUiState.Loaded(
            story = currentStory,
            characterMap = characterMap,
            talkMap = talkMap,
            playerCharacter = workspace.playerCharacter,
            dayProgress = dayProgress,
            watchableDays = watchableDays,
            currentDay = currentDay,
            canMoveToNextDay = currentDay < currentStory.periods.size - 1,
            elements = currentElements(currentStory, characterMap, workspace.playerCharacter, dayProgress, currentDay),
        )
    }

    private fun updateWorkspace(update: (Workspace) -> Workspace) {
        workspace = update(workspace).copy(lastModified = Clock.System.now())
        publishState()
        viewModelScope.launch {
            workspaceStore.update(workspace)
        }
    }

    fun changeDay(day: Int) {
        val currentStory = story ?: return
        val dayProgress = workspace.dayProgress
        val clampedDay = if (dayProgress != null) minOf(day, dayProgress) else minOf(day, currentStory.periods.size - 1)
        if (clampedDay == workspace.currentDay) return
        updateWorkspace { it.copy(currentDay = clampedDay) }
    }

    fun moveToNextDay() {
        val currentStory = story ?: return
        val day = workspace.currentDay
        if (day + 1 >= currentStory.periods.size) return

        val dayProgress = workspace.dayProgress
        updateWorkspace {
            val nextDayProgress = if (dayProgress != null && dayProgress < day + 1) {
                if (day + 1 >= currentStory.periods.size - 1) null else day + 1
            } else {
                dayProgress
            }
            it.copy(currentDay = day + 1, dayProgress = nextDayProgress)
        }
    }

    private fun nextId(): String = Uuid.random().toString()

    fun openTalkThread(day: Int, talk: Talk, linkKey: String, targets: List<TalkWithDay>) {
        _talkThread.value = createTalkThreadRoot(nextId(), day, talk, linkKey, targets, ::nextId)
    }

    fun toggleThreadLink(path: List<String>, linkKey: String, targets: List<TalkWithDay>) {
        _talkThread.value = _talkThread.value?.toggleLink(path, linkKey, targets, ::nextId)
    }

    fun closeTalkThread() {
        _talkThread.value = null
    }
}
