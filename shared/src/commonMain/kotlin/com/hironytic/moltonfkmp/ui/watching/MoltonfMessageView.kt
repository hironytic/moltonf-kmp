package com.hironytic.moltonfkmp.ui.watching

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MoltonfMessageView(message: WatchingElement.Message, modifier: Modifier = Modifier) {
    val color = Color(0xFFB08D2A)
    OutlinedCard(
        modifier = modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, color),
        colors = CardDefaults.outlinedCardColors(contentColor = color),
    ) {
        Text(
            text = message.messageLines.joinToString("\n"),
            modifier = Modifier.padding(8.dp),
        )
    }
}
