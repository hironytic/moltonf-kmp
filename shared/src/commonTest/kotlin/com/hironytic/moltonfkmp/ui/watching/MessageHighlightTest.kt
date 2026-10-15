package com.hironytic.moltonfkmp.ui.watching

import com.hironytic.moltonfkmp.ui.theme.MoltonfColors
import kotlin.test.Test
import kotlin.test.assertEquals

class MessageHighlightTest {
    @Test
    fun symbolsAreColored() {
        val c = computeHighlightColors("a▼b☆")
        assertEquals(listOf(null, MoltonfColors.messageSymbolBlue, null, MoltonfColors.messageSymbolYellow), c)
    }

    @Test
    fun bracketRangeWithSymbolPriority() {
        val c = computeHighlightColors("x【a★】y")
        val r = MoltonfColors.messageBracketRange
        assertEquals(listOf(null, r, r, MoltonfColors.messageSymbolYellow, r, null), c)
    }

    @Test
    fun unmatchedBracketsAreIgnored() {
        assertEquals(listOf<Any?>(null, null, null), computeHighlightColors("【ax"))
        assertEquals(listOf<Any?>(null, null), computeHighlightColors("a】"))
    }

    @Test
    fun nestedRangesColorEverythingInside() {
        val r = MoltonfColors.messageBracketRange
        assertEquals(List(7) { r }, computeHighlightColors("【a【b】c】"))
    }
}
