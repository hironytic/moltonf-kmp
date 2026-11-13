package com.hironytic.moltonfkmp.ui.watching

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hironytic.moltonfkmp.storage.WorkspaceStore
import com.hironytic.moltonfkmp.story.CharacterMap
import com.hironytic.moltonfkmp.story.PeriodType
import com.hironytic.moltonfkmp.story.Story
import com.hironytic.moltonfkmp.story.Talk
import com.hironytic.moltonfkmp.story.TalkMap
import com.hironytic.moltonfkmp.story.TalkType
import com.hironytic.moltonfkmp.story.TalkWithDay
import com.hironytic.moltonfkmp.story.createCharacterMap
import com.hironytic.moltonfkmp.story.createTalkMap
import com.hironytic.moltonfkmp.story.nullTalkMap
import com.hironytic.moltonfkmp.workspace.Bookmark
import com.hironytic.moltonfkmp.workspace.Workspace
import kotlin.time.Clock
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

enum class WatchingTab { TALK, BOOKMARK, CONSIDERATION }

data class WatchableDay(val day: Int, val text: String)

sealed interface WatchingUiState {
    data object Loading : WatchingUiState

    data class Loaded(
        val workspaceName: String,
        val story: Story,
        val characterMap: CharacterMap,
        val talkMap: TalkMap,
        val playerCharacter: String,
        val dayProgress: Int?,
        val readPosition: Int,
        val watchableDays: List<WatchableDay>,
        val currentDay: Int,
        val canMoveToNextDay: Boolean,
        /** All elements of the current day, ignoring the talk filter. */
        val elements: List<WatchingElement>,
        /** The elements to display after applying [talkFilter], each with its index in [elements]. */
        val displayedElements: List<IndexedElement>,
        val talkFilter: TalkFilter,
        /**
         * True while the talk filter hides the unread part of the newest day (to prevent spoilers),
         * i.e. [displayedElements] stops at the read position although more elements follow.
         */
        val isUnreadHiddenByFilter: Boolean,
        val selectedTab: WatchingTab,
        /** Element to scroll to on the talk tab; cleared by [WatchingViewModel.consumeScrollTarget]. */
        val scrollTargetElementId: String?,
        /** Bookmark to scroll to on the bookmark tab; cleared by [WatchingViewModel.consumeBookmarkScrollTarget]. */
        val scrollTargetBookmarkId: String?,
        val bookmarkItems: List<BookmarkItem>,
        /** Element IDs of the bookmarked talks on [currentDay]. */
        val bookmarkedElementIds: Set<String>,
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

    private var talkFilter = TalkFilter()
    private var selectedTab = WatchingTab.TALK
    private var scrollTargetElementId: String? = null
    private var scrollTargetBookmarkId: String? = null
    private val saveMutex = Mutex()

    private val _uiState = MutableStateFlow<WatchingUiState>(WatchingUiState.Loading)
    val uiState: StateFlow<WatchingUiState> = _uiState.asStateFlow()

    /** Expanded link trees, keyed by "<day>/<elementId>" of the top-level talk they hang under. */
    private val _expandedThreads = MutableStateFlow<Map<String, TalkThreadEntry>>(emptyMap())
    val expandedThreads: StateFlow<Map<String, TalkThreadEntry>> = _expandedThreads.asStateFlow()

    /**
     * One-shot: the id of the first thread entry inserted by the latest link expansion, which the
     * talk tab scrolls into view and then clears with [consumeThreadScrollTarget].
     */
    private val _threadScrollTarget = MutableStateFlow<String?>(null)
    val threadScrollTarget: StateFlow<String?> = _threadScrollTarget.asStateFlow()

    fun consumeThreadScrollTarget() {
        _threadScrollTarget.value = null
    }

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
        val elements = currentElements(currentStory, characterMap, workspace.playerCharacter, dayProgress, currentDay)
        // Filtering shrinks the list, so showing the unread part of the newest day could let the user
        // skip over hidden talks and see what comes later. Only the read part is shown then.
        val isNewestDay = dayProgress != null && currentDay >= dayProgress
        val isUnreadHiddenByFilter = isNewestDay && !talkFilter.isDefault && workspace.readPosition < elements.lastIndex
        var displayedElements = applyTalkFilter(elements, talkFilter)
        if (isNewestDay && !talkFilter.isDefault) {
            displayedElements = displayedElements.filter { it.index <= workspace.readPosition }
        }
        _uiState.value = WatchingUiState.Loaded(
            workspaceName = workspace.name,
            story = currentStory,
            characterMap = characterMap,
            talkMap = talkMap,
            playerCharacter = workspace.playerCharacter,
            dayProgress = dayProgress,
            readPosition = workspace.readPosition,
            watchableDays = watchableDays,
            currentDay = currentDay,
            canMoveToNextDay = currentDay < currentStory.periods.size - 1,
            elements = elements,
            displayedElements = displayedElements,
            talkFilter = talkFilter,
            isUnreadHiddenByFilter = isUnreadHiddenByFilter,
            selectedTab = selectedTab,
            scrollTargetElementId = scrollTargetElementId,
            scrollTargetBookmarkId = scrollTargetBookmarkId,
            bookmarkItems = resolveBookmarks(currentStory, workspace.bookmarks),
            bookmarkedElementIds = workspace.bookmarks.filter { it.day == currentDay }.map { it.elementId }.toSet(),
        )
    }

