package com.hironytic.moltonfkmp.ui.watching

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * The Material "filter_alt" icon (Filled), copied locally instead of depending on the very large
 * material-icons-extended library. The path data is from Google's Material Icons, licensed under
 * the Apache License 2.0.
 */
internal val FilterAltIcon: ImageVector = ImageVector.Builder(
    name = "Filled.FilterAlt",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).apply {
    path(fill = SolidColor(Color.Black)) {
        moveTo(4.25f, 5.61f)
        curveTo(6.27f, 8.2f, 10.0f, 13.0f, 10.0f, 13.0f)
        verticalLineToRelative(6.0f)
        curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
        horizontalLineToRelative(2.0f)
        curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
        verticalLineToRelative(-6.0f)
        curveToRelative(0.0f, 0.0f, 3.72f, -4.8f, 5.74f, -7.39f)
        curveTo(20.25f, 4.95f, 19.78f, 4.0f, 18.95f, 4.0f)
        horizontalLineTo(5.04f)
        curveTo(4.21f, 4.0f, 3.74f, 4.95f, 4.25f, 5.61f)
        close()
    }
}.build()
