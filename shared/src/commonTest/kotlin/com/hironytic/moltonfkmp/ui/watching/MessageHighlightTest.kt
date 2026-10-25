package com.hironytic.moltonfkmp.ui.watching

import com.hironytic.moltonfkmp.ui.theme.MoltonfColors
import kotlin.test.Test
import kotlin.test.assertEquals

class MessageHighlightTest {
    private val p = MoltonfColors.messageHighlightPublic

    @Test
    fun symbolsAreColored() {
        val c = computeHighlightColors("a▼b☆", p)
        assertEquals(listOf(null, p.symbolPurple, null, p.symbolOrange), c)
    }

    @Test
    fun bracketRangeWithSymbolPriority() {
        val c = computeHighlightColors("x【a★】y", p)
        val r = p.bracketRange
        assertEquals(listOf(null, r, r, p.symbolOrange, r, null), c)
    }

    @Test
    fun unmatchedBracketsAreIgnored() {
        assertEquals(listOf<Any?>(null, null, null), computeHighlightColors("【ax", p))
        assertEquals(listOf<Any?>(null, null), computeHighlightColors("a】", p))
    }

    @Test
    fun onlyInnermostNestedRangeIsColored() {
        val r = p.bracketRange
        assertEquals(listOf(null, null, r, r, r, null, null), computeHighlightColors("【a【b】c】", p))
    }
}