    private fun updateWorkspace(update: (Workspace) -> Workspace) {
        workspace = update(workspace).copy(lastModified = Clock.System.now())
        publishState()
        persistWorkspace()
    }

    /** Saves the latest workspace; the mutex keeps writes ordered so the last one wins. */
    private fun persistWorkspace() {
        viewModelScope.launch {
            saveMutex.withLock { workspaceStore.update(workspace) }
        }
    }

    fun selectTab(tab: WatchingTab) {
        if (tab == selectedTab) return
        selectedTab = tab
        publishState()
    }

    /**
     * Switches to the talk tab, shows the day of [bookmark] and requests scrolling to its talk.
     * The talk filter is reset so that the talk is not hidden by it.
     */
    fun jumpToBookmark(bookmark: Bookmark) {
        val currentStory = story ?: return
        val dayProgress = workspace.dayProgress
        val maxDay = if (dayProgress != null) minOf(dayProgress, currentStory.periods.size - 1) else currentStory.periods.size - 1
        if (bookmark.day > maxDay) return
        selectedTab = WatchingTab.TALK
        scrollTargetElementId = bookmark.elementId
        talkFilter = TalkFilter()
        if (bookmark.day != workspace.currentDay) {
            updateWorkspace { it.copy(currentDay = bookmark.day) }
        } else {
            publishState()
        }
    }

    fun consumeScrollTarget() {
        if (scrollTargetElementId == null) return
        scrollTargetElementId = null
        publishState()
    }

    /** Switches to the bookmark tab and requests scrolling to the bookmark. */
    fun showBookmark(bookmarkId: String) {
        selectedTab = WatchingTab.BOOKMARK
        scrollTargetBookmarkId = bookmarkId
        publishState()
    }

    fun consumeBookmarkScrollTarget() {
        if (scrollTargetBookmarkId == null) return
        scrollTargetBookmarkId = null
        publishState()
    }

    /** Adds a bookmark on the talk, or updates the comment of the existing one. */
    fun saveBookmark(day: Int, elementId: String, comment: String) {
        updateWorkspace { ws ->
            val existing = ws.bookmarks.firstOrNull { it.day == day && it.elementId == elementId }
            val bookmarks = if (existing != null) {
                ws.bookmarks.map { if (it.id == existing.id) it.copy(comment = comment) else it }
            } else {
                ws.bookmarks + Bookmark(id = nextId(), day = day, elementId = elementId, comment = comment)
            }
            ws.copy(bookmarks = bookmarks)
        }
    }

    fun removeBookmark(bookmarkId: String) {
        updateWorkspace { ws -> ws.copy(bookmarks = ws.bookmarks.filterNot { it.id == bookmarkId }) }
    }

    val memo: String get() = workspace.memo

