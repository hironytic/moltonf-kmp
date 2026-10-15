package com.hironytic.moltonfkmp.ui.watching

import androidx.compose.ui.graphics.Color
import com.hironytic.moltonfkmp.ui.theme.MoltonfColors

private const val BRACKET_OPEN = '【'
private const val BRACKET_CLOSE = '】'

private val symbolColors: Map<Char, Color> = mapOf(
    '▼' to MoltonfColors.messageSymbolBlue,
    '▽' to MoltonfColors.messageSymbolPurple,
    '●' to MoltonfColors.messageSymbolBlue,
    '○' to MoltonfColors.messageSymbolPurple,
    '■' to MoltonfColors.messageSymbolOrange,
    '□' to MoltonfColors.messageSymbolOrange,
    '★' to MoltonfColors.messageSymbolYellow,
    '☆' to MoltonfColors.messageSymbolYellow,
)

/**
 * Returns the highlight color of each character of [line] (`null` for no highlight).
 *
 * - Marker symbols (▼, ● and so on) get their own colors.
 * - A range enclosed by 【 and 】 (brackets included) gets [MoltonfColors.messageBracketRange];
 *   the innermost range wins when nested, and unmatched brackets are not highlighted.
 * - A symbol inside a bracketed range keeps the symbol's color.
 */
fun computeHighlightColors(line: String): List<Color?> {
    val colors = arrayOfNulls<Color>(line.length)
    line.forEachIndexed { i, ch -> colors[i] = symbolColors[ch] }

    val openStack = ArrayList<Int>()
    val ranges = ArrayList<IntRange>()
    line.forEachIndexed { i, ch ->
        when (ch) {
            BRACKET_OPEN -> openStack.add(i)
            BRACKET_CLOSE -> if (openStack.isNotEmpty()) ranges.add(openStack.removeAt(openStack.lastIndex)..i)
        }
    }
    // Inner ranges close first, so they claim their characters before the outer ones.
    for (range in ranges) {
        for (i in range) {
            if (colors[i] == null) colors[i] = MoltonfColors.messageBracketRange
        }
    }
    return colors.asList()
}
