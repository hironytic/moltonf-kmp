package com.hironytic.moltonfkmp.ui.watching

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hironytic.moltonfkmp.story.TalkWithDay
import com.hironytic.moltonfkmp.ui.theme.MoltonfTopAppBar
import com.hironytic.moltonfkmp.ui.theme.NeutralOutlinedButton

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
                LaunchedEffect(state.currentDay) {
                    if (state.currentDay != scrolledToTopForDay) {
                        listState.scrollToItem(0)
                        scrolledToTopForDay = state.currentDay
                    }
                }

                Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    DayChanger(
                        watchableDays = state.watchableDays,
                        currentDay = state.currentDay,
                        onDaySelected = viewModel::changeDay,
                    )
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
