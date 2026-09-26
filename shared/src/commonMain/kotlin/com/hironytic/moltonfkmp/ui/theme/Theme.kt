package com.hironytic.moltonfkmp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Same color scheme as moltonf-web: a dark theme based on Tailwind's gray palette, with red as
 * the accent color. The page is [Palette.Gray900] and the content panels are black.
 */
private val MoltonfColorScheme = darkColorScheme(
    primary = Palette.Red600,
    onPrimary = Palette.White,
    primaryContainer = Palette.Red800,
    onPrimaryContainer = Palette.White,
    inversePrimary = Palette.Red500,
    secondary = Palette.Gray600,
    onSecondary = Palette.White,
    secondaryContainer = Palette.Gray700,
    onSecondaryContainer = Palette.White,
    tertiary = Palette.Gray600,
    onTertiary = Palette.White,
    tertiaryContainer = Palette.Gray700,
    onTertiaryContainer = Palette.White,
    background = Palette.Gray900,
    onBackground = Palette.Gray400,
    surface = Palette.Gray900,
    onSurface = Palette.Gray400,
    surfaceVariant = Palette.Gray800,
    onSurfaceVariant = Palette.Gray400,
    surfaceTint = Palette.Red600,
    inverseSurface = Palette.Gray300,
    inverseOnSurface = Palette.Gray900,
    error = Palette.Red500,
    onError = Palette.White,
    errorContainer = Palette.Red800,
    onErrorContainer = Palette.White,
    outline = Palette.Gray600,
    outlineVariant = Palette.Gray700,
    scrim = Palette.Black,
    surfaceBright = Palette.Gray700,
    surfaceDim = Palette.Black,
    surfaceContainerLowest = Palette.Black,
    surfaceContainerLow = Palette.Gray900,
    surfaceContainer = Palette.Gray800,
    surfaceContainerHigh = Palette.Gray800,
    surfaceContainerHighest = Palette.Gray700,
)

@Composable
fun MoltonfTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = MoltonfColorScheme) {
        // Everything is drawn on top of the black content panel; this also makes the default
        // content color follow the theme.
        Surface(color = MaterialTheme.colorScheme.surfaceContainerLowest, content = content)
    }
}
