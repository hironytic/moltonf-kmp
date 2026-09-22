package com.hironytic.moltonfkmp.ui.watching

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DayChanger(
    watchableDays: List<WatchableDay>,
    currentDay: Int,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.horizontalScroll(rememberScrollState()).padding(8.dp)) {
        watchableDays.forEach { watchableDay ->
            FilterChip(
                selected = watchableDay.day == currentDay,
                onClick = { onDaySelected(watchableDay.day) },
                label = { Text(watchableDay.text) },
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
}
