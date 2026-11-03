package com.hironytic.moltonfkmp.ui.watching

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hironytic.moltonfkmp.story.TalkWithDay
import com.hironytic.moltonfkmp.ui.theme.MoltonfTopAppBar
import com.hironytic.moltonfkmp.ui.theme.NeutralOutlinedButton
import kotlinx.coroutines.launch

@Composable
fun WatchingScreen(
    viewModel: WatchingViewModel,
    onBack: () -> Unit,
    onOpenTalkThread: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

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
                // re-enters composition (e.g. returning from TalkThreadScreen) — otherwise the
                // scroll position restored by rememberLazyListState would be immediately discarded.
                var scrolledToTopForDay by rememberSaveable { mutableStateOf(state.currentDay) }
                LaunchedEffect(state.currentDay) {
                    if (state.currentDay != scrolledToTopForDay) {
                        listState.scrollToItem(0)
                        scrolledToTopForDay = state.currentDay
                    }
                }

                // On the newest revealed day, remember how far the user has actually scrolled so that
                // "scroll to bottom" never jumps past what they have already seen (spoiler prevention).
                val isNewestDay = state.dayProgress != null && state.currentDay >= state.dayProgress
                var furthestSeenIndex by rememberSaveable(state.currentDay) { mutableIntStateOf(0) }
                LaunchedEffect(state.currentDay, isNewestDay) {
                    if (!isNewestDay) return@LaunchedEffect
                    snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 }
                        .collect { furthestSeenIndex = maxOf(furthestSeenIndex, it) }
                }
                val scope = rememberCoroutineScope()
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
                                val target = if (isNewestDay) minOf(furthestSeenIndex, lastItemIndex) else lastItemIndex
                                scope.launch { listState.animateScrollToItem(target) }
                            },
                            modifier = Modifier.padding(start = 4.dp, end = 8.dp),
                            text = "末尾",
                        )
                    }
                    LazyColumn(state = listState, modifier = Modifier.weight(1f).fillMaxWidth()) {
                        items(state.elements, key = { it.elementId }) { element ->
                            WatchingElementRow(
                                element = element,
                                day = state.currentDay,
                                story = state.story,
                                characterMap = state.characterMap,
                                talkMap = state.talkMap,
                                isTalkVisible = isVisible,
                                onLinkClick = { talk, linkKey, targets ->
                                    viewModel.openTalkThread(state.currentDay, talk, linkKey, targets)
                                    onOpenTalkThread()
                                },
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                            )
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
