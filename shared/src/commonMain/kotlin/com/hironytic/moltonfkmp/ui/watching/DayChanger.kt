package com.hironytic.moltonfkmp.ui.watching

import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hironytic.moltonfkmp.ui.theme.MoltonfFilterChip

/**
 * A row of day chips spanning the full width. The selected day is kept scrolled into the
 * center of the row whenever it changes.
 */
@Composable
fun DayChanger(
    watchableDays: List<WatchableDay>,
    currentDay: Int,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    LaunchedEffect(currentDay, watchableDays) {
        val index = watchableDays.indexOfFirst { it.day == currentDay }
        if (index < 0) return@LaunchedEffect
        // Bring the chip into composition first so that its size and offset are known.
        if (listState.layoutInfo.visibleItemsInfo.none { it.index == index }) {
            listState.scrollToItem(index)
        }
        val info = listState.layoutInfo
        val item = info.visibleItemsInfo.firstOrNull { it.index == index } ?: return@LaunchedEffect
        val viewportCenter = (info.viewportStartOffset + info.viewportEndOffset) / 2
        val itemCenter = item.offset + item.size / 2
        // Scrolling stops at the ends of the list, so the first/last chips are not over-scrolled.
        listState.animateScrollBy((itemCenter - viewportCenter).toFloat())
    }
    LazyRow(
        state = listState,
        modifier = modifier.padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(watchableDays, key = { it.day }) { watchableDay ->
            MoltonfFilterChip(
                selected = watchableDay.day == currentDay,
                onClick = { onDaySelected(watchableDay.day) },
                label = watchableDay.text,
            )
        }
    }
}