    /** Saves the consideration memo. Not published to [uiState], so typing does not recompute the screen. */
    fun updateMemo(memo: String) {
        if (memo == workspace.memo) return
        workspace = workspace.copy(memo = memo, lastModified = Clock.System.now())
        persistWorkspace()
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
            it.copy(
                currentDay = day + 1,
                dayProgress = nextDayProgress,
                readPosition = if (nextDayProgress != dayProgress) 0 else it.readPosition,
            )
        }
    }

    /** Records that the user has seen list items up to [index] on the newest revealed day. */
    fun updateReadPosition(index: Int) {
        val dayProgress = workspace.dayProgress ?: return
        if (workspace.currentDay < dayProgress || index <= workspace.readPosition) return
        workspace = workspace.copy(readPosition = index)
        (_uiState.value as? WatchingUiState.Loaded)?.let { _uiState.value = it.copy(readPosition = index) }
        persistWorkspace()
    }

    private fun updateTalkFilter(update: (TalkFilter) -> TalkFilter) {
        talkFilter = update(talkFilter)
        publishState()
    }

    fun toggleAvatarFilter(avatarId: String) = updateTalkFilter {
        val hidden = it.hiddenAvatarIds
        it.copy(hiddenAvatarIds = if (avatarId in hidden) hidden - avatarId else hidden + avatarId)
    }

    fun toggleTalkTypeFilter(talkType: TalkType) = updateTalkFilter {
        val hidden = it.hiddenTalkTypes
        it.copy(hiddenTalkTypes = if (talkType in hidden) hidden - talkType else hidden + talkType)
    }

    /** Shows only [avatarId] among the characters. */
    fun showOnlyAvatar(avatarId: String) = updateTalkFilter { filter ->
        val allIds = story?.avatarList?.map { it.avatarId }.orEmpty()
        filter.copy(hiddenAvatarIds = allIds.filter { it != avatarId }.toSet())
    }

    /** Shows only [talkType] among the talk types. */
    fun showOnlyTalkType(talkType: TalkType) = updateTalkFilter { filter ->
        filter.copy(hiddenTalkTypes = TalkType.entries.filter { it != talkType }.toSet())
    }

    fun resetTalkFilter() = updateTalkFilter { TalkFilter() }

    fun checkAllAvatars() = updateTalkFilter { it.copy(hiddenAvatarIds = emptySet()) }

    fun uncheckAllAvatars() = updateTalkFilter {
        it.copy(hiddenAvatarIds = story?.avatarList?.map { avatar -> avatar.avatarId }?.toSet() ?: emptySet())
    }

    private fun nextId(): String = Uuid.random().toString()

    /**
     * Toggles [linkKey] on the entry reached by [path] below the top-level [talk] of [day]
     * (an empty [path] means the talk itself), expanding or collapsing the linked talks in place.
     */
    fun toggleLink(day: Int, talk: Talk, path: List<String>, linkKey: String, targets: List<TalkWithDay>) {
        val key = threadKey(day, talk.elementId)
        val root = _expandedThreads.value[key] ?: TalkThreadEntry(nextId(), day, talk)
        val oldIds = _expandedThreads.value[key]?.flattenDescendants()?.map { it.entry.id }?.toSet() ?: emptySet()
        val newRoot = root.toggleLink(path, linkKey, targets, ::nextId)
        _expandedThreads.value = _expandedThreads.value + (key to newRoot)
        // Only an expansion adds entries; a collapse leaves no target.
        _threadScrollTarget.value = newRoot.flattenDescendants().firstOrNull { it.entry.id !in oldIds }?.entry?.id
    }

    /** Closes the expanded entry reached by the non-empty [path] below the top-level [talk] of [day]. */
    fun closeThreadEntry(day: Int, talk: Talk, path: List<String>) {
        val key = threadKey(day, talk.elementId)
        val root = _expandedThreads.value[key] ?: return
        _expandedThreads.value = _expandedThreads.value + (key to root.removeEntry(path))
    }
}

fun threadKey(day: Int, elementId: String): String = "$day/$elementId"
