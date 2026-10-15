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
 *   when nested, only the innermost range is highlighted (not the outer ones), and unmatched
 *   brackets are not highlighted.
 * - A symbol inside a bracketed range keeps the symbol's color.
 */
fun computeHighlightColors(line: String): List<Color?> {
    val colors = arrayOfNulls<Color>(line.length)
    line.forEachIndexed { i, ch -> colors[i] = symbolColors[ch] }

    // Open brackets, each with whether a closed range has been found inside it.
    val openStack = ArrayList<IntArray>()
    val ranges = ArrayList<IntRange>()
    line.forEachIndexed { i, ch ->
        when (ch) {
            BRACKET_OPEN -> openStack.add(intArrayOf(i, 0))
            BRACKET_CLOSE -> if (openStack.isNotEmpty()) {
                val open = openStack.removeAt(openStack.lastIndex)
                // Only innermost ranges (those containing no other range) are highlighted.
                if (open[1] == 0) ranges.add(open[0]..i)
                openStack.lastOrNull()?.set(1, 1)
            }
        }
    }
    for (range in ranges) {
        for (i in range) {
            if (colors[i] == null) colors[i] = MoltonfColors.messageBracketRange
        }
    }
    return colors.asList()
}
