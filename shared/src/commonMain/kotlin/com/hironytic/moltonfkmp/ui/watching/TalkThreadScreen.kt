package com.hironytic.moltonfkmp.ui.watching

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hironytic.moltonfkmp.story.TalkWithDay
import com.hironytic.moltonfkmp.ui.theme.MoltonfTopAppBar
import com.hironytic.moltonfkmp.ui.theme.NeutralOutlinedButton

/**
 * Screen shown when a talk-mention link is tapped: the linking talk plus the talk(s) it points
 * to. Tapping a link inside any of the displayed talks toggles a further expansion directly
 * below it (tree-like open/close), instead of navigating to another screen.
 */
@Composable
fun TalkThreadScreen(
    viewModel: WatchingViewModel,
    onClose: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val root by viewModel.talkThread.collectAsState()

    // Clears the shared talk-thread state whenever this screen leaves composition, regardless of
    // whether it was dismissed via the close button or a system back gesture.
    DisposableEffect(Unit) {
        onDispose { viewModel.closeTalkThread() }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = {
            MoltonfTopAppBar(
                title = { Text("発言のつながり") },
                navigationIcon = {
                    NeutralOutlinedButton(onClick = onClose, text = "閉じる")
                },
            )
        },
    ) { innerPadding ->
        val state = uiState as? WatchingUiState.Loaded
        val rootEntry = root
        if (state != null && rootEntry != null) {
            val viewerCharacter = state.characterMap[state.playerCharacter]
            val isVisible: (TalkWithDay) -> Boolean = { talkWithDay ->
                viewerCharacter != null &&
                    isTalkVisible(state.story, talkWithDay.day, talkWithDay.talk, viewerCharacter, state.dayProgress)
            }

            LazyColumn(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                items(rootEntry.flatten(), key = { it.entry.id }) { flat ->
                    TalkView(
                        talk = flat.entry.talk,
                        day = flat.entry.day,
                        story = state.story,
                        characterMap = state.characterMap,
                        talkMap = state.talkMap,
                        isTalkVisible = isVisible,
                        onLinkClick = { linkKey, targets -> viewModel.toggleThreadLink(flat.path, linkKey, targets) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = (8 + flat.depth * 16).dp, top = 8.dp, end = 8.dp),
                    )
                }
            }
        }
    }
}
