package com.hironytic.moltonfkmp.ui.watching

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hironytic.moltonfkmp.story.StoryEvent
import com.hironytic.moltonfkmp.story.StoryEventExtra
import com.hironytic.moltonfkmp.story.StoryEventOrder

@Composable
fun StoryEventView(storyEvent: StoryEvent, modifier: Modifier = Modifier) {
    val color = when (storyEvent) {
        is StoryEventOrder -> MaterialTheme.colorScheme.error
        is StoryEventExtra -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, color),
        colors = CardDefaults.outlinedCardColors(contentColor = color),
    ) {
        Text(
            text = storyEvent.messageLines.joinToString("\n"),
            modifier = Modifier.padding(8.dp),
        )
    }
}
