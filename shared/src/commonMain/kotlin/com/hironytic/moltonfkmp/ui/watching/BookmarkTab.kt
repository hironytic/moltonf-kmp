package com.hironytic.moltonfkmp.ui.watching

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hironytic.moltonfkmp.story.TalkWithDay
import com.hironytic.moltonfkmp.workspace.Bookmark
import kotlinx.coroutines.delay
import moltonf_kmp.shared.generated.resources.Res
import moltonf_kmp.shared.generated.resources.ic_comment
import org.jetbrains.compose.resources.painterResource

/** The list of bookmarks, each shown with its comment and talk. */
@Composable
fun BookmarkTab(
    state: WatchingUiState.Loaded,
    viewModel: WatchingViewModel,
    modifier: Modifier = Modifier,
) {
    var editing by remember { mutableStateOf<Bookmark?>(null) }
    var deleting by remember { mutableStateOf<Bookmark?>(null) }
    val viewerCharacter = state.characterMap[state.playerCharacter]
    val isVisible: (TalkWithDay) -> Boolean = { talkWithDay ->
        viewerCharacter != null &&
            isTalkVisible(state.story, talkWithDay.day, talkWithDay.talk, viewerCharacter, state.dayProgress)
    }

    if (state.bookmarkItems.isEmpty()) {
        Box(modifier = modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text(
                text = "ブックマークはありません。トーク画面の ☆ から追加できます。",
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    } else {
        val listState = rememberLazyListState()
        var highlightedId by remember { mutableStateOf<String?>(null) }
        // Jump requested from the star of a bookmarked talk on the talk tab.
        LaunchedEffect(state.scrollTargetBookmarkId) {
            val target = state.scrollTargetBookmarkId ?: return@LaunchedEffect
            val index = state.bookmarkItems.indexOfFirst { it.bookmark.id == target }
            if (index >= 0) {
                listState.scrollToItem(index)
                highlightedId = target
            }
            viewModel.consumeBookmarkScrollTarget()
        }
        // Separate from the effect above, which restarts once the scroll target is consumed.
        LaunchedEffect(highlightedId) {
            if (highlightedId != null) {
                delay(HIGHLIGHT_FLASH_HOLD)
                highlightedId = null
            }
        }
        LazyColumn(state = listState, modifier = modifier.fillMaxSize()) {
            items(state.bookmarkItems, key = { it.bookmark.id }) { item ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(highlightFlashColor(item.bookmark.id == highlightedId))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = state.watchableDays.getOrNull(item.bookmark.day)?.text ?: "${item.bookmark.day}日目",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    TalkView(
                        talk = item.talk,
                        day = item.bookmark.day,
                        story = state.story,
                        characterMap = state.characterMap,
                        talkMap = state.talkMap,
                        isTalkVisible = isVisible,
                        onLinkClick = { _, _ -> },
                    )
                    if (item.bookmark.comment.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_comment),
                                contentDescription = "コメント",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 2.dp).size(18.dp),
                            )
                            Text(
                                text = item.bookmark.comment,
                                color = MaterialTheme.colorScheme.inverseSurface,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp).weight(1f),
                            )
                        }
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        // Kept apart from the other actions so it is not tapped by mistake.
                        TextButton(
                            onClick = { deleting = item.bookmark },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        ) { Text("削除") }
                        Row {
                            TextButton(onClick = { editing = item.bookmark }) { Text("編集") }
                            TextButton(onClick = { viewModel.jumpToBookmark(item.bookmark) }) { Text("トークへ移動") }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }

    editing?.let { bookmark ->
        BookmarkEditDialog(
            initialComment = bookmark.comment,
            isExisting = true,
            onSave = {
                viewModel.saveBookmark(bookmark.day, bookmark.elementId, it)
                editing = null
            },
            onDismiss = { editing = null },
        )
    }

    deleting?.let { bookmark ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("ブックマークを削除") },
            text = { Text("このブックマークを削除しますか？コメントも削除され、元に戻せません。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeBookmark(bookmark.id)
                        deleting = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("削除") }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) { Text("キャンセル") }
            },
        )
    }
}
