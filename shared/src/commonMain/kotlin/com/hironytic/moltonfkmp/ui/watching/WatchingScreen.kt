package com.hironytic.moltonfkmp.ui.watching

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.hironytic.moltonfkmp.story.Assault
import com.hironytic.moltonfkmp.story.Talk
import com.hironytic.moltonfkmp.story.TalkWithDay
import com.hironytic.moltonfkmp.ui.theme.MoltonfColors
import com.hironytic.moltonfkmp.ui.theme.MoltonfTopAppBar
import com.hironytic.moltonfkmp.ui.theme.NeutralOutlinedButton
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moltonf_kmp.shared.generated.resources.Res
import moltonf_kmp.shared.generated.resources.ic_bookmark
import moltonf_kmp.shared.generated.resources.ic_chat_bubble
import moltonf_kmp.shared.generated.resources.ic_lightbulb
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Lower bounds of the height of a list row, used by the "scroll to bottom" button.
 * A talk row has at least a name line, the 40dp avatar icon and the row's own vertical padding
 * (the name line is about 16dp at the default font size; kept a little below the real value).
 * Other rows (events, synthesized messages) are just a bordered text.
 */
private val MIN_TALK_ROW_HEIGHT = 64.dp
private val MIN_OTHER_ROW_HEIGHT = 40.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchingScreen(
    viewModel: WatchingViewModel,
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val expandedThreads by viewModel.expandedThreads.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = {
            MoltonfTopAppBar(
                title = { Text(((uiState as? WatchingUiState.Loaded)?.story?.villageFullName) ?: "観戦") },
                navigationIcon = {
                    NeutralOutlinedButton(onClick = onBack, text = "戻る")
                },
            )
        },
        bottomBar = {
            (uiState as? WatchingUiState.Loaded)?.let { loaded ->
                WatchingNavigationBar(selectedTab = loaded.selectedTab, onSelectTab = viewModel::selectTab)
            }
        },
    ) { innerPadding ->
        when (val state = uiState) {
            is WatchingUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is WatchingUiState.Loaded -> {
                // Hoisted here so the talk list keeps its scroll position while other tabs are shown.
                val listState = rememberLazyListState()
                var scrolledToTopForDay by rememberSaveable { mutableStateOf(state.currentDay) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        // The bottom bar's inset is already applied above, so only the part of the
                        // keyboard that goes beyond it pushes the content up (e.g. the memo field).
                        .consumeWindowInsets(innerPadding)
                        .imePadding(),
                ) {
                    when (state.selectedTab) {
                        WatchingTab.TALK -> TalkTab(
                            state = state,
                            viewModel = viewModel,
                            expandedThreads = expandedThreads,
                            listState = listState,
                            scrolledToTopForDay = scrolledToTopForDay,
                            onScrolledToTopForDay = { scrolledToTopForDay = it },
                            modifier = Modifier.weight(1f),
                        )
                        WatchingTab.BOOKMARK -> BookmarkTab(state, viewModel, Modifier.weight(1f))
                        WatchingTab.CONSIDERATION -> ConsiderationTab(viewModel, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun WatchingNavigationBar(selectedTab: WatchingTab, onSelectTab: (WatchingTab) -> Unit) {
    Column {
        HorizontalDivider(thickness = 2.dp, color = MaterialTheme.colorScheme.background)
        NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest) {
            WatchingTab.entries.forEach { tab ->
                NavigationBarItem(
                    selected = selectedTab == tab,
                    onClick = { onSelectTab(tab) },
                    icon = { Icon(painterResource(tab.icon), contentDescription = null) },
                    label = { Text(tab.title) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface,
                        indicatorColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    ),
                )
            }
        }
    }
}

private val WatchingTab.icon: DrawableResource
    get() = when (this) {
        WatchingTab.TALK -> Res.drawable.ic_chat_bubble
        WatchingTab.BOOKMARK -> Res.drawable.ic_bookmark
        WatchingTab.CONSIDERATION -> Res.drawable.ic_lightbulb
    }

private val WatchingTab.title: String
    get() = when (this) {
        WatchingTab.TALK -> "トーク"
        WatchingTab.BOOKMARK -> "ブックマーク"
        WatchingTab.CONSIDERATION -> "考察"
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TalkTab(
    state: WatchingUiState.Loaded,
    viewModel: WatchingViewModel,
    expandedThreads: Map<String, TalkThreadEntry>,
    listState: LazyListState,
    scrolledToTopForDay: Int,
    onScrolledToTopForDay: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewerCharacter = state.characterMap[state.playerCharacter]
    val isVisible: (TalkWithDay) -> Boolean = { talkWithDay ->
        viewerCharacter != null &&
            isTalkVisible(state.story, talkWithDay.day, talkWithDay.talk, viewerCharacter, state.dayProgress)
    }
    // Maps list items to the element they belong to (an element's row plus the talk thread
    // rows expanded beneath it), because readPosition is an element index and does not
    // change when threads are expanded or collapsed. The owner is the element's index in the
    // unfiltered list, so readPosition does not depend on the talk filter either.
    val itemOwners = mutableListOf<Int>()
    // The list item index of each displayed element's row (parallel to state.displayedElements).
    val displayedItemIndices = mutableListOf<Int>()
    state.displayedElements.forEach { (index, element) ->
        displayedItemIndices += itemOwners.size
        itemOwners += index
        val threadSize = expandedThreads[threadKey(state.currentDay, element.elementId)]
            ?.flattenDescendants()?.size ?: 0
        repeat(threadSize) { itemOwners += index }
    }
    var bookmarkTarget by remember { mutableStateOf<Talk?>(null) }
    var highlightedElementId by remember { mutableStateOf<String?>(null) }
    // Separate from the scroll effect, which restarts once the scroll target is consumed.
    LaunchedEffect(highlightedElementId) {
        if (highlightedElementId != null) {
            delay(HIGHLIGHT_FLASH_HOLD)
            highlightedElementId = null
        }
    }
    val currentItemOwners by rememberUpdatedState(itemOwners.toList())
    val currentLastElementIndex by rememberUpdatedState(state.elements.lastIndex)
    val isNewestDay = state.dayProgress != null && state.currentDay >= state.dayProgress
    // On the newest revealed day, the furthest item seen is persisted in the workspace so
    // that "scroll to bottom" never jumps past what the user has seen (spoiler prevention).
    val furthestSeenIndex = state.readPosition
    // A pending jump from the bookmark list wins. Otherwise reset to top only when the day
    // actually changes, not merely when this tab re-enters composition — otherwise the
    // scroll position kept in listState would be immediately discarded.
    LaunchedEffect(state.currentDay, isNewestDay, state.scrollTargetElementId) {
        val target = state.scrollTargetElementId
        if (target != null) {
            val displayedPos = state.displayedElements.indexOfFirst { it.element.elementId == target }
            listState.scrollToItem(displayedItemIndices.getOrNull(displayedPos) ?: 0)
            onScrolledToTopForDay(state.currentDay)
            highlightedElementId = target
            viewModel.consumeScrollTarget()
            return@LaunchedEffect
        }
        if (state.currentDay != scrolledToTopForDay) {
            listState.scrollToItem(0)
            onScrolledToTopForDay(state.currentDay)
        }
        // Tracking starts only after the reset above so a stale scroll position from the
        // previously shown day is never recorded as read.
        if (!isNewestDay) return@LaunchedEffect
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
            .collect { itemIndex ->
                // Items past the owners list (e.g. the "next day" button) mean the whole day was seen.
                val owners = currentItemOwners
                val owner = owners.getOrNull(itemIndex) ?: currentLastElementIndex
                if (owner >= 0) viewModel.updateReadPosition(owner)
            }
    }
    var filterExpanded by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val lastItemIndex = (listState.layoutInfo.totalItemsCount - 1).coerceAtLeast(0)

    Column(modifier = modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            DayChanger(
                watchableDays = state.watchableDays,
                currentDay = state.currentDay,
                onDaySelected = viewModel::changeDay,
                modifier = Modifier.weight(1f),
                trailingContent = {
                    TalkFilterToggleButton(
                        isFilterActive = !state.talkFilter.isDefault,
                        onClick = { filterExpanded = !filterExpanded },
                    )
                },
            )
            NeutralOutlinedButton(
                onClick = { scope.launch { listState.animateScrollToItem(0) } },
                text = "先頭",
            )
            NeutralOutlinedButton(
                onClick = {
                    // readPosition indexes the unfiltered elements; the element it points to may be
                    // hidden by the talk filter, in which case use the last displayed one before it.
                    val displayedPos = state.displayedElements.indexOfLast { it.index <= furthestSeenIndex }
                    val target = if (isNewestDay) {
                        displayedItemIndices.getOrNull(displayedPos) ?: lastItemIndex
                    } else {
                        lastItemIndex
                    }
                    scope.launch {
                        if (!isNewestDay) {
                            listState.animateScrollToItem(target)
                            return@launch
                        }
                        if (displayedPos < 0) {
                            listState.animateScrollToItem(0)
                            return@launch
                        }
                        // Scroll so that only the top part of the last-seen element is shown, with
                        // a fixed length that is no longer than any element (so nothing unseen
                        // below it can appear). Unlike aligning its bottom to the viewport's,
                        // this needs no knowledge of the element's real height.
                        val revealPx = with(density) {
                            val element = state.displayedElements.getOrNull(displayedPos)?.element
                            val storyElement = (element as? WatchingElement.Element)?.storyElement
                            val isTalkLike = storyElement is Talk || storyElement is Assault
                            (if (isTalkLike) MIN_TALK_ROW_HEIGHT else MIN_OTHER_ROW_HEIGHT).toPx().toInt()
                        }
                        val info = listState.layoutInfo
                        val viewportEnd = info.viewportEndOffset - info.afterContentPadding
                        val item = info.visibleItemsInfo.firstOrNull { it.index == target }
                        if (item != null) {
                            // Already on screen: only move forward, and only if needed.
                            val cutOff = item.offset + revealPx - viewportEnd
                            if (cutOff > 0) listState.animateScrollBy(cutOff.toFloat())
                        } else {
                            val viewportHeight = viewportEnd - info.viewportStartOffset - info.beforeContentPadding
                            listState.animateScrollToItem(target, (revealPx - viewportHeight).coerceAtMost(0))
                        }
                    }
                },
                modifier = Modifier.padding(start = 4.dp, end = 8.dp),
                text = "末尾",
            )
        }
        LazyColumn(state = listState, modifier = Modifier.weight(1f).fillMaxWidth()) {
            state.displayedElements.forEach { (_, element) ->
                item(key = element.elementId) {
                    WatchingElementRow(
                        element = element,
                        day = state.currentDay,
                        story = state.story,
                        characterMap = state.characterMap,
                        talkMap = state.talkMap,
                        isTalkVisible = isVisible,
                        onLinkClick = { talk, linkKey, targets ->
                            viewModel.toggleLink(state.currentDay, talk, emptyList(), linkKey, targets)
                        },
                        bookmarkedElementIds = state.bookmarkedElementIds,
                        onBookmarkClick = { talk ->
                            // A bookmarked talk's star shows its bookmark in the list; otherwise add a new one.
                            val existing = state.bookmarkItems.firstOrNull {
                                it.bookmark.day == state.currentDay && it.bookmark.elementId == talk.elementId
                            }
                            if (existing != null) viewModel.showBookmark(existing.bookmark.id) else bookmarkTarget = talk
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(highlightFlashColor(element.elementId == highlightedElementId))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                    )
                }
                val thread = expandedThreads[threadKey(state.currentDay, element.elementId)]
                if (thread != null) thread.flattenDescendants().forEach { flat ->
                    item(key = flat.entry.id) {
                        TalkView(
                            talk = flat.entry.talk,
                            day = flat.entry.day,
                            story = state.story,
                            characterMap = state.characterMap,
                            talkMap = state.talkMap,
                            isTalkVisible = isVisible,
                            onLinkClick = { linkKey, targets ->
                                viewModel.toggleLink(state.currentDay, thread.talk, flat.path, linkKey, targets)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MoltonfColors.linkedTalkBackground)
                                .padding(start = (8 + flat.depth * 16).dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                        )
                    }
                }
            }
            if (state.canMoveToNextDay) {
                item {
                    Button(
                        onClick = viewModel::moveToNextDay,
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Text("次の日へ")
                    }
                }
            }
        }
        if (filterExpanded) {
            ModalBottomSheet(onDismissRequest = { filterExpanded = false }) {
                TalkFilterPanel(
                    avatars = state.story.avatarList,
                    filter = state.talkFilter,
                    onToggleAvatar = viewModel::toggleAvatarFilter,
                    onToggleTalkType = viewModel::toggleTalkTypeFilter,
                    onCheckAllAvatars = viewModel::checkAllAvatars,
                    onUncheckAllAvatars = viewModel::uncheckAllAvatars,
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                )
            }
        }
    }

    bookmarkTarget?.let { talk ->
        val day = state.currentDay
        BookmarkEditDialog(
            initialComment = "",
            isExisting = false,
            onSave = {
                viewModel.saveBookmark(day, talk.elementId, it)
                bookmarkTarget = null
            },
            onDismiss = { bookmarkTarget = null },
        )
    }
}
