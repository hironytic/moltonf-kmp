package com.hironytic.moltonfkmp.ui.watching

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import kotlinx.coroutines.launch

/**
 * Lower bounds of the height of a list row, used by the "scroll to bottom" button.
 * A talk row has at least a name line, the 40dp avatar icon and the row's own vertical padding
 * (the name line is about 16dp at the default font size; kept a little below the real value).
 * Other rows (events, synthesized messages) are just a bordered text.
 */
private val MIN_TALK_ROW_HEIGHT = 64.dp
private val MIN_OTHER_ROW_HEIGHT = 40.dp

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
    ) { innerPadding ->
        when (val state = uiState) {
            is WatchingUiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is WatchingUiState.Loaded -> {
                val viewerCharacter = state.characterMap[state.playerCharacter]
                val isVisible: (TalkWithDay) -> Boolean = { talkWithDay ->
                    viewerCharacter != null &&
                        isTalkVisible(state.story, talkWithDay.day, talkWithDay.talk, viewerCharacter, state.dayProgress)
                }
                val listState = rememberLazyListState()
                // Reset to top only when the day actually changes, not merely when this screen
                // re-enters composition — otherwise the
                // scroll position restored by rememberLazyListState would be immediately discarded.
                var scrolledToTopForDay by rememberSaveable { mutableStateOf(state.currentDay) }
                // Maps list items to the element they belong to (an element's row plus the talk thread
                // rows expanded beneath it), because readPosition is an element index and does not
                // change when threads are expanded or collapsed.
                val itemOwners = mutableListOf<Int>()
                val elementItemIndices = mutableListOf<Int>()
                state.elements.forEachIndexed { i, element ->
                    elementItemIndices += itemOwners.size
                    itemOwners += i
                    val threadSize = expandedThreads[threadKey(state.currentDay, element.elementId)]
                        ?.flattenDescendants()?.size ?: 0
                    repeat(threadSize) { itemOwners += i }
                }
                val currentItemOwners by rememberUpdatedState(itemOwners.toList())
                val isNewestDay = state.dayProgress != null && state.currentDay >= state.dayProgress
                // On the newest revealed day, the furthest item seen is persisted in the workspace so
                // that "scroll to bottom" never jumps past what the user has seen (spoiler prevention).
                val furthestSeenIndex = state.readPosition
                LaunchedEffect(state.currentDay, isNewestDay) {
                    if (state.currentDay != scrolledToTopForDay) {
                        listState.scrollToItem(0)
                        scrolledToTopForDay = state.currentDay
                    }
                    // Tracking starts only after the reset above so a stale scroll position from the
                    // previously shown day is never recorded as read.
                    if (!isNewestDay) return@LaunchedEffect
                    snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
                        .collect { itemIndex ->
                            // Items past the owners list (e.g. the "next day" button) mean the whole day was seen.
                            val owners = currentItemOwners
                            val owner = owners.getOrNull(itemIndex) ?: owners.lastIndex
                            if (owner >= 0) viewModel.updateReadPosition(owner)
                        }
                }
                val scope = rememberCoroutineScope()
                val density = LocalDensity.current
                val lastItemIndex = (listState.layoutInfo.totalItemsCount - 1).coerceAtLeast(0)

                Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DayChanger(
                            watchableDays = state.watchableDays,
                            currentDay = state.currentDay,
                            onDaySelected = viewModel::changeDay,
                            modifier = Modifier.weight(1f),
                        )
                        NeutralOutlinedButton(
                            onClick = { scope.launch { listState.animateScrollToItem(0) } },
                            text = "先頭",
                        )
                        NeutralOutlinedButton(
                            onClick = {
                                val target = if (isNewestDay) {
                                    elementItemIndices.getOrNull(furthestSeenIndex) ?: lastItemIndex
                                } else {
                                    lastItemIndex
                                }
                                scope.launch {
                                    if (!isNewestDay) {
                                        listState.animateScrollToItem(target)
                                        return@launch
                                    }
                                    // Scroll so that only the top part of the last-seen element is shown, with
                                    // a fixed length that is no longer than any element (so nothing unseen
                                    // below it can appear). Unlike aligning its bottom to the viewport's,
                                    // this needs no knowledge of the element's real height.
                                    val revealPx = with(density) {
                                        val element = state.elements.getOrNull(furthestSeenIndex)
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
                        state.elements.forEach { element ->
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
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
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
                }
            }
        }
    }
}
