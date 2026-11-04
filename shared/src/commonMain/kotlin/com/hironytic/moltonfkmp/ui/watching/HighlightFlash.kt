package com.hironytic.moltonfkmp.ui.watching

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.hironytic.moltonfkmp.ui.theme.MoltonfColors
import kotlin.time.Duration.Companion.milliseconds

/** How long a jumped-to item stays highlighted before it starts fading out. */
val HIGHLIGHT_FLASH_HOLD = 250.milliseconds

/**
 * Background color that flashes to the accent color while [active] and fades out afterwards, used
 * to show which item a jump landed on.
 */
@Composable
fun highlightFlashColor(active: Boolean): Color {
    val color by animateColorAsState(
        targetValue = if (active) MoltonfColors.jumpHighlight else Color.Transparent,
        animationSpec = tween(durationMillis = if (active) 150 else 800),
        label = "highlightFlash",
    )
    return color
}
