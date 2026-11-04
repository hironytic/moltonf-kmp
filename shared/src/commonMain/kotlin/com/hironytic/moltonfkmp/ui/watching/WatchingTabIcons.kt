package com.hironytic.moltonfkmp.ui.watching

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/*
 * Material icons (Filled) used by the watching screen, copied locally instead of
 * depending on the very large material-icons-extended library. The path data is from Google's
 * Material Icons, licensed under the Apache License 2.0.
 */

internal fun materialIcon(name: String, pathData: String): ImageVector = ImageVector.Builder(
    name = name,
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).addPath(addPathNodes(pathData), fill = SolidColor(Color.Black)).build()

/** The Material "chat_bubble" icon. */
internal val ChatBubbleIcon: ImageVector = materialIcon(
    "Filled.ChatBubble",
    "M20,2L4,2c-1.1,0 -2,0.9 -2,2v18l4,-4h14c1.1,0 2,-0.9 2,-2L22,4c0,-1.1 -0.9,-2 -2,-2z",
)

/** The Material "bookmark" icon. */
internal val BookmarkIcon: ImageVector = materialIcon(
    "Filled.Bookmark",
    "M17,3H7c-1.1,0 -1.99,0.9 -1.99,2L5,21l7,-3 7,3V5c0,-1.1 -0.9,-2 -2,-2z",
)

/** The Material "lightbulb" icon. */
internal val LightbulbIcon: ImageVector = materialIcon(
    "Filled.Lightbulb",
    "M9,21c0,0.55 0.45,1 1,1h4c0.55,0 1,-0.45 1,-1v-1L9,20v1zM12,2C8.14,2 5,5.14 5,9c0,2.38 1.19,4.47 3,5.74L8,17c0,0.55 0.45,1 1,1h6c0.55,0 1,-0.45 1,-1v-2.26c1.81,-1.27 3,-3.36 3,-5.74 0,-3.86 -3.14,-7 -7,-7z",
)
