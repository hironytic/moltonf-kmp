package com.hironytic.moltonfkmp.ui.watching

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hironytic.moltonfkmp.ui.theme.MoltonfFilterChip

@Composable
fun DayChanger(
    watchableDays: List<WatchableDay>,
    currentDay: Int,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    trailingContent: @Composable () -> Unit = {},
) {
    Row(modifier = modifier.padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        DayChips(watchableDays, currentDay, onDaySelected, Modifier.weight(1f))
        trailingContent()
    }
}

@Composable
private fun DayChips(
    watchableDays: List<WatchableDay>,
    currentDay: Int,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier,
) {
    Row(modifier = modifier.horizontalScroll(rememberScrollState()).padding(vertical = 8.dp)) {
        watchableDays.forEach { watchableDay ->
            MoltonfFilterChip(
                selected = watchableDay.day == currentDay,
                onClick = { onDaySelected(watchableDay.day) },
                label = watchableDay.text,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
}
